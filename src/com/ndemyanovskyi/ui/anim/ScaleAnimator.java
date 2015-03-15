/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.anim;

import static com.ndemyanovskyi.ui.anim.AbstractAnimator.DEFAULT_DURATION;
import java.util.Arrays;
import java.util.Collection;
import javafx.animation.ScaleTransition;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 *
 * @author Назарій
 */
public class ScaleAnimator extends AbstractAnimator<ScaleTransition> {
    
    private final double downX, upX, downY, upY, downZ, upZ;
    
    public ScaleAnimator(double downX, double upX, double downY, double upY, double downZ, double upZ, Collection<Node> nodes) {
        this(DEFAULT_DURATION, downX, upX, downY, upY, downZ, upZ, nodes);
    } 
    
    public ScaleAnimator(double downX, double upX, double downY, double upY, double downZ, double upZ, Node... nodes) {
        this(downX, upX, downY, upY, downZ, upZ, Arrays.asList(nodes));
    } 
    
    public ScaleAnimator(Duration duration, double downX, double upX, double downY, double upY, double downZ, double upZ, Node... nodes) {
        this(duration, downX, upX, downY, upY, downZ, upZ, Arrays.asList(nodes));
    } 
    
    public ScaleAnimator(Duration duration, double downX, double upX, double downY, double upY, double downZ, double upZ, Collection<Node> nodes) {
        super(duration, nodes);
        this.downX = downX;
        this.downY = downY;
        this.downZ = downZ;
        this.upX = upX;
        this.upY = upY;
        this.upZ = upZ;
    } 
    
    public static ScaleAnimator ofX(double downX, double upX, Node... nodes) {
        return new ScaleAnimator(downX, upX, 0, 0, 0, 0, nodes);
    }
    
    public static ScaleAnimator ofY(double downY, double upY, Node... nodes) {
        return new ScaleAnimator(0, 0, downY, upY, 0, 0, nodes);
    }
    
    public static ScaleAnimator ofZ(double downZ, double upZ, Node... nodes) {
        return new ScaleAnimator(0, 0, 0, 0, downZ, upZ, nodes);
    }
    
    public static ScaleAnimator ofXZ(double downX, double upX, double downZ, double upZ, Node... nodes) {
        return new ScaleAnimator(downX, upX, 0, 0, downZ, upZ, nodes);
    }
    
    public static ScaleAnimator ofXY( double downX, double upX, double downY, double upY, Node... nodes) {
        return new ScaleAnimator(downX, upX, downY, upY, 0, 0, nodes);
    }
    
    public static ScaleAnimator ofYZ(double downY, double upY, double downZ, double upZ, Node... nodes) {
        return new ScaleAnimator(0, 0, downY, upY, downZ, upZ, nodes);
    }
    
    public static ScaleAnimator ofXYZ(double downX, double upX, double downY, double upY, double downZ, double upZ, Node... nodes) {
        return new ScaleAnimator(downX, upX, downY, upY, downZ, upZ, nodes);
    }
    
    public static ScaleAnimator ofX(Duration duration, double downX, double upX, Node... nodes) {
        return new ScaleAnimator(duration, downX, upX, 0, 0, 0, 0, nodes);
    }
    
    public static ScaleAnimator ofY(Duration duration, double downY, double upY, Node... nodes) {
        return new ScaleAnimator(duration, 0, 0, downY, upY, 0, 0, nodes);
    }
    
    public static ScaleAnimator ofZ(Duration duration, double downZ, double upZ, Node... nodes) {
        return new ScaleAnimator(duration, 0, 0, 0, 0, downZ, upZ, nodes);
    }
    
    public static ScaleAnimator ofXZ(Duration duration, double downX, double upX, double downZ, double upZ, Node... nodes) {
        return new ScaleAnimator(duration, downX, upX, 0, 0, downZ, upZ, nodes);
    }
    
    public static ScaleAnimator ofXY(Duration duration, double downX, double upX, double downY, double upY, Node... nodes) {
        return new ScaleAnimator(duration, downX, upX, downY, upY, 0, 0, nodes);
    }
    
    public static ScaleAnimator ofYZ(Duration duration, double downY, double upY, double downZ, double upZ, Node... nodes) {
        return new ScaleAnimator(duration, 0, 0, downY, upY, downZ, upZ, nodes);
    }
    
    public static ScaleAnimator ofXYZ(Duration duration, double downX, double upX, double downY, double upY, double downZ, double upZ, Node... nodes) {
        return new ScaleAnimator(duration, downX, upX, downY, upY, downZ, upZ, nodes);
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
        double x = Math.abs((node.getScaleX() - downX) / (upX - downX));
        double y = Math.abs((node.getScaleY() - downY) / (upY - downY));
        double z = Math.abs((node.getScaleZ() - downZ) / (upZ - downZ));
        
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
        return getDuration().multiply(1 - getProgress(node));
    }

    @Override
    protected Duration getUpOffset(Node node) {
        return getDuration().multiply(getProgress(node));
    }

    @Override
    protected ScaleTransition initUpTransition(Node node, ScaleTransition transition) {
        if(transition == null) {
            transition = new ScaleTransition();
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
    protected ScaleTransition initDownTransition(Node node, ScaleTransition transition) {
        if(transition == null) {
            transition = new ScaleTransition();
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
