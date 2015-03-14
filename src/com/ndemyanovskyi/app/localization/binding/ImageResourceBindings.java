/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ndemyanovskyi.app.localization.binding;

import com.ndemyanovskyi.app.res.Resources;
import javafx.beans.property.Property;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ImageResourceBindings extends ResourceBindings<Image> {

    ImageResourceBindings() {
	super(l -> Resources.images(l));
    }
    
    public void bind(Property<? super Image> value, String resourceName) {
	value.bind(get(resourceName));
    }

    public void bind(ImageView imageView, String resourceName) {
	bind(imageView.imageProperty(), resourceName);
    }

}
