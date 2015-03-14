/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ndemyanovskyi.app.res;

import com.ndemyanovskyi.app.localization.Language;
import java.util.Arrays;

public final class StringResources extends ValueResources<String> {
    
    StringResources(Language language) {
	super(language, "strings", Arrays.asList("string"));
    }

    @Override
    protected String convert(String nodeName, String text) {
	return text;
    }

    @Override
    public StringResources getParentResources() {
	Language parent = getLanguage().parent();
	return parent != null ? Resources.strings(parent) : null;
    }

    @Override
    public StringResources getDefaultResources() {
	return Resources.strings();
    }
    
}

