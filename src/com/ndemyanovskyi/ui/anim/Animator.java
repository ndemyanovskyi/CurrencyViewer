/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.anim;

import java.util.function.Predicate;
import javafx.animation.Transition;
import javafx.beans.property.ReadOnlyBooleanProperty;
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
    
    public default boolean isPlaying() {
        return playingProperty().get();
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
    
    public ReadOnlyBooleanProperty playingProperty();
    public ReadOnlyObjectProperty<Duration> durationProperty();
    public ReadOnlySetProperty<Node> nodesProperty();
    public ReadOnlyObjectProperty<S> currentStateProperty();
    public ReadOnlyListProperty<S> statesProperty();
    
    public interface State<T extends Transition> extends Predicate<Node> {
        
        public default T init(Node node, T transition) {
            return init(node, transition, Duration.UNKNOWN);
        }
        
        public T init(Node node, T transition, Duration defaultDuration);
        
    }

    public interface Rationable<T> {

        public double ratio(T state, T last);
        public double ratio(Node node, T last);
        
        public static double ratio(double value, double first, double last) {
            if(Double.isNaN(first)) {
                throw new IllegalArgumentException("first == NaN");
            }
            if(Double.isNaN(last)) {
                throw new IllegalArgumentException("last == NaN");
            }
            if(first == last) {
                return value == first ? 1 : 0;
            }
            return (value - first) / (last - first);
        }

    }
    
}
