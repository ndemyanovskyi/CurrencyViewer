/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ndemyanovskyi.app.res;

import com.ndemyanovskyi.app.localization.Language;
import java.util.Arrays;
import java.util.Objects;


public class BooleanResources extends ValueResources<Boolean> {
    
    private final Language language;
    
    BooleanResources(Language language) {
	super(language, "booleans", Arrays.asList("boolean"));
	this.language = Objects.requireNonNull(language);
    }

    @Override
    protected Boolean convert(String nodeName, String text) {
	return Boolean.parseBoolean(text);
    }

    @Override
    public BooleanResources getParentResources() {
	Language parent = getLanguage().parent();
	return parent != null ? Resources.booleans(parent) : null;
    }

    @Override
    public BooleanResources getDefaultResources() {
	return Resources.booleans();
    }

}
