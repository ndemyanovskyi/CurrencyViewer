/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ndemyanovskyi.backend;

import com.ndemyanovskyi.app.localization.Language;
import com.ndemyanovskyi.app.localization.binding.ResourceBindings;
import com.ndemyanovskyi.app.res.Resources;
import com.ndemyanovskyi.util.Unmodifiable;
import java.util.Set;
import javafx.beans.property.ReadOnlyProperty;


public enum Currency {
    
    AUD, AZN, GBP, BYR, DKK, USD, EUR, ISK, KZT, CAD, LTL, MDL, NOK, 
    PLN, RUB, SGD, XDR, TRY, TMT, HUF, UZS, CZK, SEK, CHF, CNY, JPY;
    
    public static final String RESOURCE_PERFIX = "currency_";
    
    private static final Set<Currency> DEFAULT_VALUES = 
	    Unmodifiable.set(USD, EUR, RUB, GBP, CHF);
    
    private final ReadOnlyProperty<String> displayName = 
            ResourceBindings.strings().get(RESOURCE_PERFIX + name());
    
    public static Set<Currency> defaultValues() {
	return DEFAULT_VALUES;
    }
    
    public String getDisplayName(Language language) {
	return Resources.strings(language).get(RESOURCE_PERFIX + name());
    }
    
    public String getDisplayName() {
	return displayNameProperty().getValue();
    }

    public ReadOnlyProperty<String> displayNameProperty() {
        return displayName;
    }
    
    public static Currency ofLocaleName(String name) {
	for(Language l : Language.values()) {
	    for(Currency c : values()) {
		if(c.getDisplayName(l).equals(name)) {
		    return c;
		}
	    }
	}
	
	throw new IllegalArgumentException(
		"Currency with locale name '" + name + "' not found.");
    }

}
