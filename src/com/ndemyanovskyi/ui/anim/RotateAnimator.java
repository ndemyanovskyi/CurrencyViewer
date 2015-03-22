/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.anim;

import com.ndemyanovskyi.ui.anim.Animator.Rationable;
import com.ndemyanovskyi.ui.anim.Animator.State;
import java.util.Objects;
import javafx.animation.RotateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 *
 * @author Назарій
 */
public class RotateAnimator extends AbstractAnimator<RotateTransition, RotateAnimator.Rotate> {

    public RotateAnimator(ObservableSet<Node> nodes) {
        super(nodes);
    }
    
    public RotateAnimator(Node... nodes) {
        this(FXCollections.observableSet(nodes));
    } 

    public RotateAnimator(Duration duration, ObservableSet<Node> nodes) {
        super(duration, nodes);
    }
    
    public RotateAnimator(Duration duration, Node... nodes) {
        this(duration, FXCollections.observableSet(nodes));
    } 

    public void play(double angle) {
        play(angle, null);
    }

    public void play(double angle, Point3D axis) {
        Rotate rotate = null;
        for(Rotate o : getStates()) {
            if(o.is(angle, axis)) {
                rotate = o;
                break;
            }
        }
        if(rotate == null) {
            rotate = new Rotate(angle, axis);
        }
        play(rotate);
    }
    
    public static class Rotate implements State<RotateTransition>, Rationable<Rotate>, Comparable<Rotate> {
        
        private final double angle;
        private final Point3D axis;

        public Rotate(double angle) {
            this(angle, null);
        }

        public Rotate(double angle, Point3D axis) {
            this.angle = angle;
            this.axis = axis;
        }

        public Point3D getAxis() {
            return axis;
        }

        public double getAngle() {
            return angle;
        }

        @Override
        public RotateTransition init(Node node, RotateTransition transition, Duration duration) {
            if(transition == null) {
                transition = new RotateTransition();
            }
            
            if(!duration.isUnknown()) {
                transition.setDuration(duration);
            }
            transition.setFromAngle(node.getRotate());
            transition.setToAngle(getAngle());
            transition.setAxis(getAxis());
            transition.setNode(node);
            return transition;
        }
        
        public boolean is(double angle) {
            return Double.compare(angle, getAngle()) == 0;
        }
        
        public boolean is(double angle, Point3D axis) {
            return is(angle) && Objects.equals(axis, getAxis());
        }

        @Override
        public boolean test(Node n) {
            return is(n.getRotate(), n.getRotationAxis());
        }

        @Override
        public double ratio(Node node, Rotate last) {
            return ratio(node.getRotate(), last);
        }

        @Override
        public double ratio(Rotate state, Rotate last) {
            return ratio(getAngle(), last);
        }

        public double ratio(double angle, Rotate last) {
            return Rationable.ratio(angle, getAngle(), last.getAngle());
        }

        @Override
        public int compareTo(Rotate other) {
            return Double.compare(getAngle(), other.getAngle());
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 53 * hash + (int) (Double.doubleToLongBits(this.angle) ^ (Double.doubleToLongBits(this.angle) >>> 32));
            hash = 53 * hash + Objects.hashCode(this.axis);
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            if(o == this) return true;
            if(o == null) return false;
            if(!(o instanceof Rotate)) return false;
            
            Rotate other = (Rotate) o;
            return is(other.getAngle(), other.getAxis());
        }
        
    }
    
}
