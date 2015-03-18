/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.app.localization.binding;

import com.ndemyanovskyi.app.Manifest;
import com.ndemyanovskyi.app.Manifest.Key;
import com.ndemyanovskyi.app.Settings;
import static com.ndemyanovskyi.app.localization.binding.ResourceBindings.strings;
import java.util.Objects;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;

/**
 *
 * @author Назарій
 */
public class StringExpressionParser {
    
    private static final StringExpression EMPTY = Bindings.concat("");

    public static final String DEFAULT_OPEN_ANCHOR = "::{";
    public static final String SETTINGS_OPEN_ANCHOR = ":s:{";
    public static final String MANIFEST_OPEN_ANCHOR = ":m:{";
    public static final String CLOSE_ANCHOR = "}";
    
    private static String[] openAnchors 
            = { DEFAULT_OPEN_ANCHOR, SETTINGS_OPEN_ANCHOR, MANIFEST_OPEN_ANCHOR };
    
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
        String currentOpenAnchor = null;
        int fromIndex = -1;
        for(String anchor : openAnchors) {
            fromIndex = text.indexOf(anchor);
            if(fromIndex >= 0) {
                currentOpenAnchor = anchor;
                break;
            }
        }
        if(fromIndex == -1) return null;
        
        StringExpression exp = EMPTY;
        while(fromIndex != -1) {
            int toIndex = text.indexOf(CLOSE_ANCHOR, fromIndex);
            if(toIndex == -1) toIndex = text.length();
            
            String subText = text.substring(lastToIndex + CLOSE_ANCHOR.length(), fromIndex);
            if(!subText.equals("")) exp = exp.concat(subText);
            
            Object value;
            String key = text.substring(fromIndex + currentOpenAnchor.length(), toIndex);
            switch(currentOpenAnchor) {
                case MANIFEST_OPEN_ANCHOR:
                    value = Manifest.getInstance().get(Key.values().get(key)); break;
                case SETTINGS_OPEN_ANCHOR:
                    value = Settings.getInstance().get(key).asString(); break;
                default: 
                    value = strings().get(key); break;
            }
            exp = exp.concat(value);
            
            lastToIndex = toIndex;

            for(String anchor : openAnchors) {
                fromIndex = text.indexOf(anchor, toIndex);
                if(fromIndex >= 0) {
                    currentOpenAnchor = anchor;
                    break;
                }
            }
        }
        if(lastToIndex + CLOSE_ANCHOR.length() < text.length()) {
            exp = exp.concat(text.substring(lastToIndex + CLOSE_ANCHOR.length()));
        }
        return exp;
    }

}
