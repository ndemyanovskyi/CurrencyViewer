/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ndemyanovskyi.ui.pane.button;

import com.ndemyanovskyi.app.localization.binding.ResourceBindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


public class ImageButton extends Button {
    
    private final ObjectProperty<Image> pressedImage = new SimpleObjectProperty<>();
    private final ObjectProperty<Image> normalImage = new SimpleObjectProperty<>();
    
    private final ImageView imageView = new ImageView(); {
	pressedImage.addListener((a, b, c) -> { if(isPressed()) imageView.setImage(c); });
	normalImage.addListener((a, b, c) -> { if(!isPressed()) imageView.setImage(c); });
    }
    
    public ImageButton() {
	this(null);
    }
    
    public ImageButton(Image normal) {
	this(normal, normal);
    }
    
    public ImageButton(Image normal, Image pressed) {
	super("");
	setGraphic(imageView);
	pressedProperty().addListener((a, b, c) -> {
	    imageView.setImage(c ? pressedImage.get() : normalImage.get());
	});
	
	setStyle("-fx-background-radius: 1000000em; ");
    }
    
    public ImageButton(String normalResourceName, String pressedResourceName) {
	super("");
	setGraphic(imageView);
	ResourceBindings.images().bind(normalImage, normalResourceName);
	ResourceBindings.images().bind(pressedImage, pressedResourceName);
	
	pressedProperty().addListener((a, b, c) -> {
	    imageView.setImage(c ? pressedImage.get() : normalImage.get());
	});
	
	setStyle("-fx-background-radius: 1000000em; ");
    }

    public Image getPressedImage() {
	return pressedImage.get();
    }

    public void setPressedImage(Image image) {
	pressedImage.set(image);
    }

    public Image getNormalImage() {
	return normalImage.get();
    }

    public void setNormalImage(Image image) {
	normalImage.set(image);
    }

    public ObjectProperty<Image> pressedImageProperty() {
	return pressedImage;
    }

    public ObjectProperty<Image> normalImageProperty() {
	return normalImage;
    }

}
