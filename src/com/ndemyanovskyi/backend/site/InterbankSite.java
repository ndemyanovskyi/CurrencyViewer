/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ndemyanovskyi.backend.site;

import com.ndemyanovskyi.backend.Bank;
import com.ndemyanovskyi.backend.Currency;
import com.ndemyanovskyi.backend.Rate;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


public abstract class InterbankSite<B extends Bank<R>, R extends Rate>  extends Site {
    
    public static final Comparator<InterbankSite<?, ?>> PRIORITY_COMPARATOR = 
	    (a, b) -> a.getPriority() - b.getPriority();

    private final Set<B> banks;
    private final int priority;
    
    InterbankSite(String name, int priority, String mainUrl, Set<B> banks) {
	super(name, mainUrl);
	this.priority = priority;
	this.banks = Objects.requireNonNull(banks, "banks");
	addIntoValues(this);
    }
    
    @Override
    public Set<B> getSupportedBanks() {
	return banks;
    }

    public int getPriority() {
	return priority;
    }
    
    /**
     * Loaded and parsed document from interbank site.
     * @param currency currency for loading
     * @param date date for loading.
     * @return map of banks and rates, not null
     * @throws IOException general exception for any io error
     * @throws DocumentParseException if loaded document can`t be parsed correctly or parsed elements equals zero
     */
    public abstract Map<B, R> load(Currency currency, LocalDate date) throws IOException;
    
}
