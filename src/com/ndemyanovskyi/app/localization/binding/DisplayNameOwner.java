/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.app.localization.binding;

import com.ndemyanovskyi.app.localization.Language;
import javafx.beans.property.ReadOnlyProperty;

/**
 *
 * @author Назарій
 */
public interface DisplayNameOwner {
    
    public default String getDisplayName() {
        return displayNameProperty().getValue();
    }
    
    public String getDisplayName(Language language);
    public ReadOnlyProperty<String> displayNameProperty();
}
