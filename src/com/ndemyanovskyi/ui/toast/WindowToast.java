/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ndemyanovskyi.ui.toast;

import com.ndemyanovskyi.app.Application;
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.animation.FadeTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.Scene;
import javafx.stage.Window;


class WindowToast extends Toast<Window> {
    
    private static Map<Window, ToastThread> threads = new HashMap<>();

    public WindowToast(Window owner) {
	super(owner);
    }

    @Override
    public void onShow() {
	ToastThread thread = threads.get(getOwner());
	
	if(thread == null) {
	    synchronized(thread = new ToastThread(getOwner())) {
		threads.put(getOwner(), thread);
		thread.add(this);
		thread.start();
	    }
	} else {
	    synchronized(thread) {
		if(!thread.isAlive()) {
		    thread = new ToastThread(getOwner());
		    threads.put(getOwner(), thread);
		    thread.add(this);
		    thread.start();
		} else {
		    thread.add(this);
		}
	    }
	}
    }
    
    static WindowToast getCurrent0(Window window) {
	ToastThread thread = threads.get(window);
	return thread != null ? !thread.toasts.isEmpty() ? 
		thread.toasts.get(0) : null : null;
    }

    protected void updateLocation(ToastPopup<Window> popup) {
	double x, y;
	Scene s = popup.getToast().getOwner().getScene();
	//System.out.println(t + " Scene bounds = [" + s.getX() + "; " + s.getY() + "]");
	double offsetX = s.getX() + s.getWindow().getX();
	double offsetY = s.getY() + s.getWindow().getY();

	switch(popup.getToast().getAlignment()) {
	    //<editor-fold defaultstate="collapsed" desc="bottom cases">
	    case BOTTOM_LEFT:
		x = 0;
		y = s.getHeight() - popup.getHeight();
		break;

	    case BOTTOM_CENTER:
		x = s.getWidth() / 2 - (popup.getWidth() / 2);
		y = s.getHeight() - popup.getHeight();
		break;

	    case BOTTOM_RIGHT:
		x = s.getWidth() - popup.getWidth();
		y = s.getHeight() - popup.getHeight();
		break;
		//</editor-fold>
	    //<editor-fold defaultstate="collapsed" desc="top cases">
	    case TOP_LEFT:
		x = 0;
		y = 0;
		break;

	    case TOP_CENTER:
		x = s.getWidth() / 2 - (popup.getWidth() / 2);
		y = 0;
		break;

	    case TOP_RIGHT:
		x = s.getWidth() - popup.getWidth();
		y = 0;
		break;
		//</editor-fold>
	    //<editor-fold defaultstate="collapsed" desc="left, right, center cases">
	    case LEFT:
		x = 0;
		y = s.getHeight() / 2 - (popup.getHeight() / 2);
		break;
	    case RIGHT:
		x = s.getWidth() - popup.getWidth();
		y = s.getHeight() / 2 - (popup.getHeight() / 2);
		break;

	    default:
		x = s.getWidth() / 2 - (popup.getWidth() / 2);
		y = s.getHeight() / 2 - (popup.getHeight() / 2);
		break;
	    //</editor-fold>
	    }

	popup.setX(x + popup.getToast().getOffset().getX() + offsetX);
	popup.setY(y + popup.getToast().getOffset().getY() + offsetY);
    }
    
    private class ToastThread extends Thread implements InvalidationListener {

	private final List<WindowToast> toasts = new ArrayList<>();
	private final Map<WindowToast, ToastPopup<Window>> popups = new HashMap<>();

	/*private final ToastController controller = new ToastController();

	private final Popup popup = new Popup(); {
	    popup.getContent().add(controller.getRoot());
	}*/
	
	private final Window window;
	
	private boolean finished;

	public ToastThread(Window w) {
	    window = w;
	}
	
	public void add(WindowToast toast) {
	    synchronized(toasts) {
		if(!toast.isCancelled() && !toast.isInQueue() && 
			!toast.isShown() && !toasts.contains(toast)) {
		    toasts.add(toast);
		}
	    }
	}

	@Override
	public void run() {
	    window.xProperty().addListener(this);
	    window.yProperty().addListener(this);
	    window.getScene().widthProperty().addListener(this);
	    window.getScene().heightProperty().addListener(this);
	    
	    try {
		while(!toasts.isEmpty()) {
		    WindowToast t = toasts.get(0);
		    if(!t.isCancelled()) {
			show(t);
		    }
		    toasts.remove(0);
		}
		finished = true;
	    } catch(InterruptedException ex) {
	    } finally {
		threads.remove(window);
	    }
	}
	
	public void show(WindowToast t) throws InterruptedException {
	    FadeTransition anim = new FadeTransition(ANIMATION_DURATION);
	    
	    Application.execute(() -> {
		ToastPopup<Window> popup = new ToastPopup<>();
		popups.put(t, popup);
		popup.show(t, t.getOwner());
		updateLocation(popup);
		anim.setNode(popup.getRoot());
		
		anim.setFromValue(0);
		anim.setToValue(1);
		anim.play();
	    });
	    
	    double sleep = (t.getDuration().inMillis() + anim.getDuration().toMillis());
	    while((sleep -= 50) > 0) {
		if(!t.isCancelled()) sleep(50);
		else break;
	    }
	    
	    Application.execute(() -> {
		anim.setFromValue(1);
		anim.setToValue(0);
		anim.setOnFinished(e -> finish(t));
		anim.play();
	    });
	}
	
	private void finish(WindowToast t) {
	    popups.remove(t).hide();
	    if(finished) {
		window.xProperty().removeListener(this);
		window.yProperty().removeListener(this);
		window.getScene().widthProperty().removeListener(this);
		window.getScene().heightProperty().removeListener(this);
	    }
	}

	@Override
	public void invalidated(Observable observable) {
	    synchronized(popups) {
		for(ToastPopup<Window> popup : popups.values()) {
		    updateLocation(popup);
		}
	    }
	}

    }

}
