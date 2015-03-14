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
public class StringExpressionParser {
    
    private static final StringExpression EMPTY = Bindings.concat("");

    public static final String OPEN_ANCHOR = "::{";
    public static final String CLOSE_ANCHOR = "}";

    /**
     * Converts source text to Resource dependence expression.
     * Resource dependence must be writes as {@code ::{some_resource}}.
     * 
     * @param text source string including resource dependencies
     * @return resource dependence expression, if source text including them, {@code null} - otherwise
     * @throws IllegalArgumentException when some parsed resource key is incorrect
     */
    public static StringExpression parse(final String text) {
        Objects.requireNonNull(text, "text");
        int lastToIndex = -1;
        int fromIndex = text.indexOf(OPEN_ANCHOR);
        if(fromIndex == -1) return null;
        
        StringExpression exp = EMPTY;
        while(fromIndex != -1) {
            int toIndex = text.indexOf(CLOSE_ANCHOR, fromIndex);
            if(toIndex == -1) toIndex = text.length();
            
            String subText = text.substring(lastToIndex + CLOSE_ANCHOR.length(), fromIndex);
            if(!subText.equals("")) exp = exp.concat(subText);
            
            ReadOnlyProperty<String> resource = 
                    strings().get(text.substring(fromIndex + OPEN_ANCHOR.length(), toIndex));
            exp = exp.concat(resource);
            
            lastToIndex = toIndex;
            fromIndex = text.indexOf(OPEN_ANCHOR, fromIndex + 1);
        }
        if(lastToIndex + CLOSE_ANCHOR.length() < text.length()) {
            exp = exp.concat(text.substring(lastToIndex + CLOSE_ANCHOR.length()));
        }
        return exp;
    }

}
