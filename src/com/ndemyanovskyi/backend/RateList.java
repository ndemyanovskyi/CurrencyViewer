/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.backend;

import com.ndemyanovskyi.backend.Rate.Field;
import com.ndemyanovskyi.collection.list.DefaultList;
import com.ndemyanovskyi.time.Interval;
import com.ndemyanovskyi.time.Period;
import java.lang.reflect.Array;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;

/**
 *
 * @author Назарій
 */
public interface RateList<R extends Rate> extends ObservableList<R>, DefaultList<R> {
    
    public Period getPeriod();
    public Bank<R> getBank();
    public Currency getCurrency();
    public RateList<R> subList(Interval interval);
    
    public default RateList<R> subList(Collection<LocalDate> dates) {
        return subList(Period.of(dates));
    }

    @Override
    public default RateList<R> subList(int fromIndex, int toIndex) {
        return subList(getPeriod().subList(fromIndex, toIndex));
    }
    
    public default <N extends Number> XYChart.Series<N, Float> buildSeries(Function<R, N> xFactory, Function<R, Float> yFactory) {
	XYChart.Series<N, Float> series = new XYChart.Series<>();
	for(R rate : this) {
	    series.getData().add(new XYChart.Data<>(
                    xFactory.apply(rate), yFactory.apply(rate)));
	}
	return series;
    }
    
    public default R get(LocalDate date) {
        int index = getPeriod().indexOf(date);
        return index >= 0 ? get(index) : null;
    }

    @Override
    public default boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    @Override
    public default boolean isEmpty() {
        return size() == 0;
    }
    
    public default R first() {
        return !isEmpty() ? get(0) : null;
    }
    
    public default R last() {
        return !isEmpty() ? get(size() - 1) : null;
    }

    @Override
    public default boolean containsAll(Collection<?> c) {
        for(Object o : c) {
            if(!contains(o)) return false;
        }
        return true;
    }

    @Override
    public default Object[] toArray() {
        Object[] array = new Object[size()];
        int index = 0;
        for(R rate : this) {
            array[index++] = rate;
        }
        return array;
    }

    @Override
    public default <T> T[] toArray(T[] array) {
        array = array.length >= size()
                ? array
                : (T[]) Array.newInstance(array.getClass().getComponentType(), size());
        
        int index = 0;
        for(R rate : this) {
            array[index++] = (T) rate;
        }
        return array;
    }

    /*@Override
    public default int indexOf(Object o) {
        if(o != null && o instanceof Rate) {
            Rate rate = (Rate) o;
            int index = per.indexOf(rate.getDate());
            if(index != -1 && rate.equals(get(index))) {
                return index;
            }
        }
        return -1;
    }*/

    @Override
    public default ListIterator<R> listIterator() {
        return listIterator(0);
    }

    @Override
    public default Iterator<R> iterator() {
        return listIterator(0);
    }

    @Override
    public default int lastIndexOf(Object o) {
        return indexOf(o);
    }
    
    public default boolean is(Bank<?> bank, Currency currency, Period period) {
	return is(bank, currency) && getPeriod().equals(period);
    }
    
    public default boolean is(Bank<?> bank, Currency currency) {
	return getBank().equals(bank) && getCurrency().equals(currency);
    }
    
    public static int hashCode(RateList<?> list) {
        int hash = 1;
        hash = 31 * hash + list.getBank().hashCode();
        hash = 31 * hash + list.getCurrency().hashCode();
        hash = 31 * hash + DefaultList.hashCode(list);
        return hash;
    }
    
    public static boolean equals(RateList<?> list, Object o) {
        if(list == o) return true;
        if(o == null) return false;
        if(!(o instanceof RateList)) return false;
        
        RateList<?> other = (RateList<?>) o;
        if(list.size() != other.size()) return false;
        if(!list.getBank().equals(other.getBank())) return false;
        if(!list.getCurrency().equals(other.getCurrency())) return false;
        return DefaultList.equals(list, o);
    }
    
    public static <R extends Rate> String toString(RateList<R> list) {
        StringBuilder b = new StringBuilder();
        b.append("RateList [").
                append("bank: ").append(list.getBank()).append(", ").
                append("currency: ").append(list.getCurrency()).append(", ");
       
        b.append("rates: [");
        Iterator<R> it = list.iterator();
        while(it.hasNext()) {
            R rate = it.next();
            b.append("[date: ").append(rate.getDate()).append(", ");
            Iterator<Field> fieldIt = rate.getFields().iterator();
            while(fieldIt.hasNext()) {
                Field field = fieldIt.next();
                b.append(field.name().toLowerCase()).append(": ").append(rate.get(field));
                if(fieldIt.hasNext()) b.append(", ");
            }
            b.append("]");
            if(it.hasNext()) b.append(", ");
        }
        b.append("]]");
        return b.toString();
    }
    
    //<editor-fold defaultstate="collapsed" desc="Unsupported">
    @Override
    public default R set(int index, R element) {
        throw new UnsupportedOperationException("set");
    }
    
    @Override
    public default boolean setAll(Collection<? extends R> col) {
        throw new UnsupportedOperationException("setAll");
    }
    
    @Override
    public default boolean setAll(R... elements) {
        throw new UnsupportedOperationException("setAll");
    }
    
    @Override
    public default boolean addAll(Collection<? extends R> c) {
        throw new UnsupportedOperationException("addAll");
    }
    
    @Override
    public default boolean addAll(R... elements) {
        throw new UnsupportedOperationException("addAll");
    }
    
    @Override
    public default boolean addAll(int index, Collection<? extends R> c) {
        throw new UnsupportedOperationException("addAll");
    }
    
    @Override
    public default void add(int index, R element) {
        throw new UnsupportedOperationException("add");
    }
    
    @Override
    public default R remove(int index) {
        throw new UnsupportedOperationException("remove");
    }
    
    @Override
    public default void remove(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("remove");
    }
    
    @Override
    public default boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("removeAll");
    }
    
    @Override
    public default boolean removeAll(R... elements) {
        throw new UnsupportedOperationException("removeAll");
    }
    
    @Override
    public default boolean removeIf(Predicate<? super R> filter) {
        throw new UnsupportedOperationException("removeIf");
    }
    
    @Override
    public default void replaceAll(UnaryOperator<R> operator) {
        throw new UnsupportedOperationException("replaceAll");
    }
    
    @Override
    public default boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("retainAll");
    }
    
    @Override
    public default void clear() {
        throw new UnsupportedOperationException("clear");
    }
    
    @Override
    public default boolean retainAll(R... elements) {
        throw new UnsupportedOperationException("retainAll");
    }
    
    @Override
    public default boolean add(R element) {
        throw new UnsupportedOperationException("add");
    }
    
    @Override
    public default boolean remove(Object obj) {
        throw new UnsupportedOperationException("remove");
    }
//</editor-fold>
    
}
