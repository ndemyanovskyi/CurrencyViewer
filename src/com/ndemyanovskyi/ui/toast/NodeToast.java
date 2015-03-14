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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.stage.Popup;


class NodeToast extends Toast<Node> {
    
    private static final Map<Node, ToastThread> threads = new HashMap<>();

    public NodeToast(Node owner) {
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
    
    static NodeToast getCurrent0(Node node) {
	ToastThread thread = threads.get(node);
	return thread != null ? !thread.toasts.isEmpty() ? 
		thread.toasts.get(0) : null : null;
    }
    
    private static class ToastThread extends Thread implements ChangeListener {

	private final List<NodeToast> toasts = new ArrayList<>();
	private final Map<NodeToast, ToastPopup<Node>> popups = new HashMap<>();

	/*private final ToastController controller = new ToastController();

	private final Popup popup = new Popup(); {
	    popup.getContent().add(controller.getRoot());
	}*/
	
	private final Node node;
	
	private boolean finished;

	public ToastThread(Node n) {
	    node = n;
	}
	
	private void add(NodeToast toast) {
	    synchronized(toasts) {
		if(!toast.isCancelled() && !toast.isInQueue() && 
			!toast.isShown() && !toasts.contains(toast)) {
		    toasts.add(toast);
		}
	    }
	}

	@Override
	public void run() {
	    node.layoutBoundsProperty().addListener(this);
	    node.getScene().getWindow().xProperty().addListener(this);
	    node.getScene().getWindow().yProperty().addListener(this);
	    node.getScene().getWindow().widthProperty().addListener(this);
	    node.getScene().getWindow().heightProperty().addListener(this);
	    
	    try {
		while(!toasts.isEmpty()) {
		    NodeToast t = toasts.get(0);
		    if(!t.isCancelled()) {
			show(t);
		    }
		    toasts.remove(0);
		}
		finished = true;
	    } catch(InterruptedException ex) {
	    } finally {
		threads.remove(node);
	    }
	}
	
	public void show(NodeToast t) throws InterruptedException {
	    FadeTransition anim = new FadeTransition(ANIMATION_DURATION);
	    
	    Application.execute(() -> {
		ToastPopup<Node> popup = new ToastPopup<>();
		popups.put(t, popup);
		popup.show(t, t.getOwner().getScene().getWindow());
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
		anim.play();
		anim.setOnFinished(e -> {
		    Popup p = popups.remove(t);
		    if(p != null) p.hide();
		    if(finished) {
			node.getScene().getWindow().xProperty().removeListener(this);
			node.getScene().getWindow().yProperty().removeListener(this);
			node.getScene().widthProperty().removeListener(this);
			node.getScene().heightProperty().removeListener(this);
		    }
		});
	    });
	}
	
	private static void updateLocation(ToastPopup<Node> popup) {
	    double x, y;
	    
	    Bounds b = popup.getToast().getOwner().
		    localToScreen(popup.getToast().getOwner().getBoundsInLocal());
	    switch(popup.getToast().getAlignment()) {
		//<editor-fold defaultstate="collapsed" desc="bottom cases">
		case BOTTOM_LEFT:
		    x = b.getMinX();
		    y = b.getMaxY() - popup.getHeight();
		    break;
		    
		case BOTTOM_CENTER:
		    x = b.getMinX() + b.getWidth() / 2 - (popup.getWidth() / 2);
		    y = b.getMaxY() - popup.getHeight();
		    break;
		    
		case BOTTOM_RIGHT:
		    x = b.getMaxX()- popup.getWidth();
		    y = b.getMaxY()- popup.getHeight();
		    break;
		//</editor-fold>
		//<editor-fold defaultstate="collapsed" desc="top cases">
		case TOP_LEFT:
		    x = b.getMinX();
		    y = b.getMinY();
		    break;
		    
		case TOP_CENTER:
		    x = b.getMinX() + b.getWidth() / 2 - (popup.getWidth() / 2);
		    y = b.getMinY();
		    break;
		    
		case TOP_RIGHT:
		    x = b.getMaxX() - popup.getWidth();
		    y = b.getMinY();
		    break;
		//</editor-fold>
		//<editor-fold defaultstate="collapsed" desc="left, right, center cases">
		case LEFT:
		    x = b.getMinX();
		    y = b.getMinY() + b.getHeight() / 2 - (popup.getHeight() / 2);
		    break;
		case RIGHT:
		    x = b.getMaxX() - popup.getWidth();
		    y = b.getMinY() + b.getHeight() / 2 - (popup.getHeight() / 2);
		    break;

		default:
		    x = b.getMinX() + b.getWidth() / 2 - (popup.getWidth() / 2);
		    y = b.getMinY() + b.getHeight() / 2 - (popup.getHeight() / 2);
		    break;
		//</editor-fold>
	    }
	    
	    popup.setX(x + popup.getToast().getOffset().getX());
	    popup.setY(y + popup.getToast().getOffset().getY());
	}

	@Override
	public void changed(ObservableValue observable, Object oldValue, Object newValue) {
	    synchronized(popups) {
		for(ToastPopup<Node> popup : popups.values()) {
		    updateLocation(popup);
		}
	    }
	}

    }

}
