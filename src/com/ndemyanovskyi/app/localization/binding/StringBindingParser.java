/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.app.localization.binding;

import static com.ndemyanovskyi.app.localization.binding.ResourceBindings.strings;
import java.util.Objects;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.ReadOnlyProperty;

/**
 *
 * @author Назарій
 */
class StringBindingParser {
    
    private static final StringExpression EMPTY = Bindings.concat("");

    public static final String ANCHOR = "::";
    
    public static void main(String[] args) {
        //System.out.println(parse("::from"));
        System.out.println("12345".substring(0, 5));
    }

    public static StringExpression parse(final String text) {
        Objects.requireNonNull(text, "text");

        int lastToIndex = 0;
        int fromIndex = text.indexOf(ANCHOR);
        if(fromIndex == -1) return null;
        
        StringExpression exp = EMPTY;
        while(fromIndex != -1) {
            int toIndex = getFisrtNotLetterIndex(fromIndex + 2, text);
            if(toIndex == -1) toIndex = text.length();
            
            String subText = text.substring(lastToIndex, fromIndex);
            if(!subText.equals("")) exp = exp.concat(subText);
            System.out.println(fromIndex + " " + toIndex);
            ReadOnlyProperty<String> resource = 
                    strings().get(text.substring(fromIndex + 2, toIndex));
            exp = exp.concat(resource);
            
            lastToIndex = toIndex;
            fromIndex = text.indexOf(ANCHOR);
        }
        if(lastToIndex < text.length()) {
            exp = exp.concat(text.substring(lastToIndex));
        }
        return exp;
    }
    
    private static int getFisrtNotLetterIndex(int fromInclusive, String text) {
        for(int i = fromInclusive; i < text.length(); i++) {
            if(!Character.isLetter(text.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

}
