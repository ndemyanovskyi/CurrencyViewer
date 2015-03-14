/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.backend;

import com.ndemyanovskyi.map.unmodifiable.UnmodifiableMapWrapper;
import com.ndemyanovskyi.throwable.Exceptions;
import com.ndemyanovskyi.throwable.RuntimeSQLException;
import static com.ndemyanovskyi.util.Compare.greaterOrEquals;
import com.ndemyanovskyi.app.Settings;
import com.ndemyanovskyi.backend.loader.SimpleLoader;
import com.ndemyanovskyi.backend.loader.SimpleSubscribedLoader;
import com.ndemyanovskyi.backend.site.DocumentParseException;
import com.ndemyanovskyi.backend.site.InterbankSite;
import com.ndemyanovskyi.backend.site.Site;
import com.ndemyanovskyi.derby.Cursor;
import com.ndemyanovskyi.derby.Database;
import com.ndemyanovskyi.derby.Derby;
import com.ndemyanovskyi.derby.Row;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.SetUtils;
import static org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength.HARD;
import static org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength.SOFT;
import org.apache.commons.collections4.map.ReferenceMap;

/**
 *
 * @author Назарій
 */
public final class DataManager {
    
    //Rate list cache
    private static final Set<RateListImpl<?>> CACHE = 
	    Collections.newSetFromMap(new ReferenceMap<>(SOFT, HARD));
    
    private static final Logger LOG = Logger.getLogger(DataManager.class.getName());
    
    private static Database database;
    
    public static <R extends Rate> RateList<R> getCachedRateList(Bank<R> bank, Currency currency) {
	return getCachedRateListImpl(bank, currency);
    }
    
    static <R extends Rate> RateListImpl<R> getCachedRateListImpl(Bank<R> bank, Currency currency) {
	for(RateList<?> list : CACHE) {
	    if(list.is(bank, currency)) {
		return (RateListImpl<R>) list;
	    }
	}
	return null;
    }
    
    static <R extends Rate> RateListImpl<R> getRateListImpl(Bank<R> bank, Currency currency) {
	RateListImpl<R> list = getCachedRateListImpl(bank, currency);
        return list != null ? list : RateListLoader.of(bank, currency).sync();
    }

    public static final Database getDatabase() {
        return database != null ? database : 
                (database = Derby.connect("assets/db/currency_rates"));
    }
    
    private static <R extends Rate> void createTable(Bank<R> bank, Currency currency) {
        try {
            getDatabase().queryUpdate(String.format("CREATE TABLE %s (%s)",
                    getTableName(bank, currency), bank.getDatabaseManager().getLayout(bank, currency)));
        } catch(RuntimeSQLException ex) {}
    }
    
    public static <R extends Rate> Cursor getTable(Bank<R> bank, Currency currency) {
        return DataManager.getTable(bank, currency, Cursor.Type.FORWARD_ONLY, Cursor.Concurrency.READ_ONLY);
    }
    
    public static <R extends Rate> Cursor getTable(Bank<R> bank, Currency currency, Cursor.Type type, Cursor.Concurrency concurrency) {
        String table = getTableName(bank, currency);
        
        Cursor cursor = getDatabase().query("SELECT * FROM " + table, type, concurrency);
        try {
            cursor.init();
            return cursor;
        } catch(RuntimeSQLException ex) {
            createTable(bank, currency);
            return getTable(bank, currency, type, concurrency);
        }
    }
    
    public static <R extends Rate> RateList<R> getRateList(Bank<R> bank, Currency currency) {
        return getRateListImpl(bank, currency);   
    }
    
    @SuppressWarnings("unchecked")
    public static <R extends Rate> void writeRate(R rate) {
        Bank<R> bank = (Bank<R>) rate.getBank();
        String table = getTableName(bank, rate.getCurrency());
        try {
            getDatabase().queryUpdate(bank.getDatabaseManager().getUpdateSql(table, rate));
        } catch(RuntimeSQLException ex) {
            SQLException sqlCause = Database.Utils.extractCause(ex);
            
            //If table does not exists.
            if(sqlCause.getSQLState().equals("42X05")) { 
                createTable(bank, rate.getCurrency());
                writeRate(rate);
            }
        }
    }
    
    static <R extends Rate> LocalDate firstDate(Bank<R> bank, Currency currency) {
        RateListImpl<R> list = getCachedRateListImpl(bank, currency);
        if(list != null) {
            return !list.isEmpty() 
                    ? list.getPeriod().first()
                    : null;
        } 
        return null;
        /*String table = getTableName(bank, currency);
        return getDatabase().queryFirst(
                "SELECT MIN(DATE) FROM " + table).toLocalDate();*/
    }
    
    static <R extends Rate> LocalDate lastDate(Bank<R> bank, Currency currency) {
        RateListImpl<R> list = getCachedRateListImpl(bank, currency);
        if(list != null) {
            return list.getPeriod().last();
        } else {
            String table = getTableName(bank, currency);
            return getDatabase().queryFirst(
                    "SELECT MAX(DATE) FROM " + table).toLocalDate();
        }
    }
    
    public static <R extends Rate> boolean containsDate(Bank<R> bank, Currency currency, LocalDate date) {
        RateListImpl<R> list = getCachedRateListImpl(bank, currency);
        if(list != null) {
            return list.getPeriod().contains(date);
        } else {
            String table = getTableName(bank, currency);
            return getDatabase().queryFirst(
                    String.format("SELECT DATE FROM %s WHERE DATE = DATE('%s')", table, date)).nonNull();
        }
    }
    
    /**
     * Loaded rate from any exist site.
     * @param <R> rate type
     * @param bank any bank, not null.
     * @param currency currency, supported by bank, not null.
     * @param date any date in bounds from minimum to today date, not null.
     * @return loaded rate, not null
     * @throws IOException for any io error.
     * @throws DocumentParseException if document from the last exist site can`t be parsed, or parsed elements count equals zero.
     */
    public static <R extends Rate> R loadRate(Bank<R> bank, Currency currency, LocalDate date) throws IOException, DocumentParseException {
	if(bank.getSite() != null) {
            Set<InterbankSite> sites = Site.supported(InterbankSite.class, bank);
	    BankSiteLoader<R> loader = BankSiteLoader.of(bank, date);
            
            try {
                R rate = loader.sync().get(currency);
                if(rate != null || sites.isEmpty()) return rate;
            } catch(IOException ex) {
                if(sites.isEmpty()) throw ex;
            }
	}
        
        InterbankSiteLoader<R> loader = InterbankSiteLoader.of(currency, date);
        R rate = loader.subscribe(bank);
	if(rate != null) return rate;
	        
        String errorMessage = String.format(
                "Rate can`t be loaded on any exists sites (date = %s, bank = %s, currency = %s", 
                date, bank, currency);
        LOG.log(Level.SEVERE, errorMessage);
        throw new IOException(errorMessage);
    }
    
    static String getTableName(Bank<?> bank, Currency currency) {
        return bank.getName() + "_" + currency.name();
    }
    
    private static class BankSiteLoader<R extends Rate> extends SimpleLoader<Map<Currency, R>> {
        
        private static final Set<BankSiteLoader<?>> LOADERS = new HashSet<>();
        
        private final Bank<R> bank;
        private final LocalDate date;

        public BankSiteLoader(Bank<R> bank, LocalDate date) {
            this.bank = Objects.requireNonNull(bank, "bank");
            this.date = Objects.requireNonNull(date, "date");
        }     
        
        public static <R extends Rate> BankSiteLoader<R> of(Bank<R> bank, LocalDate date) {
            synchronized(LOADERS) {
                for(BankSiteLoader<?> loader : LOADERS) {
                    if(loader.getBank().equals(bank)
                            && loader.getDate().equals(date)
                            && !loader.isFinished()) {
                        return (BankSiteLoader<R>) loader;
                    }
                }

                BankSiteLoader<R> loader = new BankSiteLoader<>(bank, date);
                LOADERS.add(loader);
                return loader;
            }
        }

        public LocalDate getDate() {
            return date;
        }

        public Bank<R> getBank() {
            return bank;
        }

        @Override
        protected void onFinish() {
            LOADERS.remove(this);
        }

        @Override
        public Map<Currency, R> load() throws IOException {
            Map<Currency, R> rates = bank.getSite().load(getDate());
            for(Map.Entry<Currency, R> e : rates.entrySet()) {

                RateListImpl<R> list = getCachedRateListImpl(bank, e.getValue().getCurrency());
                LocalDate firstDate = firstDate(bank, e.getValue().getCurrency());
                if(firstDate == null) {
                    firstDate = LocalDate.now().minusYears(Settings.STORED_DATA_YEARS_COUNT.get());
                }
                R rate = e.getValue();

                if(!rate.isNaN() || greaterOrEquals(rate.getDate(), firstDate)) {
                    if(list != null) {
                        R other = list.get(getDate());
                        if(other != null) {
                            rate = (R) other.merge(rate);
                            if(!rate.equals(other)) {
                                list.modifier().removeOrThrow(other);
                                list.modifier().addOrThrow(rate);
                            }
                        }else {
                            list.modifier().addOrThrow(e.getValue());
                        }
                    }
                    writeRate(e.getValue());
                }
            }
            return rates;
        }
        
    }
    
    private static class InterbankSiteLoader<R extends Rate> extends SimpleSubscribedLoader<Bank<?>, R> {
        
        private static final Set<InterbankSiteLoader<?>> LOADERS = SetUtils.synchronizedSet(new HashSet<>());
        
        private final Currency currency;
        private final List<InterbankSite<Bank<?>, ?>> sites = ListUtils.synchronizedList(new ArrayList<>());
        private final LocalDate date;

        private InterbankSiteLoader(Currency currency, LocalDate date) {
            this.currency = Objects.requireNonNull(currency, "currency");
            this.date = Objects.requireNonNull(date, "date");
            setResult(new UnmodifiableMapWrapper<>(new HashMap<>()));
        }
        
        public static <R extends Rate> InterbankSiteLoader<R> of(Currency currency, LocalDate date) {
            synchronized(LOADERS) {
                for(InterbankSiteLoader<?> loader : LOADERS) {
                    if(loader.getCurrency().equals(currency)
                            && loader.getDate().equals(date)
                            && !loader.isFinished()) {
                        return (InterbankSiteLoader<R>) loader;
                    }
                }

                InterbankSiteLoader<R> loader = new InterbankSiteLoader<>(currency, date);
                LOADERS.add(loader);
                return loader;
            }            
        }

        @Override
        protected void onFinish() {
            LOADERS.remove(this);
        }

        @Override
        public Map<Bank<?>, R> load() throws IOException {
            while(!sites.isEmpty()) {
                InterbankSite<Bank<?>, ?> site = sites.get(0);

                if(getSubscribedLocks().isEmpty()) {
                    return getResult();
                }
                try {
                    Map<Bank<?>, ? extends Rate> rates = site.load(currency, getDate());
                    sites.remove(site);
                    
                    if(rates != null) {
                        getResult().putAll((Map<Bank<?>, R>) rates);
                        for(Map.Entry<Bank<?>, ? extends Rate> e : rates.entrySet()) {
                            RateListImpl<R> list = getCachedRateListImpl((Bank<R>) e.getKey(), currency);
                            if(list != null) {
                                list.modifier().add((R) e.getValue());
                            }
                            writeRate((R) e.getValue());
                            
                            if(!e.getValue().isNaN() || sites.isEmpty()) {
                                getSubscribedLocks().removeIf(lock -> lock.is(e.getKey()));
                            }
                        }
                    }
                } catch(IOException ex) {
                    sites.remove(site);
                    if(sites.isEmpty()) throw ex;
                }
            }
            return getResult();
        } 

        @Override
        protected void onSubscribe(Bank<?> subscriber) {
            if(!subscriber.getCurrencys().contains(getCurrency())) {
                throw new IllegalArgumentException(
                        String.format("Subscriber bank(%s) not supported loader currency(%s)", 
                                subscriber, getCurrency()));
            }
            for(InterbankSite<Bank<?>, ?> site : Site.supported(InterbankSite.class, subscriber)) {
                if(!sites.contains(site)) {
                    sites.add(site);
                }
            }
        }

        public LocalDate getDate() {
            return date;
        }

        public Currency getCurrency() {
            return currency;
        } 
            
    }
    
    private static class RateListLoader<R extends Rate> extends SimpleLoader<RateListImpl<R>> {
        
        private static final Set<RateListLoader<?>> LOADERS = SetUtils.synchronizedSet(new HashSet<>());
        
        private final Bank<R> bank;
        private final Currency currency;

        public RateListLoader(Bank<R> bank, Currency currency) {
            this.bank = Objects.requireNonNull(bank, "bank");
            this.currency = Objects.requireNonNull(currency, "currency");
        }

        public Currency getCurrency() {
            return currency;
        }

        public Bank<R> getBank() {
            return bank;
        }
        
        public boolean is(Bank<?> bank, Currency currency) {
            return getBank().equals(bank) && getCurrency().equals(currency);
        }
        
        public static <R extends Rate> RateListLoader<R> of(Bank<R> bank, Currency currency) {
            synchronized(LOADERS) {
                for(RateListLoader<?> loader : LOADERS) {
                    if(loader.is(bank, currency)) {
                        return (RateListLoader<R>) loader;
                    }
                }

                RateListLoader<R> loader = new RateListLoader<>(bank, currency);
                LOADERS.add(loader);
                return loader;
            }
        }
        
        @Override
        public RateListImpl<R> load() {
            RateListImpl<R> list = getCachedRateListImpl(bank, currency);
            if(list == null) {
                List<R> rates = new ArrayList<>();
                for(Row row : getTable(bank, currency)) {
                    rates.add(bank.getDatabaseManager().getRate(bank, currency, row));
                }
                list = new RateListImpl<>(bank, currency, rates);
                CACHE.add(list);
            }
            return list;
        }

        @Override
        protected void onFinish() {
            LOADERS.remove(this);
        }

        @Override
        public RateListImpl<R> sync() {
            return Exceptions.execute(super::sync);
        }
        
    }
        
}
