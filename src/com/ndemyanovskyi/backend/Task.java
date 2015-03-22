/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.backend;

import com.ndemyanovskyi.app.Application;
import com.ndemyanovskyi.app.Constants;
import com.ndemyanovskyi.backend.site.DocumentParseException;
import com.ndemyanovskyi.collection.set.FilteredSet;
import com.ndemyanovskyi.derby.Database;
import com.ndemyanovskyi.map.Pool;
import com.ndemyanovskyi.map.WeakHashPool;
import com.ndemyanovskyi.time.Period;
import com.ndemyanovskyi.time.Period.Builder;
import com.ndemyanovskyi.util.Compare;
import com.ndemyanovskyi.util.Unmodifiable;
import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Назарій
 * @param <R>
 */
public class Task<R extends Rate> {
    private static final Logger LOG = Logger.getLogger(Task.class.getName());
    
    public static enum Result {
        CANCELLED, SUCCESS, PARTIAL_SUCCESS, FAILURE;
    }
    
    public static enum Source {
        UNKNOWN, DATABASE, INTERNET;
    }
    
    public static class Cause implements Unmodifiable {
        
        public static enum Type {

            UNKNOWN(Source.UNKNOWN),
            ERROR_INTERNET_CONNECTION(Source.INTERNET),
            DATABASE_ALREADY_LOADED(Source.DATABASE, ""),
            DATABASE_LOCK_FAIL(Source.DATABASE, "40XL1"),
            INCORRECT_DOCUMENT(Source.INTERNET);
            
            private static final Set<Type> values = Unmodifiable.set(values());
            private static Pool<Source, Set<Type>> filteredPool;

            private final Source source;
            private final String state;

            private Type(Source source) {
                this(source, "");
            }

            private Type(Source source, String state) {
                this.source = Objects.requireNonNull(source, "source");
                this.state = Objects.requireNonNull(state, "state");
            }

            public Source source() {
                return source;
            }

            public String state() {
                return state;
            }
            
            public static Set<Type> values(Source source) {
                Objects.requireNonNull(source, "source");
                if(filteredPool == null) {
                    filteredPool = new WeakHashPool<>(Source.class, 
                            s -> new FilteredSet<>(values, v -> v.source().equals(s)));
                }
                return filteredPool.get(source);
            }

            public static Type valueOf(Throwable exception) {
                Objects.requireNonNull(exception, "exception");
                if(exception instanceof UnknownHostException) {
                    return ERROR_INTERNET_CONNECTION;
                }
                if(exception instanceof DocumentParseException) {
                    return INCORRECT_DOCUMENT;
                }
                if(exception instanceof SQLException) {
                    SQLException sqlCause = Database.Utils.extractCause(exception);
                    for(Type type : values(Source.DATABASE)) {
                        if(type.state().equals(sqlCause.getSQLState())) {
                            return type;
                        }
                    }
                }
                return UNKNOWN;
            }
            
        }
        
        private final Type type;
        private final Throwable exception;
        
        public Cause(Type type, Throwable exception) {
            this.exception = Objects.requireNonNull(exception, "exception");
            this.type = Objects.requireNonNull(type, "type");
        }
        
        public Cause(Throwable exception) {
            this(Type.valueOf(exception), exception);
        }

        public Throwable getException() {
            return exception;
        }

        public Type getType() {
            return type;
        }
        
    }
    
    private final Bank<R> bank;
    private final Currency currency;
    private Period missingPeriod = Period.EMPTY;
    private RateListImpl<R> rateList;
    
    private Result result;
    private Thread thread;
    private boolean cancelled;
    private boolean finished;
    private boolean errored;
    private boolean started;
    
    private final Builder failurePeriodBuilder = Period.builder();
    private final Builder successPeriodBuilder = Period.builder();

    public Task(Bank<R> bank, Currency currency) {
        this.bank = Objects.requireNonNull(bank, "bank");
        this.currency = Objects.requireNonNull(currency, "currency");
    }

    public Currency getCurrency() {
        return currency;
    }

    public Bank<R> getBank() {
        return bank;
    }
    
    public RateList<R> getRateList() {
        return rateList;
    }

    public void start() {
        checkCancelled();
        checkStarted();
        
        started = true;
        thread = new Thread(() -> {
            try {
                execute();
            } catch(RuntimeException ex) {
                Logger.getGlobal().log(Level.SEVERE, "", ex);
                onError(new Cause(ex));
            }
        });
        thread.start();
    }

    public void cancel() {
        cancelled = true;
    }

    private void checkCancelled() {
        if (isCancelled()) {
            throw new IllegalArgumentException("Task alredy cancelled.");
        }
    }

    private void checkStarted() {
        if (isStarted()) {
            throw new IllegalArgumentException("Task alredy started.");
        }
    }

    public Period getFailurePeriod() {
        return failurePeriodBuilder.build();
    }

    public Period getSuccessPeriod() {
        return successPeriodBuilder.build();
    }
    
    /*private void loadAndNotifyIntermediateSuccess() {
        Cursor cursor = DataManager.getTable(bank, currency, 
                Cursor.Type.SCROLL_INSENSITIVE, Cursor.Concurrency.UPDATABLE);
        
        final int count = cursor.getCount();
        int index = 0;
        
        rateList = new RateListImpl<>(bank, currency);
        
        for(Row row : cursor) {
            R rate = bank.getDatabaseManager().getRate(bank, currency, row);
            rateList.modifier().addOrThrow(rate);
            
            if(!isCancelled()) {
                successPeriodBuilder.plusDate(rate.getDate());
                int tempIndex = index++;
                Application.execute(() -> {
                    onLoadFromDatabaseProgress(((double) tempIndex) / count);
                    onIntermediateSuccess(rate, Source.DATABASE, tempIndex);
                });
            }
        }
        cursor.close();
        DataManager.getRateListCache().add(rateList);
    }*/

    public Period getMissingPeriod() {
        return missingPeriod;
    }

    private void execute() {
        Application.execute(() -> onStart());
        
        //<editor-fold defaultstate="collapsed" desc="Cancelled check">
        if(isCancelled()) {
            onFinish(Result.CANCELLED);
            return;
        }
        //</editor-fold>
        
        rateList = (RateListImpl<R>) DataManager.getRateList(bank, currency);
        
        Period postfix;
        if(!rateList.isEmpty()) {
            postfix = Period.of(rateList.getPeriod().last(), false, LocalDate.now(), true);
        } else {
            LocalDate last = LocalDate.now();
            LocalDate first = last.minusYears(1);
            postfix = Period.of(first, false,last, true);
        }
        
        //<editor-fold defaultstate="collapsed" desc="Cancelled check">
        if(isCancelled()) {
            onFinish(Result.CANCELLED);
            return;
        }
        //</editor-fold>
        
        Application.execute(() -> onLoadFromDatabaseSuccess(rateList));
        
        //<editor-fold defaultstate="collapsed" desc="Cancelled check">
        if(isCancelled()) {
            onFinish(Result.CANCELLED);
            return;
        }
        //</editor-fold>
        
        missingPeriod = Period.builder().
                plusIntervals(rateList.getPeriod().getExcluded()).
                plusInterval(postfix).
                build();
        
        Period period = rateList.getPeriod();
        LocalDate min = Constants.MINIMAL_PERIOD.getValue().first();
        
        if(!period.isEmpty()) {
            LocalDate first = missingPeriod.isEmpty()
                    ? period.first()
                    : Compare.min(period.first(), missingPeriod.first());
            if((first.isAfter(min))) {
                missingPeriod = missingPeriod.plus(Period.of(min, first));
            }
        } else {
            missingPeriod = Period.ofInclusive(min, LocalDate.now());
        }      
        
        //<editor-fold defaultstate="collapsed" desc="Cancelled check">
        if(isCancelled()) {
            onFinish(Result.CANCELLED);
            return;
        }
        //</editor-fold>
        
        int errorConnectionCount = 0;
        int index = rateList.size();
        
        for(LocalDate date : missingPeriod.descendingSet()) {
            
            //<editor-fold defaultstate="collapsed" desc="Cancelled check">
            if(isCancelled()) {
                onFinish(Result.CANCELLED);
                return;
            }
            //</editor-fold>
            
            final int currentIndex = index++;
            try {
                R rate = DataManager.loadRate(bank, currency, date);
                successPeriodBuilder.plusDate(date);
                Application.execute(() -> {
                    onIntermediateSuccess(rate, Source.INTERNET, currentIndex);
                });
            } catch(IOException ex) {
                LOG.log(Level.SEVERE, String.format(
                        "Fail load rate: bank: %s, currency: %s, date: %s.", 
                        getBank(), getCurrency(), date), ex);
                failurePeriodBuilder.plusDate(date);
                Cause cause = new Cause(ex);
                Application.execute(() -> {
                    onIntermediateFailure(date, cause, currentIndex);
                });
                if(cause.getType() == Cause.Type.ERROR_INTERNET_CONNECTION) {
                    errorConnectionCount++;
                    if(errorConnectionCount > 5) {
                        errored = true;
                        onError(cause);
                        return;
                    }
                }
            } finally {
                Application.execute(() -> {
                    onLoadFromInternetProgress(((double) currentIndex) / missingPeriod.size());
                });
            }
        }
        
        result = !isCancelled()
                ? !successPeriodBuilder.isEmpty() 
                        ? !failurePeriodBuilder.isEmpty() 
                                ? Result.PARTIAL_SUCCESS 
                                : Result.SUCCESS
                        : Result.FAILURE
                : Result.CANCELLED;
        
        finished = true;
        Application.execute(() -> onFinish(result));
    }

    public Result getResult() {
        return result;
    }

    public boolean isRunning() {
        return isStarted() && !isCancelled() && !isFinished() && !isErrored();
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isErrored() {
        return errored;
    }
    
    protected void onLoadFromDatabaseProgress(double progress) {
    }
    
    protected void onLoadFromInternetProgress(double progress) {
    }
    
    protected void onLoadFromDatabaseSuccess(RateList<R> list) {
    }

    protected void onIntermediateSuccess(R rate, Source source, int index) {
    }

    protected void onIntermediateFailure(LocalDate date, Cause cause, int index) {
    }

    protected void onStart() {
    }

    protected void onFinish(Result result) {
    } 

    protected void onError(Cause cause) {
    } 
    
}
