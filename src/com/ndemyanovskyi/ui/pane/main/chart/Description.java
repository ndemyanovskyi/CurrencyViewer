/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.pane.main.chart;

import com.ndemyanovskyi.ui.pane.AnimatedLabel;
import com.ndemyanovskyi.ui.pane.InitializableStackPane;
import com.ndemyanovskyi.util.Compare;
import com.ndemyanovskyi.util.DateTimeFormatters;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import javafx.scene.layout.Pane;

/**
 *
 * @author Назарій
 */
public class Description extends InitializableStackPane {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatters.of("dd.MM.yyyy");
    
    @FXML private AnimatedLabel dateLabel;
    @FXML private Pane backgroundPane;
    @FXML private GridPane content;
    
    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>(this, "date");
    private final ListProperty<Item> data = 
            new SimpleListProperty<>(this, "data", FXCollections.observableArrayList());
    
    public Description() {
        final InvalidationListener listListener = p -> updateContent();
        
        widthProperty().addListener((property, oldWidth, newWidth) -> {
            setPrefWidth(Compare.max(
                    oldWidth.doubleValue(), newWidth.doubleValue()));
        });
        data.addListener((property, oldList, newList) -> {
            if(oldList != null) {
                oldList.removeListener(listListener);
            }
            if(newList != null) {
                newList.addListener(listListener);
            }
            updateContent();
        });
        date.addListener(p-> {
            LocalDate localDate = getDate();
            dateLabel.setVisible(localDate != null);
            dateLabel.setText(localDate != null
                    ? FORMATTER.format(localDate) : "");
            dateLabel.layout();
        });
    } 

    public Pane getBackgroundPane() {
        return backgroundPane;
    }
   
    private void updateContent() {
        content.getChildren().clear();
        List<Item> list = getData();
        if(list != null) {
            ListIterator<Item> it = list.listIterator();
            while(it.hasNext()) {
                Item each = it.next();
                if(each == null) continue;
                content.addRow(it.nextIndex(),
                        each.getLeftItem(), each.getRightItem());
            }
        }
    }
    
    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }
    
    public ListProperty<Item> dataProperty() {
        return data;
    }

    public ObservableList<Item> getData() {
        return dataProperty().get();
    }

    public void setData(ObservableList<Item> data) {
        dataProperty().set(data);
    }

    public LocalDate getDate() {
        return dateProperty().get();
    }

    public void setDate(LocalDate date) {
        dateProperty().set(date);
    }
    
    public final static class Item {
        
        private final ObjectProperty<Intent<?>> intent = new SimpleObjectProperty<>();
        private final FloatProperty value = new SimpleFloatProperty(this, "value");
        
        private final LeftDecriptionItem leftItem = new LeftDecriptionItem(intent);
        private final RightDecriptionItem rightItem = new RightDecriptionItem(value);
        
        public Item(Intent<?> intent) {
            this(intent, 0);
        }
        
        public Item(Intent<?> intent, float value) {
            setIntent(intent);
            setValue(value);
        }
        
        public ObjectProperty<Intent<?>> intentProperty() {
            return intent;
        }
        
        public FloatProperty valueProperty() {
            return value;
        }

        protected RightDecriptionItem getRightItem() {
            return rightItem;
        }

        protected LeftDecriptionItem getLeftItem() {
            return leftItem;
        }
        
        public void setValue(float value) {
            valueProperty().set(value);
        }
        
        public void setIntent(Intent<?> intent) {
            intentProperty().set(intent);
        }

        public Intent<?> getIntent() {
            return intentProperty().get();
        }

        public Float getValue() {
            return valueProperty().get();
        }
        
    }
    
}
