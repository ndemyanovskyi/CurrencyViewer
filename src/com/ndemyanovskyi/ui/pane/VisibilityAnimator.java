/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.pane;

import java.util.Objects;
import javafx.animation.FadeTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 *
 * @author Назарій
 */
public class VisibilityAnimator {
    
    private final Node node;
    private final FadeTransition showTransition;
    private final FadeTransition hideTransition;
    private ObjectProperty<EventHandler<? super ActionEvent>> onHidden;
    
    private volatile boolean shown;
    private volatile boolean showing;
    private volatile boolean hidden;
    private volatile boolean hiding;
    
    public VisibilityAnimator(Node node) {
        this(Duration.millis(400), node, null);
    }
    
    public VisibilityAnimator(Node node, EventHandler<ActionEvent> onHidden) {
        this(Duration.millis(400), node, onHidden);
    }
    
    public VisibilityAnimator(Duration duration, Node node) {
        this(duration, node, null);
    }
    
    public VisibilityAnimator(Duration duration, Node node, EventHandler<ActionEvent> onHidden) {
        this(duration, node, onHidden, 0.0, 1.0);
    }    
    
    public VisibilityAnimator(Duration duration, Node node, EventHandler<ActionEvent> onHidden, double from, double to) {
        if(from > to) {
            throw new IllegalArgumentException("from > to");
        }
        
        this.node = Objects.requireNonNull(node, "node");
        
        showTransition = new FadeTransition(duration, node);
        showTransition.setFromValue(from);
        showTransition.setToValue(to);
        showTransition.setOnFinished(e -> {
            showing = false;
            hidden = false;
            shown = true;
        });
        
        hideTransition = new FadeTransition(duration, node);
        hideTransition.setFromValue(to);
        hideTransition.setToValue(from);
        hideTransition.setOnFinished(e -> {
            showing = false;
            shown = false;
            hidden = true;
            
            EventHandler<? super ActionEvent> handler = getOnHidden();
            if(handler != null) {
                handler.handle(new ActionEvent(this, ActionEvent.NULL_SOURCE_TARGET));
            }
        });
        setOnHidden(onHidden);
    }

    public Node getNode() {
        return node;
    }

    public ObjectProperty<EventHandler<? super ActionEvent>> onHiddenProperty() {
        return onHidden != null ? onHidden 
                : (onHidden = new SimpleObjectProperty<>(this, "onHidden"));
    } 

    public final EventHandler<? super ActionEvent> getOnHidden() {
        return onHidden != null ? onHiddenProperty().get() : null;
    }    

    public final void setOnHidden(EventHandler<? super ActionEvent> handler) {
        onHiddenProperty().set(handler);
    } 
    
    public void show() {
        if(!showing && !shown && node.getOpacity() < 1.0) {
            node.setVisible(true);
            
            hiding = false;
            hidden = false;
            showing = true;

            hideTransition.pause();
            showTransition.playFrom(showTransition.
                    getDuration().multiply(node.getOpacity()));
        }
    }
    
    public void hide() {
        if(!hiding && !hidden && node.getOpacity() > 0.0) {
            hiding = true;
            shown = false;
            showing = false;

            showTransition.pause();
            hideTransition.playFrom(hideTransition.
                    getDuration().multiply(1 - node.getOpacity()));
        }
    }

    public boolean isShown() {
        return shown;
    }

    public boolean isShowing() {
        return showing;
    }

    public boolean isHiding() {
        return hiding;
    }

    public boolean isHidden() {
        return hidden;
    }
    
}
