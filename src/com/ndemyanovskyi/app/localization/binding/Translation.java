/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ndemyanovskyi.app.localization.binding;

import com.ndemyanovskyi.app.Settings;
import com.ndemyanovskyi.app.localization.Language;
import javafx.beans.property.ObjectProperty;


public final class Translation {
    
    private static final Translation INSTANCE = new Translation(Settings.LANGUAGE.get());
    
    private final BooleanResourceBindings booleanBinds = new BooleanResourceBindings();
    private final StringResourceBindings stringBinds = new StringResourceBindings();
    private final NumberResourceBindings numberBinds = new NumberResourceBindings();
    private final ImageResourceBindings imageBinds = new ImageResourceBindings();

    Translation(Language language) {
	setLanguage(language);
    }

    public StringResourceBindings getStringBindings() {
	return stringBinds;
    }

    public NumberResourceBindings getNumberBindings() {
	return numberBinds;
    }

    public ImageResourceBindings getImageBindings() {
	return imageBinds;
    }

    public BooleanResourceBindings getBooleanBindings() {
	return booleanBinds;
    }

    public Language getLanguage() {
	return languageProperty().get();
    }

    public void setLanguage(Language language) {
	languageProperty().set(language);
    }
    
    public ObjectProperty<Language> languageProperty() {
	return Settings.LANGUAGE;
    }

    public static Translation getInstance() {
	return INSTANCE;
    }

}
