/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.backend.site;

import com.ndemyanovskyi.collection.set.FilteredSet;
import com.ndemyanovskyi.collection.set.unmodifiable.UnmodifiableSetWrapper;
import com.ndemyanovskyi.map.HashPool;
import com.ndemyanovskyi.map.Pool;
import static com.ndemyanovskyi.util.DateTimeFormatters.format;
import com.ndemyanovskyi.app.localization.Language;
import com.ndemyanovskyi.backend.Bank;
import com.ndemyanovskyi.backend.CommercialBank;
import com.ndemyanovskyi.backend.Currency;
import com.ndemyanovskyi.backend.ExchangeRate;
import com.ndemyanovskyi.backend.NationalBank;
import com.ndemyanovskyi.backend.Rate;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength.HARD;
import static org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength.SOFT;
import org.apache.commons.collections4.map.ReferenceMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public abstract class Site {
    
    private static final Logger LOG = Logger.getLogger(Site.class.getName());

    private static final Pool<Class<? extends Site>, UnmodifiableSetWrapper<? extends Site>> VALUES
	    = new HashPool<>(o -> {
		if(!(o instanceof Class)) {
		    throw new IllegalArgumentException("Key must be instance of Class<? extends Site>.");
		}
		Class c = (Class) o;
		
		if(!Site.class.isAssignableFrom((Class) o)) {
		    throw new IllegalArgumentException("Key class must be assignable from Site.");
		}
		
		Set<? extends Site> set;
		if(InterbankSite.class.isAssignableFrom((Class) o)) {
		    set = new TreeSet<>(InterbankSite.PRIORITY_COMPARATOR);
		} else {
		    set = new HashSet<>();
		}
		return new UnmodifiableSetWrapper<>(set);
	    });
    
    private static Map<Filter, Set<? extends Site>> filteredMap;

    public static final BankSite<NationalBank, Rate> NBU
	    = new BankSite<NationalBank, Rate>("NBU", "http://www.bank.gov.ua/") {
                
                private static final String TEMPLATE_URL = 
                        "http://www.bank.gov.ua/control/uk/curmetal/currency/search"
                        + "?formType=searchFormDate&time_step=daily&date=%s";

		@Override
		public Map<Currency, Rate> load(LocalDate date) throws IOException {
                    String url = String.format(TEMPLATE_URL, format("dd.MM.yyyy", date));
		    Document doc = Jsoup.connect(url).get();
                    
                    checkRedirected(doc, url);
                    
                    Element secondColl = doc.getElementsByClass("secondColl").first();
                    if(secondColl == null) {
                        throw incorrectDocument(this, doc, 
                                "not found element with class 'secondColl'");
                    }
                    
                    Element content = secondColl.getElementsByClass("content").first();
                    if(content == null) {
                        throw incorrectDocument(this, doc, 
                                "not found element with class 'content'");
                    }
                    
                    
		    Elements tables = content.getElementsByTag("table");
		    if(tables == null || tables.size() < 3) {
                        throw incorrectDocument(this, doc,
                                "not found 3-th element with tag 'table'");
                    }
                    
		    Elements trs = tables.get(3).getElementsByTag("tr");
                    System.out.println(date);
                    
		    Map<Currency, Rate> rates = new EnumMap<>(Currency.class);
                    for(int i = 1; i < trs.size(); i++) {
                        try {
                            Elements tds = trs.get(i).getElementsByTag("td");
                            Currency currency = Currency.valueOf(tds.get(1).text().toUpperCase());
                            float countUah = Float.parseFloat(tds.get(2).text());
                            float countCurrency = Float.parseFloat(tds.get(4).text());
                            float rate = countCurrency / countUah;
                            rates.put(currency, new Rate(Bank.NBU, currency, date, rate));
                        } catch(IllegalArgumentException ex) {}
                    }
                    if(rates.isEmpty()) {
                        throw incorrectDocument(this, doc);
                    }
                    
                    //Adding NaN rates if neeeded.
                    if(rates.size() != Currency.values().length) {
                        Set<Currency> otherCurrencys = new HashSet<>(Arrays.asList(Currency.values()));
                        otherCurrencys.removeAll(rates.keySet());
                        otherCurrencys.remove(Currency.UAH);
                        for(Currency c : otherCurrencys) {
                            rates.put(c, new Rate(Bank.NBU, c, date, Float.NaN));
                        }
                    }
		    return rates;
		}

	    };

    public static final InterbankSite<CommercialBank, ExchangeRate> VALUTA_TODAY
	    = new InterbankSite<CommercialBank, ExchangeRate>("VALUTA_TODAY", 0, "http://valuta.today/", Bank.values(CommercialBank.class)) {

                public static final String TEMPLATE_URL = "http://valuta.today/ukraine/%s/%s/cash.html";
                
		@Override
		public Map<CommercialBank, ExchangeRate> load(Currency currency, LocalDate date) throws IOException {
		    String url = String.format(TEMPLATE_URL, currency, format("yyyy-MM-dd", date));
                    Document doc = Jsoup.connect(url).get();
                    
                    checkRedirected(doc, url);
                    
		    Element table = doc.getElementsByTag("table").first();
		    if(table == null) {
                        throw incorrectDocument(this, doc,
                                "not found element with tag 'table'");
                    }

		    Element tbody = table.getElementsByTag("tbody").first();
		    if(table == null) {
                        throw incorrectDocument(this, doc,
                                "not found element with tag 'tbody'");
                    }

		    Map<CommercialBank, ExchangeRate> rates = new HashMap<>();
		    for(Element e : tbody.getElementsByTag("tr")) {
			Elements data = e.getElementsByTag("td");

			String bankName = data.get(1).text();
                        
			for(CommercialBank bank : getSupportedBanks()) {
			    for(Language language : Language.values()) {
				if(bank.getDisplayName(language).equalsIgnoreCase(bankName)) {
				    float buy = Float.parseFloat(data.get(2).
					    getElementsByClass("rate-value").get(0).text());
				    float sale = Float.parseFloat(data.get(3).
					    getElementsByClass("rate-value").get(0).text());

				    rates.put(bank, new ExchangeRate(bank, currency, date, buy, sale));
				    break;
				}
			    }
			}
		    }
                    
                    if(rates.isEmpty()) {
                        throw incorrectDocument(this, doc);
                    }
                    
                    //Adding NaN rates if neeeded.
                    if(rates.size() != getSupportedBanks().size()) {
                        Set<CommercialBank> otherBanks = new HashSet<>(getSupportedBanks());
                        otherBanks.removeAll(rates.keySet());
                        for(CommercialBank b : otherBanks) {
                            rates.put(b, new ExchangeRate(b, currency, date, Float.NaN, Float.NaN));
                        }
                    }
		    return rates;
		}

	    };

    public static final InterbankSite<CommercialBank, ExchangeRate> SRAVNIBANK
	    = new InterbankSite<CommercialBank, ExchangeRate>("SRAVNIBANK", 2, "http://sravnibank.com.ua/", Bank.values(CommercialBank.class)) {

                public static final String TEMPLATE_URL = 
                        "http://sravnibank.com.ua/nalichnyi-kurs/?currency=%s&date=%s";
                
		@Override
		public Map<CommercialBank, ExchangeRate> load(Currency currency, LocalDate date) throws IOException {
		    String url = String.format(TEMPLATE_URL, 
                            currency.name().toLowerCase(), format("dd.MM.yyyy", date));
                    Document doc = Jsoup.connect(url).get();

                    checkRedirected(doc, url);
                    
		    Element table = doc.getElementById("myTable");
		    if(table == null) {
                        throw incorrectDocument(this, doc,
                                "not found element with id 'myTable'");
                    }

		    Element tbody = table.getElementsByTag("tbody").first();
		    if(table == null) {
                        throw incorrectDocument(this, doc,
                                "not found element with tag 'tbody'");
                    }

		    Map<CommercialBank, ExchangeRate> rates = new HashMap<>();
		    for(Element e : tbody.getElementsByTag("tr")) {
			Elements data = e.getElementsByTag("td");

			String name = data.get(0).text();

			float buy = Float.parseFloat(data.get(1).attr("rel"));
			float sale = Float.parseFloat(data.get(2).attr("rel"));

			try {
			    Bank bank = Bank.ofDisplayName(name);
			    if(bank instanceof CommercialBank) {
				rates.put((CommercialBank) bank,
					new ExchangeRate(bank, currency, date, buy, sale));
			    }
			} catch(IllegalArgumentException ignored) {
			}
		    }
                    
                    if(rates.isEmpty()) {
                        throw incorrectDocument(this, doc);
                    }
                    
                    //Adding NaN rates if neeeded.
                    if(rates.size() != getSupportedBanks().size()) {
                        Set<CommercialBank> otherBanks = new HashSet<>(getSupportedBanks());
                        otherBanks.removeAll(rates.keySet());
                        for(CommercialBank b : otherBanks) {
                            rates.put(b, new ExchangeRate(b, currency, date, Float.NaN, Float.NaN));
                        }
                    }
		    return rates;
		}

	    };

    public static final InterbankSite<CommercialBank, ExchangeRate> LIGA_FINANCE
	    = new InterbankSite<CommercialBank, ExchangeRate>("LIGA_FINANCE", 1, "http://finance.liga.net/", Bank.values(CommercialBank.class)) {

                public static final String TEMPLATE_URL = 
                        "http://finance.liga.net/rates/nal/date_%s/%s";
                
		private final Map<String, CommercialBank> tags = new HashMap<>();

		{
		    tags.put("ПриватБанк", Bank.PRIVAT_BANK);
		}

		@Override
		public Map<CommercialBank, ExchangeRate> load(Currency currency, LocalDate date) throws IOException {
		    String url = String.format(TEMPLATE_URL, 
                            format("dd-MM-yyyy", date), currency);
                    Document doc = Jsoup.connect(url).get();
                    
                    checkRedirected(doc, url);
                    
		    Element valutaInfo = doc.getElementsByClass("valutaInfo").first();
		    if(valutaInfo == null) {
                        throw incorrectDocument(this, doc,
                                "not found element with class 'valutaInfo'");
                    }
                    
		    Elements tables = valutaInfo.getElementsByTag("table");
		    if(tables == null || tables.size() < 3) {
                        throw incorrectDocument(this, doc,
                                "not found 3-th element with tag 'table'");
                    }

		    Element tbody = tables.get(2).getElementsByTag("tbody").first();
		    if(tbody == null) {
                        throw incorrectDocument(this, doc,
                                "not found element with tag 'tbody'");
                    }

		    Map<CommercialBank, ExchangeRate> rates = new HashMap<>();
		    Elements trs = tbody.getElementsByTag("tr");
		    for(int i = 1; i < trs.size(); i++) {
			Elements data = trs.get(i).getElementsByTag("td");
			
			if(!data.get(0).text().isEmpty()) {
			    String tag = data.get(5).text().replace("\"", "");
			    
			    float buy = Float.parseFloat(data.get(1).text().replace(',', '.'));
			    float sale = Float.parseFloat(data.get(3).text().replace(',', '.'));
			    
			    CommercialBank bank = tags.get(tag);
			    if(bank != null) {
				rates.put(bank, new ExchangeRate(bank, currency, date, buy, sale));
			    }
			}
		    }
                    
                    if(rates.isEmpty()) {
                        throw incorrectDocument(this, doc);
                    }
                    //Adding NaN rates if neeeded.
                    if(rates.size() != getSupportedBanks().size()) {
                        Set<CommercialBank> otherBanks = new HashSet<>(getSupportedBanks());
                        otherBanks.removeAll(rates.keySet());
                        for(CommercialBank b : otherBanks) {
                            rates.put(b, new ExchangeRate(b, currency, date, Float.NaN, Float.NaN));
                        }
                    }
		    return rates;
		}

	    };

    private final String name;
    private final String mainUrl;

    Site(String name, String mainUrl) {
	this.name = Objects.requireNonNull(name, "name");
	this.mainUrl = Objects.requireNonNull(mainUrl, "mainUrl");
	addIntoValues(this);
    }

    public String name() {
	return name;
    }

    public String mainUrl() {
	return mainUrl;
    }

    @Override
    public final String toString() {
	return name();
    }

    @Override
    public boolean equals(Object o) {
        return o == this || (o instanceof Site 
                && ((Site) o).mainUrl().equals(mainUrl()));
    }
    
    public abstract Set<? extends Bank<?>> getSupportedBanks();

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.mainUrl);
        return hash;
    }

    static void addIntoValues(Site site) {
	Class c = site.getClass();
	do {
	    UnmodifiableSetWrapper values = VALUES.get(c);
	    values.add(site);
	} while((c = c.getSuperclass()) != Object.class);
    }

    public static Site of(String name) {
	for(Site site : values()) {
	    if(site.name().equals(name)) {
		return site;
	    }
	}

	throw new IllegalArgumentException(
		"Site with name '" + name + "' doe not exists.");
    }

    public static Set<? extends Site> values() {
	return values(Site.class);
    }

    public static <S extends Site> Set<S> values(Class<S> c) {
	return ((UnmodifiableSetWrapper<S>) VALUES.get(c)).unmodifiable();
    }

    public static <S extends Site> Set<Site> supported(Bank<?> bank) {
        return supported(Site.class, bank);
    }

    public static <S extends Site> Set<S> supported(Class<S> type, Bank<?> bank) {
        if(filteredMap == null) filteredMap = new ReferenceMap<>(HARD, SOFT);
	
        for(Entry<Filter, Set<? extends Site>> e : filteredMap.entrySet()) {
            if(e.getKey().is(type, bank)) {
                return (Set<S>) e.getValue();
            }
        }
        
        Filter filter = new Filter(type, bank);
        Set<S> set = new FilteredSet<>(values(type), 
                s -> s.getSupportedBanks().contains(bank));
        filteredMap.put(filter, set);
        return set;
    }
    
    private static DocumentParseException incorrectDocument(Site site, Document document) {
        return new DocumentParseException(site, document, "Incorrect document.");
    }
    
    private static DocumentParseException incorrectDocument(Site site, Document document, String message) {
        return new DocumentParseException(site, document, "Incorrect document: " + message + ".");
    }
    
    private static void checkRedirected(Document doc, String url) throws IOException {
        if(!doc.location().equals(url)) {
            LOG.log(Level.WARNING, "Connection is redirected: "
                    + "original url = '" + url + "; final url = " + doc.location() + ".");
            /*throw new ConnectException("Connection is redirected: "
                    + "original url = '" + url + "; final url = " + doc.location() + ".");*/
        }
    }

    private static final class Filter {
        
        private final Class<? extends Site> type;
        private final Bank<?> bank;
        
        private Filter(Class<? extends Site> type, Bank<?> bank) {
            this.bank = Objects.requireNonNull(bank, "bank");
            this.type = Objects.requireNonNull(type, "type");
        }

        public Class<? extends Site> getType() {
            return type;
        }

        public Bank<?> getBank() {
            return bank;
        }
        
        public boolean is(Class<?> type, Bank<?> bank) {
            return getType().equals(type) && getBank().equals(bank);
        }

        @Override
        public boolean equals(Object o) {
            return o == this || (o instanceof Filter && ((Filter) o).is(type, bank));
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + Objects.hashCode(this.type);
            hash = 79 * hash + Objects.hashCode(this.bank);
            return hash;
        }

        @Override
        public String toString() {
            return "Filter [" + "type: " + type + ", bank: " + bank + ']';
        }

    }

}
