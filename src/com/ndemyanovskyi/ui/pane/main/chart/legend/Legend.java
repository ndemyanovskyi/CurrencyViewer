/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ndemyanovskyi.ui.pane.main.chart.legend;

import com.ndemyanovskyi.app.Application;
import com.ndemyanovskyi.ui.anim.Animator;
import com.ndemyanovskyi.ui.anim.FadeAnimator;
import com.ndemyanovskyi.ui.pane.InitializableHBox;
import com.ndemyanovskyi.ui.pane.main.chart.Intent;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.stage.Popup;
import javafx.util.Duration;

public final class Legend extends InitializableHBox {
    
    @FXML private Label bankLabel;
    @FXML private Label fieldLabel;
    @FXML private Label currencyLabel;
    @FXML private Line colorLine;
    @FXML private Button closeButton;
    @FXML private VBox titleBox;
    @FXML private HBox bottomBox;
    @FXML private HBox bottomLabelBox;
    @FXML private ProgressIndicator progressIndicator;
    
    private ObjectProperty<EventHandler<ActionEvent>> onCloseRequested;
    private ReadOnlyObjectWrapper<Intent<?>> intent;
    
    public Legend(Intent<?> intent) {
	setIntent(intent);
    }	
    
    public Legend(Intent<?> intent, EventHandler<ActionEvent> handler) {
	setIntent(intent);
	setOnCloseRequested(handler);
    }	

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        progressProperty().addListener((property, oldProgress, newProgress) -> {
            if(newProgress.intValue() == 1) {
                showCloseButton();
            }
        });
        colorLine.endXProperty().bind(
                Bindings.max(bankLabel.widthProperty(), bottomBox.widthProperty()));
        Application.execute(() -> {
            descriptionPopup = new Popup(); 
            HBox node = new InitializableHBox("LegendDescription.fxml");
            node.setMouseTransparent(true);
            descriptionPopup.getContent().add(node);
        });
    }

    public Intent<?> getIntent() {
        return intentPropertyImpl().get();
    }

    private void setIntent(Intent<?> intent) {
        intentPropertyImpl().set(intent);
    }
    
    public ReadOnlyObjectProperty<Intent<?>> intentProperty() {
        return intentPropertyImpl().getReadOnlyProperty();
    }
    
    private ReadOnlyObjectWrapper<Intent<?>> intentPropertyImpl() {
        
        if(intent == null) {
            intent = new ReadOnlyObjectWrapper<Intent<?>>() {

                @Override
                public void set(Intent<?> intent) {
                   super.set(Objects.requireNonNull(intent, "intent"));
                }
                
            };
        }
	return intent;
    }

    public EventHandler<ActionEvent> getOnCloseRequested() {
	return onCloseRequestedProperty().get();
    }

    public void setOnCloseRequested(EventHandler<ActionEvent> onCloseRequested) {
	onCloseRequestedProperty().set(onCloseRequested);
    }

    public ObjectProperty<EventHandler<ActionEvent>> onCloseRequestedProperty() {
	if(onCloseRequested == null) {
	    onCloseRequested = new SimpleObjectProperty<>();
	}
	return onCloseRequested;
    }
    
    public void setProgress(double progress) {
        progressProperty().set(progress);
    }
    
    public double getProgress() {
        return progressProperty().get();
    }
    
    public DoubleProperty progressProperty() {
        return progressIndicator.progressProperty();
    }
    
    @FXML
    private void onButtonCloseAction(ActionEvent e) {
	if(onCloseRequested != null) {
	    EventHandler<ActionEvent> handler = onCloseRequested.get();
	    if(handler != null) {
		handler.handle(new ActionEvent(this, e.getTarget()));
	    }
	}
    }
    
    private final Animator closeButtonAnimator = 
            new FadeAnimator(Duration.millis(200), closeButton);
    private final Animator progressIndicatorAnimator = 
            new FadeAnimator(Duration.millis(200), progressIndicator);
    private Popup descriptionPopup;
    
    @FXML
    private void onMouseExited(MouseEvent e) { 
        //Application.execute(() -> descriptionPopup.hide());
        if(getProgress() < 1.0d) showProgressIndicator();
    }
    
    @FXML
    private void onMouseEntered(MouseEvent e) {
        /*Application.execute(() -> 
                descriptionPopup.show(this, e.getScreenX(), e.getScreenY()));*/
        showCloseButton();
    }
    
    private void showProgressIndicator() {
        closeButtonAnimator.playDown();
        progressIndicatorAnimator.playUp();
    }
    
    private void showCloseButton() {
        closeButtonAnimator.playUp();
        progressIndicatorAnimator.playDown();
    }
    
}
