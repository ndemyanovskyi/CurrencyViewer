/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ndemyanovskyi.app.localization.binding;

import com.ndemyanovskyi.app.localization.Language;
import com.ndemyanovskyi.app.res.Resources;
import java.util.function.Function;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Labeled;


public class ToStringResourceBindings<T> extends ResourceBindings<T> {

    ToStringResourceBindings(Function<Language, Resources<T>> resourcesFactory) {
	super(resourcesFactory);
    }

    public void bind(Labeled labeled, String resourceName) {
	bind(labeled.textProperty(), resourceName);
    }

    public void bind(Property<? super T> value, String resourceName) {
	value.bind(get(resourceName));
    }

    public void bind(StringProperty value, String resourceName) {
	value.bind(getWritableProperty(resourceName).asString());
    }

}
