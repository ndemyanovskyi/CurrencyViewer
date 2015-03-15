/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.pane.main.chart;

import com.ndemyanovskyi.backend.Rate;
import com.ndemyanovskyi.ui.pane.InitializableVBox;
import java.util.List;
import java.util.ListIterator;
import javafx.beans.InvalidationListener;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;

/**
 *
 * @author Назарій
 */
public class Description extends InitializableVBox {
    
    @FXML
    private GridPane content;
    
    private final ListProperty<Item<?>> data = 
            new SimpleListProperty<>(FXCollections.observableArrayList());
    
    public Description() {
        final InvalidationListener listListener = p -> updateContent();
        data.addListener((property, oldList, newList) -> {
            if(oldList != null) {
                oldList.removeListener(listListener);
            }
            if(newList != null) {
                newList.addListener(listListener);
            }
            updateContent();
        });
    }
    
    private void updateContent() {
        content.getChildren().clear();
        List<Item<?>> list = getData();
        if(list != null) {
            ListIterator<Item<?>> it = list.listIterator();
            while(it.hasNext()) {
                Item<?> each = it.next();
                if(each == null) continue;
                content.addRow(it.nextIndex(),
                        each.getLeftItem(), each.getRightItem());
            }
        }
    }
    
    public ListProperty<Item<?>> dataProperty() {
        return data;
    }

    public ObservableList<Item<?>> getData() {
        return dataProperty().get();
    }

    public void setData(ObservableList<Item<?>> data) {
        dataProperty().set(data);
    }
    
    public final static class Item<R extends Rate> {
        
        private final ObjectProperty<Intent<R>> intent = new SimpleObjectProperty<>();
        private final FloatProperty value = new SimpleFloatProperty(this, "value");
        
        private final LeftDecriptionItem<R> leftItem = new LeftDecriptionItem<>(intent);
        private final RightDecriptionItem rightItem = new RightDecriptionItem(value);
        
        public Item(Intent<R> intent) {
            this(intent, 0);
        }
        
        public Item(Intent<R> intent, float value) {
            setIntent(intent);
            setValue(value);
        }
        
        public ObjectProperty<Intent<R>> intentProperty() {
            return intent;
        }
        
        public FloatProperty valueProperty() {
            return value;
        }

        protected RightDecriptionItem getRightItem() {
            return rightItem;
        }

        protected LeftDecriptionItem<R> getLeftItem() {
            return leftItem;
        }
        
        public void setValue(float value) {
            valueProperty().set(value);
        }
        
        public void setIntent(Intent<R> intent) {
            intentProperty().set(intent);
        }

        public Intent<R> getIntent() {
            return intentProperty().get();
        }

        public Float getValue() {
            return valueProperty().get();
        }
        
    }
    
}
