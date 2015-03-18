/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.app;

import com.ndemyanovskyi.derby.Database;
import com.ndemyanovskyi.throwable.RuntimeIOException;
import com.ndemyanovskyi.throwable.RuntimeSQLException;
import java.sql.SQLException;
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
    
    public static ErrorEvent create(Throwable cause) {
        return create(Thread.currentThread(), cause, cause);
    }
    
    public static ErrorEvent create(Thread thread, Throwable cause) {
        return create(thread, cause, cause);
    }
    
    private static ErrorEvent create(Thread thread, Throwable cause, Throwable parentCause) {
        if(cause instanceof ExceptionInInitializerError
                || cause instanceof RuntimeIOException) {
            return create(thread, cause.getCause(), parentCause);
        }
        if(cause instanceof RuntimeSQLException) {
            SQLException sqlEx = Database.Utils.extractCause(cause);
            if(sqlEx != null) return create(thread, sqlEx, parentCause);
        }
        return new ErrorEvent(thread, cause, parentCause);
    }
    
}
