/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.app;

import java.util.Objects;
import javafx.event.Event;

/**
 *
 * @author Назарій
 */
public final class ErrorEvent extends Event {
    
    private static final long serialVersionUID = 1234234093285029354L;
    
    private final Throwable parentCause; 
    private final Throwable cause; 

    public ErrorEvent(Thread thread, Throwable cause) {
        this(thread, cause, cause);
    }

    public ErrorEvent(Thread thread, Throwable cause, Throwable parentCause) {
        super(Objects.requireNonNull(thread, "thread"), NULL_SOURCE_TARGET, ANY);
        this.cause = Objects.requireNonNull(cause, "cause");
        this.parentCause = Objects.requireNonNull(parentCause, "parentCause");
    }

    public Throwable getCause() {
        return cause;
    }

    public Throwable getParentCause() {
        return cause;
    }

    @Override
    public Thread getSource() {
        return (Thread) super.getSource();
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = 31 * hash + Objects.hashCode(getCause());
        hash = 31 * hash + Objects.hashCode(getSource());
        hash = 31 * hash + Objects.hashCode(getTarget());
        hash = 31 * hash + Objects.hashCode(getEventType());
        hash = 31 * hash + Objects.hashCode(getParentCause());
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null || !(o instanceof ErrorEvent)) return false;
        ErrorEvent other = (ErrorEvent) o;
        return getCause().equals(other.getCause())
                && getSource().equals(other.getSource())
                && getTarget().equals(other.getTarget())
                && getEventType().equals(other.getEventType())
                && getParentCause().equals(other.getParentCause());
    }

    @Override
    public String toString() {
        return "ErrorEvent [" + "parentCause=" + parentCause + 
                ", cause=" + cause + ", source=" + getSource() + ']';
    }
    
}
