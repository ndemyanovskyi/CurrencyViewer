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
public abstract class AbstractLoader<R> implements Loader<R> {
    
    private boolean started;
    private boolean finished;
    private Thread thread;
    private R result;
    private IOException exception;
    
    protected abstract void execute();

    protected void setException(IOException exception) {
        this.exception = exception;
    }

    protected void setFinished(boolean finished) {
        this.finished = finished;
    }

    protected void setResult(R result) {
        this.result = result;
    }

    protected void setStarted(boolean started) {
        this.started = started;
    }

    protected void setThread(Thread thread) {
        this.thread = thread;
    }

    @Override
    public IOException getException() {
        return exception;
    }

    @Override
    public R getResult() {
        return result;
    }

    protected Thread getThread() {
        return thread;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
    
    protected void onStart() {}
    
    protected void onFinish() {}
    
}
