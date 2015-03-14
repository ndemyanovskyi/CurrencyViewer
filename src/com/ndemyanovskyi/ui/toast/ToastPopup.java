/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ndemyanovskyi.ui.toast;

import com.ndemyanovskyi.app.localization.binding.ResourceBindings;
import javafx.geometry.Insets;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Popup;
import javafx.stage.Window;


class ToastPopup<T> extends Popup {
    
    private final BorderPane root = new BorderPane();
    private Toast<T> toast;

    public ToastPopup() {
	root.setPadding(new Insets(5));
	root.setStyle("-fx-background-color: rgb(55, 55, 55); -fx-background-radius: 10;");
	root.setEffect(new DropShadow());
	getContent().add(root);
	
	addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
	    if(e.getButton().equals(MouseButton.PRIMARY)) {
		if(e.getClickCount() % 2 == 0) {
		    if(!toast.isCancelled()) {
			toast.cancel();
		    }
		}
	    }
	});
    }

    public BorderPane getRoot() {
	return root;
    }

    @Override
    protected void show() {
	if(toast.isCancelled()) {
	    throw new IllegalStateException("Toast alredy cancelled.");
	}
        ResourceBindings.register(toast.getNode());
	super.show();
    }

    public void show(Toast<T> toast, Window owner) {
	setToast(toast);
	if(toast == null) {
	    throw new IllegalStateException(
		    "Toast can`t be null, if popup will be show.");
	}
	if(toast.getNode() == null) {
	    throw new IllegalStateException(
		    "Toast node can`t be null, if popup will be show.");
	}
        ResourceBindings.register(toast.getNode());
	show(owner);
    }

    public void setToast(Toast<T> toast) {
	this.toast = toast;
	root.setCenter(toast != null ? toast.getNode() : null);
    }

    public Toast<T> getToast() {
	return toast;
    }

}
