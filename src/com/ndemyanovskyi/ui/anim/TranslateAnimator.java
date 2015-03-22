/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.anim;

import static com.ndemyanovskyi.ui.anim.AbstractAnimator.DEFAULT_DURATION;
import com.ndemyanovskyi.ui.anim.XYZAnimator.XYZState;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 *
 * @author Назарій
 */
public class TranslateAnimator extends XYZAnimator<TranslateTransition, TranslateAnimator.Translate> {
    
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
    protected Translate createState(double x, double y, double z) {
        return Translate.xyz(x, y, z);
    }
    
    public static final class Translate extends XYZState<TranslateTransition, Translate> {

        public Translate(double x, double y, double z) {
            super(x, y, z);
        }
        
        public Translate(Node node) {
            this(node.getTranslateX(), node.getTranslateY(), node.getTranslateZ());
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
        //</editor-fold>

        @Override
        public TranslateTransition init(Node node, TranslateTransition transition, Duration duration) {
            if(transition == null) {
                transition = new TranslateTransition();
            }
            if(!duration.isUnknown()) {
                transition.setDuration(duration);
            }
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
        public double ratio(Node node, Translate last) {
            return ratio(node.getTranslateX(), node.getTranslateY(), node.getTranslateZ(), last);
        }
        
        @Override
        public boolean test(Node node) {
            return is(node.getTranslateX(), node.getTranslateY(), node.getTranslateZ());
        }

    }
}
