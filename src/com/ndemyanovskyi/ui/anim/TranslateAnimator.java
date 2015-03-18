/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.anim;

import static com.ndemyanovskyi.ui.anim.AbstractAnimator.DEFAULT_DURATION;
import java.util.Objects;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 *
 * @author Назарій
 */
public class TranslateAnimator extends AbstractAnimator<TranslateTransition, TranslateAnimator.Translate> {
    
    public TranslateAnimator(ObservableSet<Node> nodes) {
        this(DEFAULT_DURATION, nodes);
    } 
    
    public TranslateAnimator(Node... nodes) {
        this(FXCollections.observableSet(nodes));
    } 
    
    public TranslateAnimator(Duration duration, Node... nodes) {
        this(duration, FXCollections.observableSet(nodes));
    } 
    
    public TranslateAnimator(Duration duration, ObservableSet<Node> nodes) {
        super(duration, nodes);
    } 

    @Override
    protected TranslateTransition initTransition(Node node, TranslateTransition transition, Translate state) {
        transition = state.init(node, transition);
        if(transition.getDuration().isUnknown()) {
            transition.setDuration(getDuration());
        }
        return transition;
    }

    public void play(double x, double y, double z) {
        Translate scale = null;
        for(Translate o : getStates()) {
            if(o.is(x, y, z)) {
                scale = o;
                break;
            }
        }
        if(scale == null) {
            scale = new Translate(x, y, z);
        }
        play(scale);
    }

    public void playX(double x) {
        play(x, 0, 0);
    }

    public void playY(double y) {
        play(0, y, 0);
    }

    public void playZ(double z) {
        play(0, 0, z);
    }

    public void playXZ(double x, double z) {
        play(x, 0, z);
    }

    public void playYZ(double y, double z) {
        play(0, y, z);
    }

    public void playXYZ(double value) {
        play(value, value, value);
    }

    public void playXY(double x, double y) {
        play(x, y, 0);
    }
    
    public static final class Translate implements Animator.State<TranslateTransition> {

        private final double x, y, z;
        private final Duration duration;

        public Translate(double x, double y, double z) {
            this(Duration.UNKNOWN, x, y, z);
        }

        public Translate(Duration duration, double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.duration = duration;
        }
        
        //<editor-fold defaultstate="collapsed" desc="Static initializers">
        public static Translate x(double x) {
            return xyz(x, 0, 0);
        }
        
        public static Translate y(double y) {
            return xyz(0, y, 0);
        }
        
        public static Translate z(double z) {
            return xyz(0, 0, z);
        }
        
        public static Translate xy(double x, double y) {
            return xyz(x, y, 0);
        }
        
        public static Translate xz(double x, double z) {
            return xyz(x, 0, z);
        }
        
        public static Translate yz(double y, double z) {
            return xyz(0, y, z);
        }
        
        public static Translate xyz(double x, double y, double z) {
            return new Translate(x, y, z);
        }
        
        public static Translate xyz(double value) {
            return xyz(value, value, value);
        }
        
        public static Translate x(Duration duration, double x) {
            return xyz(duration, x, 0, 0);
        }
        
        public static Translate y(Duration duration, double y) {
            return xyz(duration, 0, y, 0);
        }
        
        public static Translate z(Duration duration, double z) {
            return xyz(duration, 0, 0, z);
        }
        
        public static Translate xy(Duration duration, double x, double y) {
            return xyz(duration, x, y, 0);
        }
        
        public static Translate xz(Duration duration, double x, double z) {
            return xyz(duration, x, 0, z);
        }
        
        public static Translate yz(Duration duration, double y, double z) {
            return xyz(duration, 0, y, z);
        }
        
        public static Translate xyz(Duration duration, double x, double y, double z) {
            return new Translate(duration, x, y, z);
        }
        
        public static Translate xyz(Duration duration, double value) {
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
        public TranslateTransition init(Node node, TranslateTransition transition) {
            if(transition == null) {
                transition = new TranslateTransition();
            }

            transition.setDuration(getDuration());
            transition.setFromX(node.getTranslateX());
            transition.setFromY(node.getTranslateY());
            transition.setFromZ(node.getTranslateZ());
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
            if(!(o instanceof Translate)) return false;
            
            Translate other = (Translate) o;
            return is(other.getX(), other.getY(), other.getZ());
        }

    }
}
