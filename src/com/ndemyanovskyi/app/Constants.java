/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.app;

import com.ndemyanovskyi.map.unmodifiable.UnmodifiableMap;
import com.ndemyanovskyi.time.Period;
import com.ndemyanovskyi.util.Convert;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;

/**
 *
 * @author Назарій
 */
public class Constants extends UnmodifiableMap<String, Constants.Constant<?>> {
    
    private static final Constants INSTANCE = new Constants();
    
    public static final Constant<Period> MINIMAL_PERIOD = new Constant<Period>("MINIMAL_PERIOD") {
        
        private Timer timer;

        @Override
        void init() {
            Settings.STORED_DATA_YEARS_COUNT.addListener(p -> updateValue());
            updateValue();
            TimerTask task = new TimerTask() {

                @Override
                public void run() {
                    updateValue();
                }
                
            };
            timer = new Timer();
            timer.scheduleAtFixedRate(
                    task, Convert.toDate(LocalDate.now().plusDays(1)), 
                    TimeUnit.DAYS.toMillis(1));
        }
        
        private void updateValue() {
            LocalDate to = LocalDate.now();
            LocalDate from = to.minusYears(Settings.STORED_DATA_YEARS_COUNT.get());
            modifiableProperty().set(Period.ofInclusive(from, to));
        }
        
    };

    public Constants() {
        super(new HashMap<>());
    }

    public static Constants getInstance() {
        return INSTANCE;
    }
    
    public static class Constant<T> implements ReadOnlyProperty<T> {
        
        private final ObjectProperty<T> modifiable;

        Constant(String name) {
            this(name, null);
        }

        Constant(String name, T initialValue) {
            modifiable = new SimpleObjectProperty<>(this, name);
            INSTANCE.base().put(name, this);
            init();
        }

        ObjectProperty<T> modifiableProperty() {
            return modifiable;
        }
        
        void init() {}

        @Override
        public Constants getBean() {
            return INSTANCE;
        }

        @Override
        public String getName() {
            return modifiable.getName();
        }

        @Override
        public T getValue() {
            return modifiable.get();
        }

        @Override
        public void addListener(ChangeListener<? super T> listener) {
            modifiable.addListener(listener);
        }

        @Override
        public void removeListener(ChangeListener<? super T> listener) {
            modifiable.removeListener(listener);
        }

        @Override
        public void addListener(InvalidationListener listener) {
            modifiable.addListener(listener);
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            modifiable.removeListener(listener);
        }
        
    }
    
}
