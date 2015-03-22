/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.anim;

import com.ndemyanovskyi.ui.anim.Animator.Rationable;
import com.ndemyanovskyi.ui.anim.Animator.State;
import com.ndemyanovskyi.util.number.Numbers.Doubles;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 *
 * @author Назарій
 */
public class OpacityAnimator extends AbstractAnimator<FadeTransition, OpacityAnimator.Opacity> {
    
    public OpacityAnimator(Node... nodes) {
        this(DEFAULT_DURATION, nodes);
    }
    
    public OpacityAnimator(ObservableSet<Node> nodes) {
        this(DEFAULT_DURATION, nodes);
    }
    
    public OpacityAnimator(Duration duration, Node... nodes) {
        this(duration, FXCollections.observableSet(nodes));
    } 
    
    public OpacityAnimator(Duration duration, ObservableSet<Node> nodes) {
        super(duration, nodes);
    } 

    public void play(double value) {
        Opacity opacity = null;
        for(Opacity o : getStates()) {
            if(o.is(value)) {
                opacity = o;
                break;
            }
        }
        if(opacity == null) {
            opacity = new Opacity(value);
        }
        play(opacity);
    }
    
    public static final class Opacity implements State<FadeTransition>, Rationable<Opacity>, Comparable<Opacity> {

        private final double value;

        public Opacity(double value) {
            this.value = Doubles.requireInRange(value, 0, 1, "value");
        }

        public double getValue() {
            return value;
        }
        
        public boolean is(double value) {
            return getValue() == value;
        }
        
        @Override
        public boolean test(Node node) {
            return node.getOpacity() == getValue();
        }

        @Override
        public double ratio(Node node, Opacity last) {
            return ratio(node.getOpacity(), last);
        }

        @Override
        public double ratio(Opacity state, Opacity last) {
            return ratio(getValue(), last);
        }

        public double ratio(double opacity, Opacity last) {
            return Rationable.ratio(opacity, getValue(), last.getValue());
        }

        @Override
        public int compareTo(Opacity other) {
            return Double.compare(getValue(), other.getValue());
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 37 * hash + (int) (Double.doubleToLongBits(this.value) ^ 
                                     (Double.doubleToLongBits(this.value) >>> 32));
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            if(o == this) return true;
            if(o == null) return false;
            if(!(o instanceof Opacity)) return false;
            
            Opacity other = (Opacity) o;
            return getValue() == other.getValue();
        }

        @Override
        public FadeTransition init(Node node, FadeTransition transition, Duration duration) {
            if(transition == null) {
                transition = new FadeTransition();
            }
            
            if(!duration.isUnknown()) {
                transition.setDuration(duration);
            }
            transition.setFromValue(node.getOpacity());
            transition.setToValue(getValue());
            transition.setNode(node);
            
            return transition;
        }

    }
    
}
