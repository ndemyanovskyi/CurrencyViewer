/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.anim;

import java.util.Collection;
import java.util.Set;
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
public abstract class Animator {
    
    private Animator owner;
    
    private ObjectProperty<EventHandler<? super ActionEvent>> onDownFinished;
    private ObjectProperty<EventHandler<? super ActionEvent>> onUpFinished;
    
    public ObjectProperty<EventHandler<? super ActionEvent>> onDownFinishedProperty() {
        return onDownFinished != null ? onDownFinished : 
                (onDownFinished = new SimpleObjectProperty<>());
    }
    
    public ObjectProperty<EventHandler<? super ActionEvent>> onUpFinishedProperty() {
        Node d;
        return onUpFinished != null ? onUpFinished : 
                (onUpFinished = new SimpleObjectProperty<>());
    }

    public void setOnDownFinished(EventHandler<? super ActionEvent> onDownFinished) {
        onDownFinishedProperty().set(onDownFinished);
    }

    public EventHandler<? super ActionEvent> getOnDownFinished() {
        return onDownFinished != null ? onDownFinished.get() : null;
    }

    public void setOnUpFinished(EventHandler<? super ActionEvent> onUpFinished) {
        onUpFinishedProperty().set(onUpFinished);
    }

    public EventHandler<? super ActionEvent> getOnUpFinished() {
        return onUpFinished != null ? onUpFinished.get() : null;
    }
    
    public abstract void playUp();
    public abstract void playDown();
    
    public abstract Duration getDuration();
    public abstract Set<Node> getNodes();
    
    public static AnimatorGroup group(Animator... animators) {
        return new AnimatorGroup(animators);
    }
    
    public static AnimatorGroup group(Collection<Animator> animators) {
        return new AnimatorGroup(animators);
    }
    
}
