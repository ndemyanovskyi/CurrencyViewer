/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ndemyanovskyi.backend;

import com.ndemyanovskyi.backend.Rate.Field;
import static com.ndemyanovskyi.backend.Rate.RATE;
import com.ndemyanovskyi.backend.site.BankSite;
import com.ndemyanovskyi.derby.Row;
import static com.ndemyanovskyi.throwable.Exceptions.ignore;
import com.ndemyanovskyi.util.Unmodifiable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;


public class NationalBank extends Bank<Rate> {
    
    static final DatabaseHelper<Rate> DATABASE_HELPER = new DatabaseHelper<Rate>() {
	
	@Override
	public Rate getRate(Bank<Rate> bank, Currency currency, Row row) {
            LocalDate date = row.get("DATE").toLocalDate();
            BigDecimal rate = ignore(
                    () -> row.get("RATE").toBigDecimal()).
                    orElse(BigDecimal.ZERO);
            return new Rate(bank, currency, date, rate);
	}

	@Override
	public String getLayout(Bank<Rate> bank, Currency currency) {
	    return "DATE DATE PRIMARY KEY, RATE VARCHAR(30)";
	}

	@Override
	public String getInsertSql(String table, Rate rate) {
	    return String.format("INSERT INTO %s (DATE, RATE) VALUES (DATE('%s'), '%s')", 
		    table, rate.getDate(), rate.getRate());
	}

        @Override
        public String getUpdateSql(String table, Rate rate) {
	    return String.format("UPDATE %s SET RATE='%s' WHERE DATE=DATE('%s')", 
		    table, rate.getRate(), rate.getDate());
        }
	
    };
    
    private static final Set<Field> FIELDS = Unmodifiable.set(RATE);

    public NationalBank(String tag, Set<Currency> currencySet, BankSite<? extends NationalBank, Rate> site) {
	super(tag, currencySet, FIELDS, site, DATABASE_HELPER);
    }

    public NationalBank(String tag, Currency[] currencys, BankSite<? extends NationalBank, Rate> site) {
	this(tag, Unmodifiable.set(currencys), site);
    }

    @Override
    public BankSite<? extends NationalBank, Rate> getSite() {
	return (BankSite) super.getSite();
    }

    @Override
    public Class<Rate> getRateType() {
        return Rate.class;
    }

}
