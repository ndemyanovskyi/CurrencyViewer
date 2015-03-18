/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.pane.main.chart;

import com.ndemyanovskyi.ui.pane.InitializableStackPane;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.value.ObservableValue;

/**
 *
 * @author Назарій
 */
public class RightDecriptionItem extends InitializableStackPane {
    
    private FloatProperty value;
    
    public RightDecriptionItem() {
    }
    
    public RightDecriptionItem(float value) {
        setValue(value);
    }
    
    public RightDecriptionItem(ObservableValue<? extends Number> property) {
        valueProperty().bind(property);
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
