/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.anim;

import static com.ndemyanovskyi.ui.anim.AbstractAnimator.DEFAULT_DURATION;
import com.ndemyanovskyi.ui.anim.XYZAnimator.XYZState;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 *
 * @author Назарій
 */
public class ScaleAnimator extends XYZAnimator<ScaleTransition, ScaleAnimator.Scale> {
    
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
    protected Scale createState(double x, double y, double z) {
        return Scale.xyz(x, y, z);
    }
    
    public static final class Scale extends XYZState<ScaleTransition, Scale> {

        public Scale(double x, double y, double z) {
            super(x, y, z);
        }
        
        public Scale(Node node) {
            this(node.getScaleX(), node.getScaleY(), node.getScaleZ());
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
        //</editor-fold>

        @Override
        public ScaleTransition init(Node node, ScaleTransition transition, Duration duration) {
            if(transition == null) {
                transition = new ScaleTransition();
            }

            if(!duration.isUnknown()) {
                transition.setDuration(duration);
            }
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
        public double ratio(Node node, Scale last) {
            return ratio(node.getScaleX(), node.getScaleY(), node.getScaleZ(), last);
        }
        
        @Override
        public boolean test(Node node) {
            return is(node.getTranslateX(), node.getTranslateY(), node.getTranslateZ());
        }

    }
    
}
