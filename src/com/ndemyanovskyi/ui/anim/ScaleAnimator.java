/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.anim;

import static com.ndemyanovskyi.ui.anim.AbstractAnimator.DEFAULT_DURATION;
import java.util.Objects;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 *
 * @author Назарій
 */
public class ScaleAnimator extends AbstractAnimator<ScaleTransition, ScaleAnimator.Scale> {
    
    public ScaleAnimator(ObservableSet<Node> nodes) {
        this(DEFAULT_DURATION, nodes);
    } 
    
    public ScaleAnimator(Node... nodes) {
        this(FXCollections.observableSet(nodes));
    } 
    
    public ScaleAnimator(Duration duration, Node... nodes) {
        this(duration, FXCollections.observableSet(nodes));
    } 
    
    public ScaleAnimator(Duration duration, ObservableSet<Node> nodes) {
        super(duration, nodes);
    } 

    @Override
    protected ScaleTransition initTransition(Node node, ScaleTransition transition, Scale state) {
        transition = state.init(node, transition);
        if(transition.getDuration().isUnknown()) {
            transition.setDuration(getDuration());
        }
        return transition;
    }

    public void play(double x, double y, double z) {
        Scale scale = null;
        for(Scale o : getStates()) {
            if(o.is(x, y, z)) {
                scale = o;
                break;
            }
        }
        if(scale == null) {
            scale = new Scale(x, y, z);
        }
        play(scale);
    }

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
    
    public static final class Scale implements Animator.State<ScaleTransition> {

        private final double x, y, z;
        private final Duration duration;

        public Scale(double x, double y, double z) {
            this(Duration.UNKNOWN, x, y, z);
        }

        public Scale(Duration duration, double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.duration = duration;
        }
        
        //<editor-fold defaultstate="collapsed" desc="Static initializers">
        public static Scale x(double x) {
            return xyz(x, 1, 1);
        }
        
        public static Scale y(double y) {
            return xyz(1, y, 1);
        }
        
        public static Scale z(double z) {
            return xyz(1, 1, z);
        }
        
        public static Scale xy(double x, double y) {
            return xyz(x, y, 1);
        }
        
        public static Scale xz(double x, double z) {
            return xyz(x, 1, z);
        }
        
        public static Scale yz(double y, double z) {
            return xyz(1, y, z);
        }
        
        public static Scale xyz(double x, double y, double z) {
            return new Scale(x, y, z);
        }
        
        public static Scale xyz(double value) {
            return xyz(value, value, value);
        }
        
        public static Scale x(Duration duration, double x) {
            return xyz(duration, x, 1, 1);
        }
        
        public static Scale y(Duration duration, double y) {
            return xyz(duration, 1, y, 1);
        }
        
        public static Scale z(Duration duration, double z) {
            return xyz(duration, 1, 1, z);
        }
        
        public static Scale xy(Duration duration, double x, double y) {
            return xyz(duration, x, y, 1);
        }
        
        public static Scale xz(Duration duration, double x, double z) {
            return xyz(duration, x, 1, z);
        }
        
        public static Scale yz(Duration duration, double y, double z) {
            return xyz(duration, 1, y, z);
        }
        
        public static Scale xyz(Duration duration, double x, double y, double z) {
            return new Scale(duration, x, y, z);
        }
        
        public static Scale xyz(Duration duration, double value) {
            return xyz(duration, value, value, value);
        }
        //</editor-fold>

        @Override
        public Duration getDuration() {
            return duration;
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

        @Override
        public ScaleTransition init(Node node, ScaleTransition transition) {
            if(transition == null) {
                transition = new ScaleTransition();
            }

            transition.setDuration(getDuration());
            transition.setFromX(node.getScaleX());
            transition.setFromY(node.getScaleY());
            transition.setFromZ(node.getScaleZ());
            transition.setToX(getX());
            transition.setToY(getY());
            transition.setToZ(getZ());
            transition.setNode(node);

            return transition;
        }
        
        @Override
        public boolean test(Node node) {
            return is(node.getTranslateX(), node.getTranslateY(), node.getTranslateZ());
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 89 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
            hash = 89 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
            hash = 89 * hash + (int) (Double.doubleToLongBits(this.z) ^ (Double.doubleToLongBits(this.z) >>> 32));
            hash = 89 * hash + Objects.hashCode(this.duration);
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            if(o == this) return true;
            if(o == null) return false;
            if(!(o instanceof Scale)) return false;
            
            Scale other = (Scale) o;
            return is(other.getX(), other.getY(), other.getZ());
        }

    }
    
}
