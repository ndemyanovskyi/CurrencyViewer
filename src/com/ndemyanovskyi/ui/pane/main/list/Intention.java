/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.pane.main.list;

import com.ndemyanovskyi.ui.pane.main.chart.Intent;
import java.util.Objects;

/**
 *
 * @author Назарій
 */
public class Intention {
    
    public static enum Action {
        REMOVE, ADD;
    }
    
    private final Action action;
    private final Intent<?> intent;

    public Intention(Action action, Intent<?> intent) {
        this.action = Objects.requireNonNull(action, "action");
        this.intent = Objects.requireNonNull(intent, "intent");
    }

    public Intent<?> getIntent() {
        return intent;
    }

    public Action getAction() {
        return action;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.action);
        hash = 97 * hash + Objects.hashCode(this.intent);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) return false;
        if(!(obj instanceof Intention)) return false;
        final Intention other = (Intention) obj;
        return this.action == other.getAction() 
                && Objects.equals(this.intent, other.getAction());
    }

    @Override
    public String toString() {
        return "Intention [" + "action: " + action + ", intent: " + intent + ']';
    }
    
}
