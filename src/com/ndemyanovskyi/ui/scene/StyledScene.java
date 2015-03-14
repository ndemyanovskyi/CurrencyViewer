/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ndemyanovskyi.ui.scene;

import javafx.beans.value.ChangeListener;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;


public class StyledScene extends Scene {
    
    private static final ChangeListener<Window> WINDOW_CHECKER = (a, b, c) -> {
	if(c != null && !(c instanceof Stage)) {
	    throw new IllegalArgumentException(
		    "StyledScene must be only attach to Stage.");
	}
    };

    public StyledScene(Parent content) {
	this(content, null);
    }

    public StyledScene(Parent content, Parent actionBar) {
	super(new StyledPane(content, actionBar));
	setFill(Color.TRANSPARENT);
	windowProperty().addListener(WINDOW_CHECKER);
    }

}
