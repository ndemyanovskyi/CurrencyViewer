/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.anim;

import static com.ndemyanovskyi.ui.anim.AbstractAnimator.DEFAULT_DURATION;
import com.ndemyanovskyi.ui.anim.Animator.State;
import com.ndemyanovskyi.ui.anim.XYZAnimator.XYZState;
import java.util.Arrays;
import javafx.animation.Transition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 *
 * @author Назарій
 */
public abstract class XYZAnimator<T extends Transition, S extends XYZState<T, S>> extends AbstractAnimator<T, S> {
    
    public XYZAnimator(ObservableSet<Node> nodes) {
        this(DEFAULT_DURATION, nodes);
    } 
    
    public XYZAnimator(Node... nodes) {
        this(FXCollections.observableSet(nodes));
    } 
    
    public XYZAnimator(Duration duration, Node... nodes) {
        this(duration, FXCollections.observableSet(nodes));
    } 
    
    public XYZAnimator(Duration duration, ObservableSet<Node> nodes) {
        super(duration, nodes);
    } 

    public void play(double x, double y, double z) {
        S state = null;
        for(S o : getStates()) {
            if(o.is(x, y, z)) {
                state = o;
                break;
            }
        }
        if(state == null) {
            state = createState(x, y, z);
        }
        play(state);
    }
    
    protected abstract S createState(double x, double y, double z);

    public void playX(double x) {
        play(x, 1, 1);
    }

    public void playY(double y) {
        play(1, y, 1);
    }

    public void playZ(double z) {
        play(1, 1, z);
    }

    public void playXZ(double x, double z) {
        play(x, 1, z);
    }

    public void playYZ(double y, double z) {
        play(1, y, z);
    }

    public void playXYZ(double value) {
        play(value, value, value);
    }

    public void playXY(double x, double y) {
        play(x, y, 1);
    }
    
    public static abstract class XYZState<T extends Transition, S extends XYZState<T, S>> implements State<T>, Rationable<S>, Comparable<S> {

        private final double x, y, z;

        public XYZState(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public double getX() {
            return x;
        }

        public double getZ() {
            return z;
        }

        public double getY() {
            return y;
        }
        
        public boolean is(double x, double y, double z) {
            return getX() == x && getY() == y && getZ() == z;
        }
        
        public double ratio(double x, double y, double z, S last) {
            double[] ratios = { 
                Rationable.ratio(x, getX(), last.getX()), 
                Rationable.ratio(y, getY(), last.getY()), 
                Rationable.ratio(z, getZ(), last.getZ()) };
            System.out.println(getClass().getSimpleName() + ": " + Arrays.toString(ratios));
            double sum = 0;
            int count = 0;
            for(double ratio : ratios) {
                if(ratio < 1) {
                    sum += ratio;
                    count++;
                }
            }
            return sum > 0 ? sum / count : 1;
        }

        @Override
        public int compareTo(S other) {
            double res = (getX() - other.getX()) 
                    + (getX() - other.getX()) 
                    + (getX() - other.getX());
            return res > 0 ? 1 : res < 0 ? -1 : 0;
        }

        @Override
        public double ratio(S state, S last) {
            return ratio(state.getX(), state.getY(), state.getZ(), last);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 89 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
            hash = 89 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
            hash = 89 * hash + (int) (Double.doubleToLongBits(this.z) ^ (Double.doubleToLongBits(this.z) >>> 32));
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            if(o == this) return true;
            if(o == null) return false;
            if(!getClass().isInstance(o)) return false;
            
            XYZState<?, ?> other = (XYZState<?, ?>) o;
            return is(other.getX(), other.getY(), other.getZ());
        }

    }
}
