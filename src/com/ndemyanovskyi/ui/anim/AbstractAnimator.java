/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.anim;

import com.ndemyanovskyi.collection.set.ArrayListedSet;
import com.ndemyanovskyi.ui.anim.Animator.State;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.animation.Transition;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener.Change;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 *
 * @author Назарій
 */
public abstract class AbstractAnimator<T extends Transition, S extends State> implements Animator<S> {
    
    protected static final Duration DEFAULT_DURATION = Duration.millis(400);
    
    private final Map<Node, T> transitions = new HashMap<>();
    
    private final ListProperty<S> states = new SimpleListProperty<>(this, "states", FXCollections.observableList(new ArrayListedSet<>()));
    private final ObjectProperty<Duration> duration = new SimpleObjectProperty<>(this, "duration", DEFAULT_DURATION);
    private final ReadOnlyObjectWrapper<S> currentState = new ReadOnlyObjectWrapper<>(this, "currentState");
    private final SetProperty<Node> nodes;
    
    public AbstractAnimator(ObservableSet<Node> nodes) {
        this(DEFAULT_DURATION, nodes);
    }  
    
    public AbstractAnimator(Duration duration, ObservableSet<Node> nodes) {  
        this.nodes = new SimpleSetProperty<>(this, "nodes", nodes);
        
        this.nodes.addListener((Change<? extends Node> c) -> stop());
        this.nodes.addListener((property, oldSet, newSet) -> {
            if(oldSet != null) {
                oldSet.forEach(transitions::remove);
            }
        });
        
        setDuration(duration);
        this.duration.addListener(p -> stop());
    } 
    
    public final void setDuration(Duration duration) {
        durationProperty().set(duration);
    }
    
    public final void setNodes(ObservableSet<Node> nodes) {
        nodesProperty().set(nodes);
    }

    @Override
    public final ObjectProperty<Duration> durationProperty() {
        return duration;
    }

    @Override
    public final SetProperty<Node> nodesProperty() {
        return nodes;
    }
    
    protected abstract T initTransition(Node node, T transition, S state);
    
    private T getTransition(Node node, S state) {
        T transition = transitions.get(node);
        if(transition == null) {
            transition = initTransition(node, null, state);
            transitions.put(node, transition);
            return transition;
        } else {
            transition.pause();
            return initTransition(node, transition, state);
        }
    }

    @Override
    public void play(S state) {
        Objects.requireNonNull(state, "state");
        if(!state.equals(getCurrentState())) {
            List<S> states = getStates();
            if(!states.contains(state)) {
                states.add(state);
            }
            setCurrentState(state);
            for(Node node : getNodes()) {
                T transition = getTransition(node, state);
                transition.playFromStart();
            }
        }
    }

    @Override
    public void stop() {
        setCurrentState(null);
        for(Node node : getNodes()) {
            T transition = transitions.get(node);
            if(transition != null) transition.stop();
        }
    }
    
    private void setCurrentState(S state) {
        currentState.set(state);
    }

    @Override
    public final ReadOnlyObjectProperty<S> currentStateProperty() {
        return currentState.getReadOnlyProperty();
    }

    @Override
    public final ListProperty<S> statesProperty() {
        return states;
    }
    
}
