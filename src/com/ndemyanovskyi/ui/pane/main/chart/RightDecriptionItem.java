/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.pane.main.chart;

import com.ndemyanovskyi.ui.pane.InitializableStackPane;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 *
 * @author Назарій
 */
public class RightDecriptionItem extends InitializableStackPane {
    
   // @FXML private Label valueLabel;
    private Map<Label, Boolean> labels = new HashMap<>();
    //private Label label = new Label();
    
    private final Timeline t = new Timeline(new KeyFrame(Duration.millis(150), event -> {
        Label label = null;
        for(Entry<Label, Boolean> e : labels.entrySet()) {
            if(!e.getValue()) {
                label = e.getKey();
            }
        }
        if(label == null) label = new Label();
        labels.put(label, true);
        label.setTextFill(Color.WHITE);
        label.setText(String.format("%.4f", getValue()));

        for(Node child : getChildren()) {
            FadeTransition ft = new FadeTransition(Duration.millis(150), child);
            ft.setFromValue(1);
            ft.setToValue(0);
            ft.setOnFinished(e -> {
                getChildren().remove(child);
                labels.put((Label) child, false);
            });
            ft.play();
        }

        FadeTransition ft = new FadeTransition(Duration.millis(75), label);
        ft.setFromValue(0);
        ft.setToValue(1);
        getChildren().add(label);
        ft.play();
        //label.setText(String.valueOf(round(getValue(), 3)));
    }));
    
    private FloatProperty value;
    
    public RightDecriptionItem() {
    }
    
    public RightDecriptionItem(float value) {
        setValue(value);
    }
    
    public RightDecriptionItem(ObservableValue<? extends Number> property) {
        valueProperty().bind(property);
        /*label.setTextFill(Color.WHITE);
        getChildren().add(label);*/
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        valueProperty().addListener((p, old, current) -> {
            if(!Objects.equals(old, current)) {
                t.play();
            }
        });
    }
    
    public void setValue(float value) {
        valueProperty().set(value);
    }
    
    public float getValue() {
        return valueProperty().get();
    }
    
    public FloatProperty valueProperty() {
        return value != null ? value : 
                (value = new SimpleFloatProperty(this, "value"));
    }
    
}
