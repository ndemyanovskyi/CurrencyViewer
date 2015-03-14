/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.backend.loader;

import com.sun.javafx.collections.ObservableListWrapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import org.apache.commons.collections4.ListUtils;

/**
 *
 * @author Назарій
 */
public abstract class SimpleSubscribedLoader<S, R> extends SimpleLoader<Map<S, R>> implements SubscribedLoader<S, R> {
    
    private final Subscribers locks = new Subscribers();

    public SimpleSubscribedLoader() {
        setResult(createDefaultResult());
    }

    @Override
    public ObservableList<SubscribedLoader.Lock<S>> getSubscribedLocks() {
        return (ObservableList) locks;
    }
    
    @Override
    void onFinishForSubscribedLoader() {
        locks.clear();
    }
    
    protected Map<S, R> createDefaultResult() {
        return new HashMap<>();
    }
    
    protected void onSubscribe(S subscriber) {}
    
    protected R extractSubscribedResult(Map<S, R> map, S subscriber) {
        return map != null ? map.get(subscriber) : null;
    }

    @Override
    public R subscribe(S subscriber) throws IOException {
        if(!isFinished()) {
            Lock<S> lock;
            synchronized(this) {
                lock = new Lock<>(subscriber);
                locks.unsafeAdd(lock);

                onSubscribe(subscriber);
                execute();
            }
            synchronized(lock) {
                while(lock.isLocked() && !isFinished()) {
                    try {
                        lock.wait();
                    } catch(InterruptedException ex) {}
                }
            }
        }
        
        IOException ex = getException();
        if(ex != null) throw ex;
        return extractSubscribedResult(getResult(), subscriber);
    }
    
    private final class Subscribers extends ObservableListWrapper<Lock<S>> {

        public Subscribers() {
            super(ListUtils.synchronizedList(new ArrayList<>()));
            addListener((Change<? extends Lock<S>> change) -> {
                while(change.next()) {
                    if(change.wasRemoved()) {
                        change.getRemoved().forEach(lock -> {
                            synchronized(lock) {
                                lock.setLocked(false);
                                lock.notifyAll();
                            }
                        });
                    }
                }
            });
        }

        @Override
        public void add(int index, Lock<S> element) {
            throw new UnsupportedOperationException(
                    "Adding into subscribers list allow only internally.");
        }

        private boolean unsafeAdd(Lock<S> e) {
            if(e != null) {
                synchronized(this) {
                    unsafeAdd(size(), e);
                }
                return true;
            } else {
                return false;
            }
        }

        private void unsafeAdd(int index, Lock<S> e) {
            if(e != null) {
                super.add(index, e);
            } 
        }

    }
    
    private static final class Lock<S> implements SubscribedLoader.Lock<S> {

        private final S subscriber;
        private boolean locked = true;

        public Lock(S subscriber) {
            this.subscriber = subscriber;
        }

        void setLocked(boolean locked) {
            this.locked = locked;
        }

        @Override
        public boolean isLocked() {
            return locked;
        }

        @Override
        public S getSubscriber() {
            return subscriber;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + Objects.hashCode(this.subscriber);
            hash = 37 * hash + (this.locked ? 1 : 0);
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            if(o == this) return true;
            if(o == null || !(o instanceof SubscribedLoader.Lock)) return false;
            final SubscribedLoader.Lock<?> other = (SubscribedLoader.Lock<?>) o;
            return getSubscriber().equals(other.getSubscriber())
                    && isLocked() == other.isLocked();
        }

        @Override
        public String toString() {
            return "Lock [" + "subscriber: " + subscriber + ", locked: " + locked + ']';
        }

    }
    
}