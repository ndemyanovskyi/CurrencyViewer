package com.ndemyanovskyi.ui.toast;

import com.ndemyanovskyi.app.localization.binding.ResourceBindings;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;

class ToastController {

    private BorderPane root = new BorderPane();
    private Node node;

    public ToastController() {
	root.setPadding(new Insets(5));
	root.setStyle("-fx-background-color: rgb(55, 55, 55); -fx-background-radius: 10;");
	root.setEffect(new DropShadow());
    }

    public ToastController(Node node) {
	root.setPadding(new Insets(5));
	root.setStyle("-fx-background-color: rgb(55, 55, 55); -fx-background-radius: 10;");
	root.setEffect(new DropShadow());
	root.setCenter(node);
    }

    public BorderPane getRoot() {
	return root;
    }

    public void setNode(Node node) {
	root.setCenter(node);
	this.node = node;
        ResourceBindings.register(node);
    }

    public Node getNode() {
	return node;
    }

}
