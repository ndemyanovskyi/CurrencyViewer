/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.backend.loader;

import com.ndemyanovskyi.throwable.function.ThrowableSupplier;
import java.io.IOException;

/**
 *
 * @author Назарій
 */
public interface Loader<R> {
    
    public R load() throws IOException;
    public R getResult();
    public IOException getException();
    public boolean isStarted();
    public boolean isFinished();
    public R sync() throws IOException;
    
    public default boolean isRunning() {
        return isStarted() && !isFinished();
    }
    
    public static <R> Loader<R> of(ThrowableSupplier<? extends R, ? extends IOException> load) {
        return new SimpleLoader<R>() {

            @Override
            public R load() throws IOException {
                return load.get();
            }
            
        };
    }
    
}