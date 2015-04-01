/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.pane;

import com.ndemyanovskyi.app.Application;
import com.ndemyanovskyi.app.localization.binding.ResourceBindings;
import com.ndemyanovskyi.reflection.Reflection;
import com.ndemyanovskyi.throwable.Exceptions;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;

public class InitializableStackPane extends StackPane implements Initializable {

    public InitializableStackPane() {
	Class<?> caller = Reflection.getCallerClass();
	init(caller.getResource(caller.getSimpleName() + ".fxml"));
    }

    public InitializableStackPane(String parent) {
	init(Reflection.getCallerClass().getResource(parent));
    }

    private void init(URL url) {
	FXMLLoader loader = new FXMLLoader(url);
	loader.setRoot(this);
	loader.setController(this);
        
	Exceptions.execute(() -> loader.load());
        ResourceBindings.register(this);
    }

    public Application getApplication() {
	return Application.getInstance();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {}

}
