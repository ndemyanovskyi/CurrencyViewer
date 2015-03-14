/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.pane.main.chart;

import com.ndemyanovskyi.backend.Rate;
import com.ndemyanovskyi.ui.pane.InitializableVBox;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;

/**
 *
 * @author Назарій
 */
public class LeftDecriptionItem<R extends Rate> extends InitializableVBox {
    
    private ObjectProperty<Intent<R>> intent;
    
    public LeftDecriptionItem() {}
    
    public LeftDecriptionItem(Intent<R> intent) {
        setIntent(intent);
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
