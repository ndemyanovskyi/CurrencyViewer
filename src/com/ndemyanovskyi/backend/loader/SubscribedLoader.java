/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.backend.loader;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import javafx.collections.ObservableList;

/**
 *
 * @author Назарій
 */
public interface SubscribedLoader<S, R> extends Loader<Map<S, R>> {
    
    public R subscribe(S subscriber) throws IOException;
    public ObservableList<? extends Lock<S>> getSubscribedLocks();
    
    public interface Lock<S> {
        
        public boolean isLocked();
        public S getSubscriber();
        
        public default boolean is(S subscriber) {
            return Objects.equals(getSubscriber(), subscriber);
        }
        
    }
    
}
