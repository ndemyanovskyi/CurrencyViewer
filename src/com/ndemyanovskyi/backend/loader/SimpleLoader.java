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
    
    private RuntimeException runtimeException;

    @Override
    protected void execute() {
        synchronized(this) {
            if(getThread() == null) {
                setThread(new Thread(() -> {
                    setStarted(true);
                    onStart();
                    try {
                        setResult(load());
                    } catch(RuntimeException ex) {
			runtimeException = ex;
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

    public RuntimeException getRuntimeException() {
	return runtimeException;
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
	
        throwIfNeeded();
        return getResult();
    }
    
    private void throwIfNeeded() throws IOException {
        IOException ioEx = getException();
        if(ioEx != null) throw ioEx;
        RuntimeException rtEx = getRuntimeException();
        if(rtEx != null) throw rtEx;
    }
    
}
