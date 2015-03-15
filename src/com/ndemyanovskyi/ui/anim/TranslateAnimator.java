/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.anim;

import java.util.Arrays;
import java.util.Collection;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 *
 * @author Назарій
 */
public class TranslateAnimator extends AbstractAnimator<TranslateTransition> {
    
    private final double downX, upX, downY, upY, downZ, upZ;
    
    public TranslateAnimator(double downX, double upX, double downY, double upY, double downZ, double upZ, Collection<Node> nodes) {
        this(DEFAULT_DURATION, downX, upX, downY, upY, downZ, upZ, nodes);
    } 
    
    public TranslateAnimator(double downX, double upX, double downY, double upY, double downZ, double upZ, Node... nodes) {
        this(downX, upX, downY, upY, downZ, upZ, Arrays.asList(nodes));
    } 
    
    public TranslateAnimator(Duration duration, double downX, double upX, double downY, double upY, double downZ, double upZ, Node... nodes) {
        this(duration, downX, upX, downY, upY, downZ, upZ, Arrays.asList(nodes));
    } 
    
    public TranslateAnimator(Duration duration, double downX, double upX, double downY, double upY, double downZ, double upZ, Collection<Node> nodes) {
        super(duration, nodes);
        this.downX = downX;
        this.downY = downY;
        this.downZ = downZ;
        this.upX = upX;
        this.upY = upY;
        this.upZ = upZ;
    } 
    
    public static TranslateAnimator ofX(double downX, double upX, Node... nodes) {
        return new TranslateAnimator(downX, upX, 0, 0, 0, 0, nodes);
    }
    
    public static TranslateAnimator ofY(double downY, double upY, Node... nodes) {
        return new TranslateAnimator(0, 0, downY, upY, 0, 0, nodes);
    }
    
    public static TranslateAnimator ofZ(double downZ, double upZ, Node... nodes) {
        return new TranslateAnimator(0, 0, 0, 0, downZ, upZ, nodes);
    }
    
    public static TranslateAnimator ofXZ(double downX, double upX, double downZ, double upZ, Node... nodes) {
        return new TranslateAnimator(downX, upX, 0, 0, downZ, upZ, nodes);
    }
    
    public static TranslateAnimator ofXY( double downX, double upX, double downY, double upY, Node... nodes) {
        return new TranslateAnimator(downX, upX, downY, upY, 0, 0, nodes);
    }
    
    public static TranslateAnimator ofYZ(double downY, double upY, double downZ, double upZ, Node... nodes) {
        return new TranslateAnimator(0, 0, downY, upY, downZ, upZ, nodes);
    }
    
    public static TranslateAnimator ofXYZ(double downX, double upX, double downY, double upY, double downZ, double upZ, Node... nodes) {
        return new TranslateAnimator(downX, upX, downY, upY, downZ, upZ, nodes);
    }
    
    public static TranslateAnimator ofX(Duration duration, double downX, double upX, Node... nodes) {
        return new TranslateAnimator(duration, downX, upX, 0, 0, 0, 0, nodes);
    }
    
    public static TranslateAnimator ofY(Duration duration, double downY, double upY, Node... nodes) {
        return new TranslateAnimator(duration, 0, 0, downY, upY, 0, 0, nodes);
    }
    
    public static TranslateAnimator ofZ(Duration duration, double downZ, double upZ, Node... nodes) {
        return new TranslateAnimator(duration, 0, 0, 0, 0, downZ, upZ, nodes);
    }
    
    public static TranslateAnimator ofXZ(Duration duration, double downX, double upX, double downZ, double upZ, Node... nodes) {
        return new TranslateAnimator(duration, downX, upX, 0, 0, downZ, upZ, nodes);
    }
    
    public static TranslateAnimator ofXY(Duration duration, double downX, double upX, double downY, double upY, Node... nodes) {
        return new TranslateAnimator(duration, downX, upX, downY, upY, 0, 0, nodes);
    }
    
    public static TranslateAnimator ofYZ(Duration duration, double downY, double upY, double downZ, double upZ, Node... nodes) {
        return new TranslateAnimator(duration, 0, 0, downY, upY, downZ, upZ, nodes);
    }
    
    public static TranslateAnimator ofXYZ(Duration duration, double downX, double upX, double downY, double upY, double downZ, double upZ, Node... nodes) {
        return new TranslateAnimator(duration, downX, upX, downY, upY, downZ, upZ, nodes);
    }

    public double getDownX() {
        return downX;
    }

    public double getUpX() {
        return upX;
    }

    public double getDownY() {
        return downY;
    }

    public double getUpY() {
        return upY;
    }

    public double getDownZ() {
        return downZ;
    }

    public double getUpZ() {
        return upZ;
    }
    
    private double getProgress(Node node) {
        double x = Math.abs((node.getTranslateX() - downX) / (upX - downX));
        double y = Math.abs((node.getTranslateY() - downY) / (upY - downY));
        double z = Math.abs((node.getTranslateZ() - downZ) / (upZ - downZ));
        
        double[] arr = {x, y, z};
        double sum = 0;
        int count = 0;
        for(double d : arr) {
            if(d > 0 && Double.isFinite(d)) {
                sum += d;
                count++;
            }
        }
        double res = sum / count;
        return Double.isFinite(res) ? res : 0;
    }

    @Override
    protected Duration getDownOffset(Node node) {
        return getDuration().multiply(1- getProgress(node));
    }

    @Override
    protected Duration getUpOffset(Node node) {
        return getDuration().multiply(getProgress(node));
    }
    
    @Override
    protected TranslateTransition initUpTransition(Node node, TranslateTransition transition) {
        if(transition == null) {
            transition = new TranslateTransition();
        }
        
        transition.setFromX(downX);
        transition.setFromY(downY);
        transition.setFromZ(downZ);
        transition.setToX(upX);
        transition.setToY(upY);
        transition.setToZ(upZ);
        transition.setDuration(getDuration());
        transition.setNode(node);
        
        return transition;
    }

    @Override
    protected TranslateTransition initDownTransition(Node node, TranslateTransition transition) {
        if(transition == null) {
            transition = new TranslateTransition();
        }
        
        transition.setFromX(upX);
        transition.setFromY(upY);
        transition.setFromZ(upZ);
        transition.setToX(downX);
        transition.setToY(downY);
        transition.setToZ(downZ);
        transition.setDuration(getDuration());
        transition.setNode(node);
        
        return transition;
    }
    
    
}
