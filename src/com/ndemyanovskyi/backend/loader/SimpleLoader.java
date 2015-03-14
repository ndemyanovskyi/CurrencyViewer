/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.backend.loader;

import java.io.IOException;

/**
 *
 * @author Назарій
 */
public abstract class SimpleLoader<R> extends AbstractLoader<R> {

    private final Object lock = new Object();

    @Override
    protected void execute() {
        synchronized(this) {
            if(getThread() == null) {
                setThread(new Thread(() -> {
                    setStarted(true);
                    onStart();
                    try {
                        setResult(load());
                    } catch(IOException ex) {
                        setException(ex);
                    }
                    setFinished(true);
                    onFinishForSubscribedLoader();
                    onFinish();
                    synchronized(lock) {
                        lock.notifyAll();
                    }
                }));
               getThread().start();
            }
        }
    }
    
    void onFinishForSubscribedLoader() {}

    protected Object getLock() {
        return lock;
    }

    @Override
    public R sync() throws IOException {
        execute();
        
        synchronized(lock) {
            while(!isFinished()) {
                try {
                    lock.wait();
                } catch(InterruptedException ex) {}
            }
        }
        
        IOException ex = getException();
        if(ex != null) throw ex;
        return getResult();
    }
    
}
