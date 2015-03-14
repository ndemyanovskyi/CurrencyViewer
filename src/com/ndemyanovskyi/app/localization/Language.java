package com.ndemyanovskyi.app.localization;

import com.ndemyanovskyi.app.localization.binding.DisplayNameOwner;
import com.ndemyanovskyi.app.localization.binding.ResourceBindings;
import com.ndemyanovskyi.app.res.Resources;
import java.util.Objects;
import javafx.beans.property.ReadOnlyProperty;



public enum Language implements DisplayNameOwner {
    
    ENGLISH("EN"), 
    RUSSIAN("RU", ENGLISH), 
    UKRAINIAN("UA", RUSSIAN);
    
    private static final String RESOURCE_PREFIX = "language_name_";
    
    private final String tag;
    private final Language parent;
    private ReadOnlyProperty<String> displayNameProperty;
    
    private Language(String id) {
	this(id, null);
    }
    
    private Language(String tag, Language parent) {
	this.tag = Objects.requireNonNull(tag, "tag");
	this.parent = parent;
    }
    
    public String tag() {
	return tag;
    } 

    public Language parent() {
	return parent;
    }
   
    public static Language valueOfTag(String tag) {
	for(Language l : values()) {
	    if(l.tag().equalsIgnoreCase(tag)) {
		return l;
	    }
	}
	
	throw new IllegalArgumentException(
		"Language with tag '" + tag + "' not defined.");
    }
   
    public static Language valueOfDisplayName(String name) {
	for(Language l : values()) {
            for(Language l1 : values()) {
                if(l.getDisplayName(l1).equalsIgnoreCase(name)) {
                    return l;
                }
            }
	}
	
	throw new IllegalArgumentException(
		"Language with display name '" + name + "' not defined.");
    }
    
    public static Language getDefault() {
	return ENGLISH;
    }

    @Override
    public String getDisplayName(Language language) {
        return Resources.strings(language).get(RESOURCE_PREFIX + tag);
    }

    @Override
    public ReadOnlyProperty<String> displayNameProperty() {
        return displayNameProperty != null ? displayNameProperty 
                : (displayNameProperty = ResourceBindings.strings().get(RESOURCE_PREFIX + tag));
    }
    
}
