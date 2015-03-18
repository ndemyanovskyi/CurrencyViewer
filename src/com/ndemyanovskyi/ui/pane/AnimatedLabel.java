/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.pane;

import java.util.HashMap;
import java.util.Map;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.util.Duration;

/**
 *
 * @author Назарій
 */
public class AnimatedLabel extends StackPane {
    
    private Map<Label, Boolean> labels = new HashMap<>();
    
    private final DoubleProperty updateRate = new SimpleDoubleProperty(this, "rate", 0);
    private final StringProperty text = new SimpleStringProperty(this, "text", "");
    private final ObjectProperty<Font> font = new SimpleObjectProperty<>(this, "font", Font.getDefault());
    private final ObjectProperty<Paint> textFill = new SimpleObjectProperty<>(this, "textFill", Color.BLACK);
    
    private final EventHandler<ActionEvent> updater = event -> {
        Label label = null;
        for(Map.Entry<Label, Boolean> e : labels.entrySet()) {
            if(!e.getValue()) {
                label = e.getKey();
            }
        }
        if(label == null) {
            label = new Label();
            label.setFont(getFont());
            label.setTextFill(getTextFill());
        }
        labels.put(label, true);
        label.setText(getText());

        for(Node child : getChildren()) {
            FadeTransition ft = new FadeTransition(Duration.millis(getUpdateRate()), child);
            ft.setFromValue(1);
            ft.setToValue(0);
            ft.setOnFinished(e -> {
                getChildren().remove(child);
                labels.put((Label) child, false);
            });
            ft.play();
        }

        FadeTransition ft = new FadeTransition(Duration.millis(getUpdateRate() / 2), label);
        label.setOpacity(0);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
        getChildren().add(label);
    };
    
    private final Timeline timeline = new Timeline();

    public AnimatedLabel() {
        setAlignment(Pos.BASELINE_LEFT);
        this.text.addListener(p -> timeline.play());
        this.font.addListener(p -> {
            for(Label l : labels.keySet()) {
                l.setFont(getFont());
            }
        });
        this.textFill.addListener(p -> {
            for(Label l : labels.keySet()) {
                l.setTextFill(getTextFill());
            }
        });
        this.updateRate.addListener((p, oldRate, newRate) -> {
            if(oldRate.doubleValue() != newRate.doubleValue()) {
                timeline.stop();
                timeline.getKeyFrames().clear();
                timeline.getKeyFrames().add(new KeyFrame(
                        Duration.millis(newRate.doubleValue()), updater));
                timeline.play();
            }
        });
        this.updateRate.set(150);
    }

    public AnimatedLabel(String text) {
        this();
        setText(text);
    }

    public AnimatedLabel(String text, double updateRate) {
        this();
        setText(text);
    }
    
    public StringProperty textProperty() {
        return text;
    }
    
    public String getText() {
        return text.get();
    }
    
    public void setText(String text) {
        this.text.set(text);
    }
    
    public ObjectProperty<Font> fontProperty() {
        return font;
    }
    
    public Font getFont() {
        return font.get();
    }
    
    public void setFont(Font font) {
        this.font.set(font);
    }
    
    public ObjectProperty<Paint> textFillProperty() {
        return textFill;
    }
    
    public Paint getTextFill() {
        return textFill.get();
    }
    
    public void setTextFill(Paint textFill) {
        this.textFill.set(textFill);
    }
    
    public DoubleProperty updateRateProperty() {
        return updateRate;
    }
    
    public double getUpdateRate() {
        return updateRate.get();
    }
    
    public void setUpdateRate(double updateRate) {
        this.updateRate.set(updateRate);
    }
    
}
