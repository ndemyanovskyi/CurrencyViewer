/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.toast;

import com.ndemyanovskyi.app.Application;
import static com.ndemyanovskyi.ui.toast.Toast.ANIMATION_DURATION;
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.FadeTransition;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ScreenToast extends Toast<Screen> {

    private static ToastThread thread;

    ScreenToast() {
	super(Screen.getPrimary());
    }

    @Override
    public void onShow() {
	
	if(thread == null) {
	    thread = new ToastThread(this);
	} else {
	    synchronized(thread) {
		if(!thread.isAlive()) {
		    thread = new ToastThread(this);
		} else {
		    thread.add(this);
		}
	    }
	}
    }
    
    static ScreenToast getCurrentToast() {
	List<ScreenToast> toasts = thread.toasts;
	return !toasts.isEmpty() ? toasts.get(0) : null;
    }

    private static class ToastThread extends Thread {

	private final List<ScreenToast> toasts = new ArrayList<>();

	private final ToastPopup<Screen> popup = new ToastPopup();

	private final Stage stage = new Stage();

	{
	    stage.initStyle(StageStyle.UTILITY);
	    stage.setAlwaysOnTop(true);
	    stage.setX(-100);
	    stage.setX(-100);
	    stage.setWidth(0);
	    stage.setHeight(0);
	    stage.setMaxWidth(0);
	    stage.setMinWidth(0);
	    stage.setMaxHeight(0);
	    stage.setMinHeight(0);
	    stage.setIconified(false);
	    stage.setOpacity(0);
	    stage.show();
	}

	public ToastThread(ScreenToast... toasts) {
	    for(ScreenToast t : toasts) {
		add(t);
	    }
	    start();
	}
	
	public void add(ScreenToast toast) {
	    synchronized(toasts) {
		if(!toast.isCancelled() && !toast.isInQueue() && 
			!toast.isShown() && !toasts.contains(toast)) {
		    toasts.add(toast);
		}
	    }
	}

	@Override
	public void run() {
	    try {
		while(true) {
		    while(!toasts.isEmpty()) {
			ScreenToast t = toasts.get(0);
			if(!t.isCancelled()) {
			    show(t);
			}
			toasts.remove(0);
		    }
		    sleep(50);
		}
	    } catch(InterruptedException ex) {
	    }
	}
	
	private static void updateLocation(ToastPopup<Screen> popup) {
	    double x, y;
	    
	    Rectangle2D rect = popup.getToast().getOwner().getBounds();
	    switch(popup.getToast().getAlignment()) {
		//<editor-fold defaultstate="collapsed" desc="bottom cases">
		case BOTTOM_LEFT:
		    x = 0;
		    y = rect.getHeight() - popup.getHeight();
		    break;
		    
		case BOTTOM_CENTER:
		    x = rect.getWidth() / 2 - (popup.getWidth() / 2);
		    y = rect.getHeight() - popup.getHeight();
		    break;
		    
		case BOTTOM_RIGHT:
		    x = rect.getWidth();
		    y = rect.getHeight() - popup.getHeight();
		    break;
		//</editor-fold>
		//<editor-fold defaultstate="collapsed" desc="top cases">
		case TOP_LEFT:
		    x = 0;
		    y = 0;
		    break;
		    
		case TOP_CENTER:
		    x = rect.getWidth() / 2 - (popup.getWidth() / 2);
		    y = 0;
		    break;
		    
		case TOP_RIGHT:
		    x = rect.getWidth() - popup.getWidth();
		    y = 0;
		    break;
		//</editor-fold>
		//<editor-fold defaultstate="collapsed" desc="left, right, center cases">
		case LEFT:
		    x = 0;
		    y = rect.getHeight() / 2 - (popup.getHeight() / 2);
		    break;
		case RIGHT:
		    x = rect.getWidth() - popup.getWidth();
		    y = rect.getHeight() / 2 - (popup.getHeight() / 2);
		    break;

		default:
		    x = rect.getWidth() / 2 - (popup.getWidth() / 2);
		    y = rect.getHeight() / 2 - (popup.getHeight() / 2);
		    break;
		//</editor-fold>
	    }
	    
	    popup.setX(x + popup.getToast().getOffset().getX());
	    popup.setY(y + popup.getToast().getOffset().getY());
	}
	
	public void show(ScreenToast t) throws InterruptedException {
	    FadeTransition anim = new FadeTransition(ANIMATION_DURATION);
	    Application.execute(() -> {
		popup.show(t, stage);
		updateLocation(popup);
		anim.setNode(popup.getRoot());
		
		anim.setFromValue(0);
		anim.setToValue(1);
		anim.play();
	    });
	    
	    sleep((long) (t.getDuration().inMillis() + anim.getDuration().toMillis()));
	    
	    Application.execute(() -> {
		anim.setFromValue(1);
		anim.setToValue(0);
		anim.play();
		//anim.setOnFinished(e -> popup.hide());
	    });
	}

	/*private void show(ScreenToast t) throws InterruptedException {
	    Application.execute(() -> {
		controller.setNode(t.getNode());
		t.showing = true;
		t.shown = true;
		
		popup.show(stage);
		setPosition(popup, t);
	    });

	    animateOpacity(controller.getRoot(), 0, 1, 0.05);
	}

	private static void animateOpacity(Node node, double from, double to, double step) throws InterruptedException {
	    for(double i = from; step > 0 ? i <= to : i >= to; i += step) {
		double opacity = i;
		Application.execute(() -> node.setOpacity(opacity));
		sleep(10);
	    }
	    Application.execute(() -> node.setOpacity(to));
	}

	private void hide(ScreenToast t) throws InterruptedException {
	    animateOpacity(controller.getRoot(), 1, 0, -0.05);

	    Application.execute(() -> {
		t.showing = false;
		popup.hide();
		controller.setNode(null);
	    });

	}*/

    }
    
}
