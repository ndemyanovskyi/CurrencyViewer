/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.pane;

import com.ndemyanovskyi.reflection.Reflection;
import com.ndemyanovskyi.throwable.Exceptions;
import java.net.URL;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.beans.InvalidationListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Region;
import javafx.util.Duration;

/**
 *
 * @author Назарій
 */
public class DelayedResizePane extends InitializablePane {

    private Region content;

    private final Transition transition = new ResizeTransition();
    
    //private ObjectProperty<EventHandler<? super ActionEvent>> onResize;

    private final Timeline resizeTimeline
            = new Timeline(new KeyFrame(Duration.millis(80), e -> {
                if(content.getLayoutX() < -15) {
                    content.setLayoutX(-15);
                    //content.setOpacity(0.5);
                }
                if(content.getLayoutY() < -15) {
                    content.setLayoutY(-15);
                    //content.setOpacity(0.5);
                }
                transition.playFromStart();
                content.setPrefSize(getWidth(), getHeight());
                
                /*if(onResize != null) {
                    EventHandler<? super ActionEvent> handler = onResize.get();
                    if(handler != null) {
                        handler.handle(new ActionEvent(this, ActionEvent.NULL_SOURCE_TARGET));
                    }
                }*/
            }));

    public DelayedResizePane() {
        super(Reflection.getCallerClass());
    }

    public DelayedResizePane(String parent) {
        super(Reflection.getCallerClass(), parent);
    }
    
    /*public EventHandler<? super ActionEvent> getOnResize() {
        return onResize != null ? onResize.get() : null;
    }
    
    public void setOnResize(EventHandler<? super ActionEvent> handler) {
        if(handler != null || onResize != null) {
            onResizeProperty().set(handler);
        } 
    }
    
    public ObjectProperty<EventHandler<? super ActionEvent>> onResizeProperty() {
        return onResize != null ? onResize : (onResize = new SimpleObjectProperty<>());
    }*/

    @Override
    void init(URL url) {
        FXMLLoader loader = new FXMLLoader(url);
        loader.setController(this);
        content = Exceptions.execute(() -> loader.load());
        getChildren().add(content);
        
        InvalidationListener widthListener = (p) -> {
            double width = getWidth();
            if(width > content.minWidth(0)) {
                content.setLayoutX(getWidth() - content.getWidth());
            }
            transition.stop();
            content.setOpacity(1);
            resizeTimeline.playFromStart();
        };
        
        InvalidationListener heightListener = (p) -> {
            double height = getHeight();
            if(height > content.minHeight(0)) {
                content.setLayoutY(getHeight() - content.getHeight());
            }
            transition.stop();
            content.setOpacity(1);
            resizeTimeline.playFromStart();
        };
        
        InvalidationListener boundsListener = (p) -> {
            widthListener.invalidated(widthProperty());
            heightListener.invalidated(heightProperty());
        };
        
        widthProperty().addListener(widthListener);
        heightProperty().addListener(heightListener);
        
        parentProperty().addListener((property, oldParent, newParent) -> {
            if(oldParent != null) {
                oldParent.boundsInLocalProperty().removeListener(boundsListener);
            }
            if(newParent != null) {
                newParent.boundsInLocalProperty().addListener(boundsListener);
            }
        });

        sceneProperty().addListener((property, oldScene, newScene) -> {
            if(oldScene != null) {
                oldScene.widthProperty().removeListener(widthListener);
                oldScene.heightProperty().removeListener(heightListener);
            }
            if(newScene != null) {
                newScene.widthProperty().addListener(widthListener);
                newScene.heightProperty().addListener(heightListener);
            }
        });
    }

    public Region getContent() {
        return content;
    }

    private class ResizeTransition extends Transition {
        
        int stopped = 0;

        public ResizeTransition() {
            setCycleDuration(Duration.seconds(0.7));
        }

        @Override
        protected void interpolate(double frac) {
            content.setLayoutX(content.getLayoutX() - (content.getLayoutX() * frac / 2.5));
            content.setLayoutY(content.getLayoutY() - (content.getLayoutY() * frac / 2.5));
            content.setOpacity(content.getOpacity() + (content.getOpacity() * frac));
            
            if(frac == 1.0d) {
                content.setOpacity(1);
                content.setLayoutX(0);
                content.setLayoutY(0);
            }
        }

    }

}
