/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.anim;

import com.ndemyanovskyi.util.Pair;
import com.ndemyanovskyi.util.Unmodifiable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 *
 * @author Назарій
 */
public abstract class AbstractAnimator<T extends Transition> extends Animator {
    
    protected static final Duration DEFAULT_DURATION = Duration.millis(400);
    
    private final Map<Node, Pair<T, T>> pairs = new HashMap<>();
    private final Duration duration;
    private Set<Node> nodeSet;
    
    public AbstractAnimator(Collection<? extends Node> nodes) {
        this(DEFAULT_DURATION, nodes);
    }  
    
    public AbstractAnimator(Duration duration, Collection<? extends Node> nodes) {  
        this.duration = Objects.requireNonNull(duration, "duration");
        nodes.forEach(n -> this.pairs.put(n, null));
    } 
    
    protected abstract T initUpTransition(Node node, T transition);
    protected abstract T initDownTransition(Node node, T transition);

    @Override
    public Set<Node> getNodes() {
        return nodeSet != null ? nodeSet : 
                (nodeSet = Unmodifiable.set(pairs.keySet()));
    }

    @Override
    public Duration getDuration() {
        return duration;
    }
    
    protected T getUpTransition(Node node) {
        Pair<T, T> pair = pairs.get(node);
        if(pair == null) {
            pair = new Pair<>(initUpTransition(node, null), null);
            pairs.put(node, pair);
        } 
        if(pair.getFirst()== null) {
            pair.setFirst(initUpTransition(node, null));
        }
        return pair.getFirst();
    }
    
    protected T getDownTransition(Node node) {
        Pair<T, T> pair = pairs.get(node);
        if(pair == null) {
            pair = new Pair<>();
            pairs.put(node, pair);
        } 
        if(pair.getSecond() == null) {
            pair.setSecond(initDownTransition(node, null));
        }
        return pair.getSecond();
    }

    @Override
    public void playUp() {
        for(Node node : getNodes()) {
            T up = getUpTransition(node);
            T down = getDownTransition(node);
            Duration offset = getUpOffset(node);
            if(up.getStatus() != Animation.Status.RUNNING && offset.lessThan(getDuration())) {
                down.pause();
                up = initUpTransition(node, up);
                pairs.get(node).setFirst(up);
                up.playFrom(offset);
            }
        }
    }

    @Override
    public void playDown() {
        for(Node node : getNodes()) {
            T up = getUpTransition(node);
            T down = getDownTransition(node);
            Duration offset = getDownOffset(node);
            if(down.getStatus() != Animation.Status.RUNNING && offset.lessThan(getDuration())) {
                up.pause();
                down = initDownTransition(node, down);
                pairs.get(node).setSecond(down);
                down.playFrom(offset);
            }
        }
    }
    
    protected abstract Duration getDownOffset(Node node);
    protected abstract Duration getUpOffset(Node node);
    
}
