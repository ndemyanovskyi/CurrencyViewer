/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.anim;

import com.ndemyanovskyi.collection.set.ArrayListedSet;
import com.ndemyanovskyi.ui.anim.Animator.Rationable;
import com.ndemyanovskyi.ui.anim.Animator.State;
import com.ndemyanovskyi.util.Compare;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener.Change;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 *
 * @author Назарій
 */
public abstract class AbstractAnimator<T extends Transition, S extends State<T> & Rationable<S> & Comparable<S>> implements Animator<S> {
    
    protected static final Duration DEFAULT_DURATION = Duration.millis(400);
    
    private final Map<Node, T> transitions = new HashMap<>();
    
    private final ReadOnlyBooleanWrapper playing = new ReadOnlyBooleanWrapper(this, "playing", false);
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

    @Override
    public ReadOnlyBooleanProperty playingProperty() {
        return playing.getReadOnlyProperty();
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
    
    private EventHandler<ActionEvent> onTransitionFinished = e -> {
        boolean running = false;
        for(Transition t : transitions.values()) {
            if(t != null && t.getStatus() == Animation.Status.RUNNING) {
                running = true;
                break;
            }
        }
        if(!running) playing.set(false);
    };
    
    private S minState() {
        return !states.isEmpty() 
                ? Compare.min(states) : null;
    }
    
    private S maxState() {
        return !states.isEmpty() 
                ? Compare.max(states) : null;
    }
    
    private T getTransition(Node node, S state) {
        /*-S first = minState();
        S last = maxState();
        
        double ratio = first != null 
                ? first.ratio(node, last) : 1;
        ratio = ratio - ((int) ratio);
        if(ratio == 0) ratio = 1;*/
        
        T transition = state.init(
                node, 
                transitions.get(node), 
                getDuration());
        
        transitions.put(node, transition);
        transition.setOnFinished(onTransitionFinished);
        return transition;
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
            playing.set(true);
            for(Node node : getNodes()) {
                T transition = getTransition(node, state);
                transition.playFromStart();
            }
        }
    }

    @Override
    public void stop() {
        setCurrentState(null);
        playing.set(false);
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
