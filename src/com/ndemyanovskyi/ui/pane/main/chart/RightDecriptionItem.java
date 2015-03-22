/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.pane.main.chart;

import com.ndemyanovskyi.ui.anim.OpacityAnimator;
import com.ndemyanovskyi.ui.pane.AnimatedLabel;
import com.ndemyanovskyi.ui.pane.InitializableStackPane;
import com.ndemyanovskyi.ui.pane.main.chart.Description.Item;
import com.ndemyanovskyi.util.Compare;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import javafx.animation.AnimationTimer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.util.Duration;

/**
 *
 * @author Назарій
 */
public class RightDecriptionItem extends InitializableStackPane {
    
    private static final Duration ANIM_DURATION = Duration.millis(250);
    private static final long NANOS_DURATION = TimeUnit.MILLISECONDS.toNanos((long) ANIM_DURATION.toMillis());
    
    private Item item;
    private ObjectProperty<BigDecimal> value;
    
    @FXML
    private AnimatedLabel valueLabel;
    
    private OpacityAnimator opacityAnimator = new OpacityAnimator(ANIM_DURATION, this);
    private HeightAnimation heightAnimation = new HeightAnimation();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        valueProperty().addListener(p -> {
            BigDecimal v = getValue();
            if(Compare.greater(v, BigDecimal.ZERO)) {
                opacityAnimator.play(1.0d);
                heightAnimation.play(50);
                valueLabel.setText(String.valueOf(v));
            } else {
                opacityAnimator.play(0d);
                heightAnimation.play(0);
                valueLabel.setText("");
            }
        });
    }
    
    public RightDecriptionItem(Item item, ObservableValue<BigDecimal> property) {
        this.item = Objects.requireNonNull(item, "item");
        valueProperty().bind(property);
        item.getLeftItem().prefHeightProperty().bind(prefHeightProperty());
        item.getLeftItem().maxHeightProperty().bind(maxHeightProperty());
        item.getLeftItem().minHeightProperty().bind(minHeightProperty());
        item.getLeftItem().opacityProperty().bind(opacityProperty());
        minHeightProperty().bind(prefHeightProperty());
        maxHeightProperty().bind(prefHeightProperty());
    }
    
    public void setValue(BigDecimal value) {
        valueProperty().set(value);
    }
    
    public BigDecimal getValue() {
        return valueProperty().get();
    }
    
    public ObjectProperty<BigDecimal> valueProperty() {
        return value != null ? value : 
                (value = new SimpleObjectProperty<>(this, "value"));
    }
    
    private class HeightAnimation {
        
        private double from =-1;
        private double to = -1;
        
        private AnimationTimer timer = new AnimationTimer() {

            private long start;

            @Override
            public void stop() {
                super.stop();
                start = 0;
            }

            @Override
            public void handle(long now) {
                if(start == 0) {
                    start = now;
                }
                
                double ratio = (double)(now - start) / NANOS_DURATION;
                System.out.println("ratio = " + ratio);
                if(ratio > 1) {
                    setPrefHeight(to);
                    stop();
                } else {
                    double offset = ratio * (to - from);
                    setPrefHeight(from + offset);
                }
            }
            
        };
        
        public void play(double to) {
            if(this.to != to) {
                timer.stop();
                this.from = getPrefHeight();
                this.to = to;
                timer.start();
            }
        }
        
        public void stop() {
            timer.stop();
        }
        
    }
    
}
