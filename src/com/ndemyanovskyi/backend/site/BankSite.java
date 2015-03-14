/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ndemyanovskyi.backend.site;

import com.ndemyanovskyi.util.Unmodifiable;
import com.ndemyanovskyi.backend.Bank;
import com.ndemyanovskyi.backend.Currency;
import com.ndemyanovskyi.backend.Rate;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


public abstract class BankSite<B extends Bank<R>, R extends Rate> extends Site {
    
    private B bank;
    private Set<B> banks;
    
    BankSite(String name, String mainUrl, B bank) {
	super(name, mainUrl);
	this.bank = Objects.requireNonNull(bank, "bank");
    }
    
    BankSite(String name, String mainUrl) {
	super(name, mainUrl);
    }
    
    public B getBank() {
	return bank != null ? bank : (bank = (B) Bank.of(name()));
    } 

    @Override
    public Set<B> getSupportedBanks() {
        return banks != null ? banks : (banks = Unmodifiable.set(bank));
    }
    
    /**
     * Loaded and parsed document from bank site.
     * @param date date for loading
     * @return map of rates, not null
     * @throws IOException general exception for any io error
     * @throws DocumentParseException if loaded document can`t be parsed correctly or parsed elements equals zero
     */
    public abstract Map<Currency, R> load(LocalDate date) throws IOException, DocumentParseException;
    
}
