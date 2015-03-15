/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.pane.main.chart;

import com.ndemyanovskyi.backend.Rate;
import com.ndemyanovskyi.ui.pane.InitializableVBox;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Line;

/**
 *
 * @author Назарій
 */
public class LeftDecriptionItem<R extends Rate> extends InitializableVBox {
    
    @FXML private Label labelBank;
    @FXML private HBox bottomBox;
    @FXML private Line colorLine;
    
    private ObjectProperty<Intent<R>> intent;
    
    public LeftDecriptionItem() {}
    
    public LeftDecriptionItem(Intent<R> intent) {
        setIntent(intent);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colorLine.endXProperty().bind(
                Bindings.max(labelBank.widthProperty(), bottomBox.widthProperty()));
    }
    
    public LeftDecriptionItem(ObservableValue<Intent<R>> intent) {
        intentProperty().bind(intent);
    }
    
    public Intent<R> getIntent() {
        return intentProperty().get();
    }

    private void setIntent(Intent<R> intent) {
        intentProperty().set(intent);
    }
    
    public ObjectProperty<Intent<R>> intentProperty() {
        return intent != null ? intent : 
                (intent = new SimpleObjectProperty<>(this, "intent"));
    }
    
}
