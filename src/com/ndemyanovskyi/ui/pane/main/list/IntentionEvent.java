/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.pane.main.list;

import java.util.Objects;
import javafx.event.Event;
import javafx.event.EventType;

/**
 *
 * @author Назарій
 */
public class IntentionEvent extends Event {

    public static final EventType<IntentionEvent> INTENTION = new EventType<>("INTENTION");

    private final Intention intention;

    public IntentionEvent(Object source, Intention intention) {
        super(source, NULL_SOURCE_TARGET, INTENTION);
        this.intention = Objects.requireNonNull(intention, "intention");
    }

    public Intention getIntention() {
        return intention;
    }

    @Override
    public String toString() {
        return String.format(
                "IntentionEvent [source: %s, intention: %s]", 
                getSource(), getIntention());
    }

}
