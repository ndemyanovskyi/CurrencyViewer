/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ndemyanovskyi.backend;

import com.ndemyanovskyi.app.localization.Language;
import com.ndemyanovskyi.app.localization.binding.ResourceBindings;
import com.ndemyanovskyi.app.res.Resources;
import com.ndemyanovskyi.backend.Rate.Field;
import com.ndemyanovskyi.backend.site.BankSite;
import com.ndemyanovskyi.backend.site.Site;
import com.ndemyanovskyi.collection.Collections;
import com.ndemyanovskyi.collection.set.unmodifiable.UnmodifiableSetWrapper;
import com.ndemyanovskyi.derby.Row;
import com.ndemyanovskyi.map.HashPool;
import com.ndemyanovskyi.map.Pool;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import javafx.beans.property.ReadOnlyProperty;


public abstract class Bank<R extends Rate> {
    
    public static final String RESOURCE_PREFIX = "bank_";
    
    private static final Pool<Class<? extends Bank>, UnmodifiableSetWrapper<? extends Bank>> VALUES = 
	    new HashPool<>(o -> {
		if(!(o instanceof Class)) {
		    throw new IllegalArgumentException("Key must be instance of Class<? extends Bank>.");
		}
		if(!Bank.class.isAssignableFrom((Class) o)) {
		    throw new IllegalArgumentException("Key must be assignable from Bank.");
		}
		return new UnmodifiableSetWrapper<>(new TreeSet<>((a, b) -> a.getName().compareTo(b.getName())));
	    });
    
    public static final NationalBank NBU = new NationalBank("NBU", Currency.values(), Site.NBU);
    
    public static final CommercialBank RAIFFEISEN_BANK_AVAL = new CommercialBank("RAIFFEISEN_BANK_AVAL");
    public static final CommercialBank FINANCE_AND_CREDIT = new CommercialBank("FINANCE_AND_CREDIT");
    public static final CommercialBank UKRSYB_BANK = new CommercialBank("UKRSYB_BANK");
    public static final CommercialBank UKRGAZ_BANK = new CommercialBank("UKRGAZ_BANK");
    public static final CommercialBank OSCHAD_BANK = new CommercialBank("OSCHAD_BANK");
    public static final CommercialBank SBER_BANK = new CommercialBank("SBER_BANK");
    public static final CommercialBank FIDOBANK = new CommercialBank("FIDOBANK");
    public static final CommercialBank VTB_BANK = new CommercialBank("VTB_BANK");
    
    //Without CHF    
    public static final CommercialBank PUMB = new CommercialBank("PUMB");
    public static final CommercialBank PRIVAT_BANK = new CommercialBank("PRIVAT_BANK");
    
    private final String name;
    private final Set<Currency> currencies;
    private final Set<Field> fieldSet;
    private final BankSite<? extends Bank, R> site;
    private final DatabaseHelper<R> databaseHelper;
    
    private final ReadOnlyProperty<String> displayNameProperty;
    
    public ReadOnlyProperty<String> displayNameProperty() {
        return displayNameProperty;
    }
    
    public String getDisplayName() {
	return displayNameProperty().getValue();
    }
    
    public String getDisplayName(Language language) {
        return Resources.strings(language).get(RESOURCE_PREFIX + getName());
    }
    
    Bank(String tag, Set<Currency> currencySet, Set<Field> fieldSet, BankSite<? extends Bank, R> site, DatabaseHelper<R> databaseHelper) {
	this.name = Objects.requireNonNull(tag, "tag");
	this.site = site;
	this.currencies = Collections.requireNonEmpty(
		Objects.requireNonNull(currencySet, "currencySet"), "currencySet is empty");
	this.fieldSet = Collections.requireNonEmpty(
		Objects.requireNonNull(fieldSet, "fieldSet"));
	this.databaseHelper = Objects.requireNonNull(databaseHelper, "databaseHelper");
        this.displayNameProperty = ResourceBindings.strings().get(RESOURCE_PREFIX + name);
        
	if(getByName(tag) != null) {
	    throw new IllegalArgumentException(
		    "Bank with tag '" + tag + "' alredy exists.");
	}
	
	addIntoValues(this);
    }
    
    Bank(String tag, Set<Currency> currencySet, Set<Field> fieldSet, DatabaseHelper<R> databaseHelper) {
	this(tag, currencySet, fieldSet, null, databaseHelper);
    }
    
    public abstract Class<R> getRateType();
    
    @SuppressWarnings("unchecked")
    private static void addIntoValues(Bank bank) {
	Class c = bank.getClass();
	do {
	    UnmodifiableSetWrapper values = VALUES.get(c);
	    values.add(bank);
	} while((c = c.getSuperclass()) != Object.class);
    }
    
    public R getTodayRate(Currency currency) {
	return getRate(currency, LocalDate.now());
    }
    
    public R getRate(Currency currency, LocalDate date) {
	RateList<R> list = getRateList(currency);
        int index = list.getPeriod().indexOf(date);
        if(index == -1) {
            throw new IllegalArgumentException(
                    "Date '" + date + "' out of period [" + list.getPeriod() + "].");
        }
        return list.get(index);
    }
    
    public RateList<R> getRateList(Currency currency) {
	return DataManager.getRateList(this, currency);
    }
    
    public BankSite<? extends Bank, R> getSite() {
	return site;
    }

    DatabaseHelper<R> getDatabaseHelper() {
	return databaseHelper;
    }

    public Set<Field> getFields() {
        return fieldSet;
    }

    public Set<Currency> getCurrencies() {
	return currencies;
    }

    public String getName() {
	return name;
    }
    
    private static Bank<?> getByName(String name) {
	for(Bank<?> bank : values()) {
	    if(bank.getName().equals(name)) {
		return bank;
	    }
	}
	return null;
    }
    
    public static Bank<?> of(String name) {
	Bank<?> bank = getByName(name);
	if(bank == null) {
	    throw new IllegalArgumentException(
		    "Bank with tag '" + name + "' does not exists.");
	}
	return bank;
    }
    
    public static Bank<?> ofDisplayName(String name) {
	for(Bank<?> bank : values()) {
	    for(Language language : Language.values()) {
		if(bank.getDisplayName(language).equals(name)) {
		    return bank;
		}
	    }
	}
	
	throw new IllegalArgumentException(
		"Bank with display name '" + name + "' does not exists.");
    }
    
    public static Set<Bank<?>> values() {
	return ((UnmodifiableSetWrapper) VALUES.get(Bank.class)).unmodifiable();
    }
    
    @SuppressWarnings("unchecked")
    public static <B extends Bank<?>> Set<B> values(Class<B> c) {
	return ((UnmodifiableSetWrapper<B>) VALUES.get(c)).unmodifiable();
    }

    @Override
    public final String toString() {
	return getName();
    }
    
    interface DatabaseHelper<R extends Rate> {
	
        public R getRate(Bank<R> bank, Currency currency, Row row);
	public String getLayout(Bank<R> bank, Currency currency);
	public String getInsertSql(String table, R rate);
	public String getUpdateSql(String table, R rate);
	
    }

}
