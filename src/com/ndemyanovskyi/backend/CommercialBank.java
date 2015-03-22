/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ndemyanovskyi.backend;

import static com.ndemyanovskyi.backend.ExchangeRate.BUY;
import static com.ndemyanovskyi.backend.ExchangeRate.SALE;
import com.ndemyanovskyi.backend.Rate.Field;
import static com.ndemyanovskyi.backend.Rate.RATE;
import com.ndemyanovskyi.backend.site.BankSite;
import com.ndemyanovskyi.derby.Row;
import static com.ndemyanovskyi.throwable.Exceptions.ignore;
import com.ndemyanovskyi.util.Unmodifiable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;


public class CommercialBank extends Bank<ExchangeRate> {
    
    static final DatabaseHelper<ExchangeRate> DATABASE_HELPER = new DatabaseHelper<ExchangeRate>() {
	
	@Override
	public ExchangeRate getRate(Bank<ExchangeRate> bank, Currency currency, Row row) {
            LocalDate date = row.get("DATE").toLocalDate();
            BigDecimal buy = ignore(() -> row.get("BUY").toBigDecimal()).
                    orElse(BigDecimal.ZERO);
            BigDecimal sale = ignore(() -> row.get("SALE").toBigDecimal()).
                    orElse(BigDecimal.ZERO);
            return new ExchangeRate(bank, currency, date, buy, sale);
	}

	@Override
	public String getLayout(Bank<ExchangeRate> bank, Currency currency) {
	    return "DATE DATE PRIMARY KEY, BUY VARCHAR(30), SALE VARCHAR(30)";
	}

	@Override
	public String getInsertSql(String table, ExchangeRate rate) {
	    String str = String.format(
                    "INSERT INTO %s (DATE, BUY, SALE) VALUES(DATE('%s'), '%s', '%s')", 
		    table, rate.getDate(), rate.getBuy(), rate.getSale());
            System.out.println(str);
            return str;
	}

        @Override
        public String getUpdateSql(String table, ExchangeRate rate) {
	    return String.format("UPDATE %s SET BUY='%s', SALE='%s' WHERE DATE=DATE('%s')", 
		    table, rate.getBuy(), rate.getSale(), rate.getDate());
        }
	
    };
    
    private static final Set<Field> FIELDS = Unmodifiable.set(BUY, SALE, RATE);

    public CommercialBank(String tag, Set<Currency> currencySet) {
	this(tag, currencySet, null);
    }

    public CommercialBank(String tag) {
	this(tag, Currency.defaultValues(), null);
    }

    public CommercialBank(String tag, Set<Currency> currencySet, BankSite<? extends CommercialBank, ExchangeRate> site) {
	super(tag, currencySet, FIELDS, site, DATABASE_HELPER);
    }

    public CommercialBank(String tag, BankSite<? extends CommercialBank, ExchangeRate> site) {
	this(tag, Currency.defaultValues(), site);
    }

    @Override
    public BankSite<? extends CommercialBank, ExchangeRate> getSite() {
	return (BankSite) super.getSite();
    }

    @Override
    public Class<ExchangeRate> getRateType() {
        return ExchangeRate.class;
    }

}
