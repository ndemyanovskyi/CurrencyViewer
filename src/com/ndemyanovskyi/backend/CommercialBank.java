/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ndemyanovskyi.backend;

import com.ndemyanovskyi.backend.Rate.Field;
import com.ndemyanovskyi.backend.site.BankSite;
import com.ndemyanovskyi.derby.Row;
import com.ndemyanovskyi.util.Unmodifiable;
import java.time.LocalDate;
import java.util.Set;


public class CommercialBank extends Bank<ExchangeRate> {
    
    static final DatabaseHelper<ExchangeRate> DATABASE_HELPER = new DatabaseHelper<ExchangeRate>() {
	
	@Override
	public ExchangeRate getRate(Bank<ExchangeRate> bank, Currency currency, Row row) {
            LocalDate date = row.get("DATE").toLocalDate();
            Float buy = row.get("BUY").toFloat();
            Float sale = row.get("SALE").toFloat();
            return new ExchangeRate(bank, currency, date, 
                    buy != null ? buy : Float.NaN, sale != null ? sale : Float.NaN);
	}

	@Override
	public String getLayout(Bank<ExchangeRate> bank, Currency currency) {
	    return "DATE DATE PRIMARY KEY, BUY FLOAT, SALE FLOAT";
	}

	@Override
	public String getInsertSql(String table, ExchangeRate rate) {
            String buy = !rate.getBuy().isNaN() ? rate.getBuy().toString() : "null";
            String sale = !rate.getSale().isNaN() ? rate.getSale().toString() : "null";
	    return String.format(
                    "INSERT INTO %s VALUES('%s', %s, %s)", 
		    table, rate.getDate(), buy, sale);
	}

        @Override
        public String getUpdateSql(String table, ExchangeRate rate) {
            String buy = !rate.getBuy().isNaN() ? rate.getBuy().toString() : "null";
            String sale = !rate.getSale().isNaN() ? rate.getSale().toString() : "null";
	    return String.format("UPDATE %s SET BUY=%s, SALE=%s WHERE DATE=DATE('%s')", 
		    table, buy, sale, rate.getDate());
        }
	
    };
    
    private static final Set<Field> FIELD_SET = Unmodifiable.set(Field.values());

    public CommercialBank(String tag, Set<Currency> currencySet) {
	this(tag, currencySet, null);
    }

    public CommercialBank(String tag) {
	this(tag, Currency.defaultValues(), null);
    }

    public CommercialBank(String tag, Set<Currency> currencySet, BankSite<? extends CommercialBank, ExchangeRate> site) {
	super(tag, currencySet, FIELD_SET, site, DATABASE_HELPER);
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
