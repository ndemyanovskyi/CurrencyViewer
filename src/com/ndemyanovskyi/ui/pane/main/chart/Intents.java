/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.ui.pane.main.chart;

import com.ndemyanovskyi.app.localization.binding.ResourceBindings;
import com.ndemyanovskyi.backend.Bank;
import com.ndemyanovskyi.backend.Currency;
import com.ndemyanovskyi.backend.Rate;
import com.ndemyanovskyi.backend.Rate.Field;
import com.ndemyanovskyi.collection.set.ArrayListedSet;
import com.sun.javafx.collections.ObservableSetWrapper;
import java.util.Iterator;
import javafx.beans.property.ReadOnlyProperty;

/**
 *
 * @author Назарій
 */
public final class Intents extends ObservableSetWrapper<Intent<?>> {
    
    private static final ReadOnlyProperty<Number> MAX_SERIES_COUNT = 
            ResourceBindings.numbers().get("max_series_count");
    
    public Intents() {
        super(new ArrayListedSet<>());
        MAX_SERIES_COUNT.addListener(p -> truncate());
    }
    
    private void truncate() {
        int difference = size() - MAX_SERIES_COUNT.getValue().intValue();
        Iterator<Intent<?>> it = iterator();
        while(difference-- > 0 && it.hasNext()) {
            it.next();
            it.remove();
        }
    }
    
    private void truncateForAdd() {
        int difference = size() - MAX_SERIES_COUNT.getValue().intValue();
        Iterator<Intent<?>> it = iterator();
        while(difference-- >= 0 && it.hasNext()) {
            Intent<?> intent = it.next();
            it.remove();
	    intent.setAttached(false);
        }
    }

    public <R extends Rate> Intent<R> add(Bank<R> bank, Currency currency) {
        return add(bank, currency, Rate.RATE);
    }

    public <R extends Rate> Intent<R> add(Bank<R> bank, Currency currency, Field field) {
        Intent<R> intent = get(bank, currency, field);
        if(intent == null) {
            intent = new Intent<>(bank, currency, field);
            intent.setAttached(true);
            add(intent);
        }
        return intent;
    }

    @Override
    public boolean add(Intent<?> o) {
        truncateForAdd();
        Intent<?> intent = get(o);
        if(intent != null) remove(intent);
        o.setAttached(true);
        return super.add(o);
    }
     
    public Intent<?> get(Object o) {
        if(o != null && o instanceof Intent) {
            Intent<?> other = (Intent<?>) o;
            return get(other.getBank(), other.getCurrency(), other.getField());
        }
        return null;
    }
     
    public <R extends Rate> Intent<R> get(Bank<R> bank, Currency currency, Field field) {
        for(Intent<?> intent : this) {
            if(intent.is(bank, currency, field)) {
                return (Intent<R>) intent;
            }
        }
        return null;
    }
     
    public <R extends Rate> Intent<R> remove(Bank<R> bank, Currency currency, Field field) {
        Iterator<Intent<?>> it = iterator();
        while(it.hasNext()) {
            Intent<?> intent = it.next();
            if(intent.is(bank, currency, field)) {
                it.remove();
                intent.setAttached(false);
                return (Intent<R>) intent;
            }
        }
        return null;
    }

    @Override
    public boolean remove(Object o) {
        if(o != null && o instanceof Intent) {
            Intent<?> other = (Intent<?>) o;
            return remove(other.getBank(), other.getCurrency(), other.getField()) != null;
        }
        return false;
    }
    
    
}
