/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.anim;

import java.util.function.Predicate;
import javafx.animation.Transition;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlySetProperty;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 *
 * @author Назарій
 */
public interface Animator<S extends Animator.State> {
    
    public void play(S state);
    public void stop();
    
    public default void play(int index) {
        play(getStates().get(index));
    }
    
    public default void replay() {
        S state = getCurrentState();
        if(state == null) {
            throw new IllegalStateException(
                    "Animator do not already played or stoped.");
        }
        play(state);
    }
    
    public default Duration getDuration() {
        return durationProperty().get();
    }
    
    public default ObservableSet<Node> getNodes() {
        return nodesProperty().get();
    }
    
    public default ObservableList<S> getStates() {
        return statesProperty().get();
    }
    
    public default S getCurrentState() {
        return currentStateProperty().get();
    }
    
    public ReadOnlyObjectProperty<Duration> durationProperty();
    public ReadOnlySetProperty<Node> nodesProperty();
    public ReadOnlyObjectProperty<S> currentStateProperty();
    public ReadOnlyListProperty<S> statesProperty();
    
    public interface State<T extends Transition> extends Predicate<Node> {
    
        public Duration getDuration();
        public T init(Node node, T transition);
        
    }
    
}
