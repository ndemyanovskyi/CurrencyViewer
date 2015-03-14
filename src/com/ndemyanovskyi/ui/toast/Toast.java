/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.toast;

import com.ndemyanovskyi.util.number.Numbers.Integers;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Window;

public abstract class Toast<T> {

    static final javafx.util.Duration ANIMATION_DURATION = javafx.util.Duration.seconds(0.3);

    private final T owner;
    private Node node;
    private Alignment alignment = Alignment.CENTER;
    private Duration duration = Duration.SHORT;
    private Offset offset = Offset.ZERO;

    boolean shown;
    boolean showing;
    boolean inQueue;
    boolean cancelled;

    Toast(T owner) {
	this.owner = Objects.requireNonNull(owner, "owner");
    }

    public Alignment getAlignment() {
	return alignment;
    }

    public T getOwner() {
	return owner;
    }

    public void setAlignment(Alignment alignment) {
	this.alignment = Objects.requireNonNull(alignment, "alignment");
    }

    public Duration getDuration() {
	return duration;
    }

    public void setDuration(Duration duration) {
	this.duration = Objects.requireNonNull(duration, "duration");
    }

    public Offset getOffset() {
	return offset;
    }

    public void setOffset(Offset offset) {
	this.offset = Objects.requireNonNull(offset, "offset");
    }

    public void setOffset(double x, double y) {
	this.offset = Offset.of(x, y);
    }

    public void setNode(Node node) {
	this.node = node;
    }

    public Node getNode() {
	return node;
    }

    public boolean isShown() {
	return shown;
    }

    public boolean isShowing() {
	return showing;
    }

    public boolean isHidden() {
	return !isShowing();
    }

    public boolean isInQueue() {
	return inQueue;
    }

    public boolean isCancelled() {
	return cancelled;
    }

    @Override
    public int hashCode() {
	int hash = 7;
	hash = 89 * hash + Objects.hashCode(this.owner);
	hash = 89 * hash + Objects.hashCode(this.node);
	hash = 89 * hash + Objects.hashCode(this.alignment);
	hash = 89 * hash + Objects.hashCode(this.duration);
	hash = 89 * hash + Objects.hashCode(this.offset);
	hash = 89 * hash + (this.shown ? 1 : 0);
	hash = 89 * hash + (this.showing ? 1 : 0);
	hash = 89 * hash + (this.inQueue ? 1 : 0);
	hash = 89 * hash + (this.cancelled ? 1 : 0);
	return hash;
    }

    @Override
    public boolean equals(Object obj) {
	if(obj == null) return false;
	if(!(obj instanceof Toast)) return false;
	final Toast<?> other = (Toast<?>) obj;
	if(!Objects.equals(this.owner, other.owner)) return false;
	if(!Objects.equals(this.node, other.node)) return false;
	if(this.alignment != other.alignment) return false;
	if(this.duration != other.duration) return false;
	if(!Objects.equals(this.offset, other.offset)) return false;
	if(this.shown != other.shown) return false;
	if(this.showing != other.showing) return false;
	if(this.inQueue != other.inQueue) return false;
	if(this.cancelled != other.cancelled) return false;
	return true;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append("Toast [owner=").append(owner).
		append(", node=").append(node).
		append(", alignment=").append(alignment).
		append(", duration=").append(duration).
		append(", ").append(offset);
	builder.append(", states=");
	List<String> states = new ArrayList<>();
	if(shown) states.add("shown");
	if(showing) states.add("showing");
	if(cancelled) states.add("cancelled");
	if(inQueue) states.add("inQueue");

	builder.append(states);
	return builder.toString();
    }

    public enum Alignment {

	CENTER, LEFT, RIGHT,
	BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT,
	TOP_LEFT, TOP_CENTER, TOP_RIGHT;
    }

    public final void show() {
	if(isCancelled()) {
	    throw new IllegalStateException("Toast alredy cancelled.");
	}
	if(isShown()) {
	    throw new IllegalStateException("Toast alredy shown.");
	}
	if(isInQueue()) {
	    throw new IllegalStateException("Toast alredy in queue.");
	}

	onShow();
	inQueue = true;
    }

    protected void onShow() {
    }

    protected void onCancel() {
    }

    public final void cancel() {
	if(isCancelled()) {
	    throw new IllegalStateException("Toast alredy cancelled.");
	}

	cancelled = true;
	inQueue = false;
	onCancel();
    }

    public final static class Offset {

	public static final Offset ZERO = new Offset(0, 0);
	public static final Offset ONE = new Offset(1, 1);
	public static final Offset TEN = new Offset(10, 10);

	private final double x, y;

	private Offset(double x, double y) {
	    this.x = x;
	    this.y = y;
	}

	public static Offset of(double x, double y) {
	    if(ZERO.is(x, y)) return ZERO;
	    if(ONE.is(x, y)) return ONE;
	    if(TEN.is(x, y)) return TEN;
	    return new Offset(x, y);
	}

	public double getY() {
	    return y;
	}

	public double getX() {
	    return x;
	}

	public boolean is(double x, double y) {
	    return Double.compare(this.x, x) == 0 &&
		    Double.compare(this.y, y) == 0;
	}

	@Override
	public int hashCode() {
	    int hash = 3;
	    hash = 37 * hash + (int) 
		    (Double.doubleToLongBits(this.x) ^ 
		    (Double.doubleToLongBits(this.x) >>> 32));
	    hash = 37 * hash + (int) 
		    (Double.doubleToLongBits(this.y) ^ 
		    (Double.doubleToLongBits(this.y) >>> 32));
	    return hash;
	}

	@Override
	public boolean equals(Object obj) {
	    if(obj == null) return false;
	    if(getClass() != obj.getClass()) return false;
	    final Offset other = (Offset) obj;
	    return Double.compare(this.x, other.x) == 0 
		    && Double.compare(this.y, other.y) == 0;
	}

	@Override
	public String toString() {
	    return "Offset [X=" + x + ", Y=" + y + "]";
	}

    }

    public enum Duration {

	SHORT(2000), LONG(5000);

	private final int millis;

	private Duration(int millis) {
	    this.millis = Integers.require(millis, m -> m > 0);
	}

	public int inMillis() {
	    return millis;
	}

    }

    /*

    public static Toast<Screen> of() {
	return new ScreenToast();
    }
    
    public static Toast<Screen> of(String text) {
	return of(text, Alignment.CENTER);
    }

    public static Toast<Screen> of(String text, Alignment alignment) {
	return of(text, Duration.SHORT, alignment);
    }

    public static Toast<Screen> of(String text, Duration duration) {
	return of(text, duration, Alignment.CENTER);
    }

    public static Toast<Screen> of(String text, Duration duration, Alignment alignment) {
	ScreenToast t = new ScreenToast();
	t.setNode(createText(text));
	t.setAlignment(alignment);
	t.setDuration(duration);
	return t;
    }*/

    public static Toast<Node> of(Node owner, String text) {
	return of(owner, text, Alignment.CENTER);
    }

    public static Toast<Node> of(Node owner, String text, Alignment alignment) {
	return of(owner, text, Duration.SHORT, alignment);
    }

    public static Toast<Node> of(Node owner, String text, Duration duration) {
	return of(owner, text, duration, Alignment.CENTER);
    }

    public static Toast<Node> of(Node owner, String text, Duration duration, Alignment alignment) {
	NodeToast t = new NodeToast(owner);
	t.setAlignment(alignment);
	t.setDuration(duration);
	
	Label label = createText(text);
	label.setMaxWidth(owner.getLayoutBounds().getWidth() - 20);
	t.setNode(label);
	return t;
    }

    public static Toast<Window> of(Window window, String text) {
	return of(window, text, Alignment.CENTER);
    }

    public static Toast<Window> of(Window window, String text, Alignment alignment) {
	return of(window, text, Duration.SHORT, alignment);
    }

    public static Toast<Window> of(Window window, String text, Duration duration) {
	return of(window, text, duration, Alignment.CENTER);
    }

    public static Toast<Window> of(Window window, String text, Duration duration, Alignment alignment) {
	WindowToast t = new WindowToast(window);
	t.setAlignment(alignment);
	t.setDuration(duration);
	
	Label label = createText(text);
	label.setMaxWidth(window.getWidth() - 45);
	t.setNode(label);
	return t;
    }

    public static Toast<Window> of(Window window) {
	return new WindowToast(window);
    }

    public static Toast<Node> of(Node owner) {
	return new NodeToast(owner);
    }

    private static Label createText(String text) {
	Label l = new Label(text);
	l.setTextFill(Color.GHOSTWHITE);
	l.setPadding(new Insets(5));
	l.setWrapText(true);
        l.setTextAlignment(TextAlignment.CENTER);
	return l;
    }

    private static Label createText(ObservableValue<String> text) {
	Label l = new Label();
        l.textProperty().bind(text);
	l.setTextFill(Color.GHOSTWHITE);
	l.setPadding(new Insets(5));
	l.setWrapText(true);
        l.setTextAlignment(TextAlignment.CENTER);
	return l;
    }

    public static Toast<Node> getCurrent(Node node) {
	return NodeToast.getCurrent0(node);
    }

    public static Toast<Window> getCurrent(Window window) {
	return WindowToast.getCurrent0(window);
    }

    /*public static Toast<Screen> getCurrent() {
	return ScreenToast.getCurrentToast();
    }*/
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        
        private Object owner;
        private Node node;
        
        private Alignment alignment = Alignment.CENTER;
        private Duration duration = Duration.SHORT;
        private Offset offset = Offset.ZERO;
        
        private Builder() {}

        public Builder setAlignment(Alignment alignment) {
            this.alignment = Objects.requireNonNull(alignment, "alignment");
            return this;
        }

        public Builder setDuration(Duration duration) {
            this.duration = Objects.requireNonNull(duration, "duration");
            return this;
        }

        public Builder setText(String text) {
            return setNode(createText(text));
        }

        public Builder setText(ObservableValue<String> textProperty) {
            return setNode(createText(textProperty));
        }

        public Builder setNode(Node node) {
            this.node = Objects.requireNonNull(node, "node");
            return this;
        }

        public Builder setOffset(Offset offset) {
            this.offset = Objects.requireNonNull(offset, "offset");
            return this;
        }

        public Builder setOffset(double x, double y) {
            return setOffset(Offset.of(x, y));
        }

        public Builder setOwner(Object owner) {
            Objects.requireNonNull(owner, "owner");
            
            if(owner instanceof Window 
                    || owner instanceof Node) {
                this.owner = owner;
            } else {
                throw new IllegalArgumentException(
                        "Owner must be instance of Window or Node.");
            }
            return this;
        }

        public Alignment getAlignment() {
            return alignment;
        }

        public Duration getDuration() {
            return duration;
        }

        public Node getNode() {
            return node;
        }

        public Offset getOffset() {
            return offset;
        }

        public Object getOwner() {
            return owner;
        }
        
        public Toast<?> show() {
            Toast<?> toast = build();
            toast.show();
            return toast;
        }
        
        public Toast<?> build() {
            Objects.requireNonNull(owner, "owner");
            Objects.requireNonNull(node, "node");
            
            Toast<?> toast;
            if(owner instanceof Node) {
                toast = Toast.of((Node) owner);
            } else {
                toast = Toast.of((Window) owner);
            }
            
            toast.setAlignment(alignment);
            toast.setOffset(offset);
            toast.setDuration(duration);
            toast.setNode(node);
            
            return toast;
        }
        
    }

}
