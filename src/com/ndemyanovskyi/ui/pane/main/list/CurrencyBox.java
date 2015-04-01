/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.pane.main.list;

import com.ndemyanovskyi.backend.Bank;
import com.ndemyanovskyi.backend.Rate.Field;
import com.ndemyanovskyi.ui.anim.OpacityAnimator;
import com.ndemyanovskyi.ui.pane.InitializableStackPane;
import com.sun.javafx.event.RedirectedEvent;
import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.shape.Circle;
import javafx.stage.Popup;
import javafx.util.Duration;

public class CurrencyBox extends InitializableStackPane {

    @FXML private Button titleButton;
    @FXML private Circle indicator;

    private ReadOnlyObjectWrapper<Field> field;
    private ReadOnlyObjectWrapper<Bank<?>> bank;
    private PopupController popupController;

    private final Timeline delayedPopupShowing = new Timeline(
	    new KeyFrame(Duration.millis(400), e -> getPopupController().show()));

    public CurrencyBox(Bank<?> bank, Field field) {
	bankPropertyImpl().set(bank);
	fieldPropertyImpl().set(field);
    }

    public Bank<?> getBank() {
	return bankPropertyImpl().get();
    }

    public Field getField() {
	return fieldPropertyImpl().get();
    }

    public final ReadOnlyObjectProperty<Field> fieldProperty() {
	return fieldPropertyImpl().getReadOnlyProperty();
    }

    public Circle getIndicator() {
	return indicator;
    }

    private ReadOnlyObjectWrapper<Field> fieldPropertyImpl() {
	return field != null ? field
		: (field = new ReadOnlyObjectWrapper<>(this, "field"));
    }

    private ReadOnlyObjectWrapper<Bank<?>> bankPropertyImpl() {
	return bank != null ? bank
		: (bank = new ReadOnlyObjectWrapper<>(this, "bank"));
    }

    public final ReadOnlyObjectProperty<Bank<?>> bankProperty() {
	return bankPropertyImpl().getReadOnlyProperty();
    }

    private PopupController getPopupController() {
	return popupController != null ? popupController
		: (popupController = new PopupController());
    }

    @FXML
    private void onAction(ActionEvent e) {
	getPopupController().show();
    }

    @FXML
    private void onMouseExited(MouseEvent e) {
	delayedPopupShowing.stop();
    }

    @FXML
    private void onMouseMoved(MouseEvent e) {
	delayedPopupShowing.play();
    }

    @FXML
    private void onScroll(ScrollEvent e) {
	delayedPopupShowing.stop();
    }

    private class PopupController {

	private Popup popup;

	private final CurrencyBoxPopupContent content = new CurrencyBoxPopupContent(CurrencyBox.this);
	private final OpacityAnimator opacityAnimator = new OpacityAnimator(Duration.millis(100), content);

	private boolean onceMouseEntered = false;
	private boolean hideRequested = false;

    private final Timeline delayedPopupHiding = new Timeline(
	    new KeyFrame(Duration.millis(1000),  e -> hideImpl()));

	public PopupController() {
	    opacityAnimator.playingProperty().addListener((p, oldPlaying, playing) -> {
		if(!playing && hideRequested) {
		    onceMouseEntered = false;
		    getPopup().hide();
		}
	    });
	}

	private boolean hasMouseOutsideFromContent(MouseEvent e) {
	    return !content.getInnerContent().contains(
		    content.getInnerContent().screenToLocal(e.getScreenX(), e.getScreenY()));
	}

	private Popup getPopup() {
	    if(popup == null) {
		popup = new Popup();
		popup.getContent().add(content);
		content.setOnMouseMoved(e -> {
		    if(hasMouseOutsideFromContent(e)) {
			hide();
		    }
		});
		content.getInnerContent().addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
		    onceMouseEntered = true;
		    delayedPopupHiding.stop();
		});
		popup.focusedProperty().addListener((p, oldFocused, focused) -> {
		    if(!focused) {
			hide();
		    }
		});
		popup.setEventDispatcher((event, tail) -> {
		    if(event.getEventType() == RedirectedEvent.REDIRECTED) {
			RedirectedEvent ev = (RedirectedEvent) event;
			if(ev.getOriginalEvent().getEventType() == MouseEvent.MOUSE_MOVED) {
			    MouseEvent mouseEvent = (MouseEvent) ev.getOriginalEvent();
			    if(hasMouseOutsideFromContent(mouseEvent)) {
				hide();
			    }
			}
		    } else {
			tail.dispatchEvent(event);
		    }
		    return null;
		});
	    }
	    return popup;
	}

	public void show() {
	    hideRequested = false;
	    delayedPopupHiding.stop();
	    Popup localPopup = getPopup();
	    if(!localPopup.isShowing()) {
		Bounds bounds = localToScreen(getBoundsInLocal());
		Bounds paneBounds = content.getBoundsInParent();
		localPopup.show(getScene().getWindow(),
			bounds.getMinX() + paneBounds.getMinX(),
			bounds.getMinY() + paneBounds.getMinY() - 10);

	    }
	    opacityAnimator.play(1.0d);
	}

	public void hide() {
	    if(onceMouseEntered && delayedPopupHiding.getStatus() != Status.RUNNING) {
		hideImpl();
	    } else {
		delayedPopupHiding.play();
	    }
	}
	
	private void hideImpl() {
	    hideRequested = true;
	    opacityAnimator.play(0.0d);
	}
    }

}
