/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.anim;

import java.util.Arrays;
import java.util.Collection;
import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 *
 * @author Назарій
 */
public class FadeAnimator extends AbstractAnimator<FadeTransition> {
    
    private final double downValue, upValue;
    
    public FadeAnimator(Node... nodes) {
        this(DEFAULT_DURATION, nodes);
    }
    
    public FadeAnimator(Collection<Node> nodes) {
        this(DEFAULT_DURATION, nodes);
    }
    
    public FadeAnimator(Duration duration, Collection<Node> nodes) {
        this(duration, 0.0, 1.0, nodes);
    } 
    
    public FadeAnimator(Duration duration, Node... nodes) {
        this(duration, 0.0, 1.0, nodes);
    } 
    
    public FadeAnimator(double down, double up, Node... nodes) {
        this(DEFAULT_DURATION, down, up, nodes);
    } 
    
    public FadeAnimator(double down, double up, Collection<Node> nodes) {
        this(DEFAULT_DURATION, down, up, nodes);
    } 
    
    public FadeAnimator(Duration duration, double down, double up, Node... nodes) {
        this(duration, Arrays.asList(nodes));
    } 
    
    public FadeAnimator(Duration duration, double down, double up, Collection<Node> nodes) {
        super(duration, nodes);
        this.downValue = down;
        this.upValue = up;
    } 
    
    public double getDownValue() {
        return downValue;
    }
    
    public double getUpValue() {
        return upValue;
    }

    @Override
    protected FadeTransition initUpTransition(Node node, FadeTransition transition) {
        if(transition == null) {
            transition = new FadeTransition();
        }
        transition.setFromValue(downValue);
        transition.setToValue(upValue);
        transition.setDuration(getDuration());
        transition.setNode(node);
        return transition;
    }

    @Override
    protected FadeTransition initDownTransition(Node node, FadeTransition transition) {
        if(transition == null) {
            transition = new FadeTransition();
        }
        transition.setFromValue(upValue);
        transition.setToValue(downValue);
        transition.setDuration(getDuration());
        transition.setNode(node);
        return transition;
    }

    @Override
    protected Duration getUpOffset(Node node) {
        return getDuration().multiply(node.getOpacity());
    }

    @Override
    protected Duration getDownOffset(Node node) {
        return getDuration().multiply(1 - node.getOpacity());
    }
    
}
