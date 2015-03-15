/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ndemyanovskyi.backend;

import com.ndemyanovskyi.time.Interval;
import com.ndemyanovskyi.time.Period;
import com.ndemyanovskyi.time.Period.Builder;
import com.sun.javafx.collections.ObservableListWrapper;
import java.time.LocalDate;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;


final class RateListImpl<R extends Rate> extends ObservableListWrapper<R> implements RateList<R> {
    
    private static final long serialVersionUID = 2342423565487561L;
    
    private final Bank<R> bank;
    private final Currency currency;
    private final Builder periodBuilder = Period.builder();
    private final Modifier<R> modifier = new Modifier<>();
    
    RateListImpl(Bank<R> bank, Currency currency) {
        super(new ArrayList<>());
	this.bank = Objects.requireNonNull(bank, "bank");
	this.currency = Objects.requireNonNull(currency, "currency");
    }
    
    RateListImpl(Bank<R> bank, Currency currency, Collection<R> rates) {
        this(bank, currency);
        modifier().addAllOrThrow(rates);
    }

    @Override
    public Period getPeriod() {
	return getPeriodBuilder().build();
    }

    private Builder getPeriodBuilder() {
        return periodBuilder;
    }

    @Override
    public Currency getCurrency() {
	return currency;
    }

    @Override
    public Bank<R> getBank() {
	return bank;
    }

    @Override
    public int indexOf(Object o) {
        if(o != null && o instanceof Rate) {
            Rate rate = (Rate) o;
            int index = periodBuilder.indexOf(rate.getDate());
            if(index != -1 && rate.equals(get(index))) {
                return index;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        return RateList.super.lastIndexOf(o);
    }

    @Override
    public RateList<R> subList(Interval interval) {
        return new SubRateList<>(this, Period.of(interval));
    }

    @Override
    public RateList<R> subList(int fromIndex, int toIndex) {
        return RateList.super.subList(fromIndex, toIndex);
    }
    
    Modifier<R> modifier() {
        return modifier;
    }

    @Override
    public boolean equals(Object o) {
        return RateList.equals(this, o);
    }

    @Override
    public int hashCode() {
        return RateList.hashCode(this);
    }

    @Override
    public String toString() {
        return RateList.toString(this);
    }
    
    private static class SubRateList<R extends Rate> extends AbstractList<R> implements RateList<R> {
        
        private final RateListImpl<R> list;
        private final Period period;
        
        public SubRateList(RateListImpl<R> list, Period period) {
            this.list = Objects.requireNonNull(list, "list");
            this.modCount = list.modCount;
            this.period = list.getPeriod().cross(
                    Objects.requireNonNull(period, "period"));
        }

        @Override
        public Period getPeriod() {
            return period;
        }

        @Override
        public Bank<R> getBank() {
            return list.getBank();
        }

        @Override
        public Currency getCurrency() {
            return list.getCurrency();
        }

        @Override
        public RateList<R> subList(Interval period) {
            return new SubRateList<>(list, this.period.cross(period));
        }

        @Override
        public RateList<R> subList(int fromIndex, int toIndex) {
            return RateList.super.subList(fromIndex, toIndex);
        }

        @Override
        public int size() {
            return period.size();
        }

        @Override
        public R get(int index) {
            if(list.modCount != modCount) {
                throw new ConcurrentModificationException();
            }
            if(index < 0 || index > size()) {
                throw new IndexOutOfBoundsException(
                        String.format("index = %d; size = %d", index, size()));
            }
            return list.get(list.getPeriodBuilder().indexOf(period.get(index)));
        }

        @Override
        public String toString() {
            return RateList.toString(this);
        }

        @Override
        public boolean equals(Object o) {
            return RateList.equals(this, o);
        }

        @Override
        public int hashCode() {
            return RateList.hashCode(this);
        }

        @Override
        public void addListener(ListChangeListener<? super R> listener) {}

        @Override
        public void removeListener(ListChangeListener<? super R> listener) {}

        @Override
        public void addListener(InvalidationListener listener) {}

        @Override
        public void removeListener(InvalidationListener listener) {}
        
    }
    
    //<editor-fold defaultstate="collapsed" desc="Modifier class">
    class Modifier<T extends R> {
        
        //<editor-fold defaultstate="collapsed" desc="Adding">
        public int addOrThrow(T rate) {
            Objects.requireNonNull(rate, "rate");
            if(rate.getList() != null) {
                throw new IllegalArgumentException(
                        "Rate(" + rate + ") already contains in some RateList");
            }
            if(periodBuilder.contains(rate.getDate())) {
                throw new IllegalArgumentException(
                        "Rate with date " + rate.getDate() + " already contains in this RateList");
            }
            if(!rate.getBank().equals(getBank())) {
                throw new IllegalArgumentException(String.format(
                        "Rate bank(%s) not equals RateList bank(%s)",
                        rate.getBank(), getBank()));
            }
            if(!rate.getCurrency().equals(getCurrency())) {
                throw new IllegalArgumentException(String.format(
                        "Rate currency(%s) not equals RateList currency(%s)",
                        rate.getCurrency(), getCurrency()));
            }
            
            periodBuilder.plusDate(rate.getDate());
            
            int index = size();
            for(int i = 0; i < size(); i++) {
                if(get(i).getDate().compareTo(rate.getDate()) > 0) {
                    index = i;
                    break;
                }
            }
            rate.setList(RateListImpl.this);
            RateListImpl.super.add(index, rate);
            return index;
        }
        
        public boolean addAllOrThrow(Collection<? extends T> c) {
            boolean modified = false;
            for(T r : c) {
                addOrThrow(r);
                modified = true;
            }
            return modified;
        }
        
        public boolean addAll(Collection<? extends T> c) {
            boolean modified = false;
            for(T r : c) {
                modified |= add(r) >= 0;
            }
            return modified;
        }
        
        public int add(T rate) {
            if(rate == null
                    || rate.getList() != null
                    || periodBuilder.contains(rate.getDate())
                    || !rate.getBank().equals(getBank())
                    || !rate.getCurrency().equals(getCurrency())) {
                return -1;
            }
            return addOrThrow(rate);
        }
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="Removing">
        public T removeOrThrow(int index) {
            T rate = (T) RateListImpl.super.remove(index);
            periodBuilder.minusDate(rate.getDate());
            rate.setList(null);
            return rate;
        }
        
        public T removeOrThrow(LocalDate date) {
            if(!periodBuilder.contains(date)) {
                throw new IllegalArgumentException(
                        "Rate with date " + date + " not contains in this RateList");
            }
            return removeOrThrow(periodBuilder.indexOf(date));
        }
        
        public T removeOrThrow(T rate) {
            Objects.requireNonNull(rate, "rate");
            
            if(!rate.getBank().equals(getBank())) {
                throw new IllegalArgumentException(String.format(
                        "Rate bank(%s) not equals RateList bank(%s)",
                        rate.getBank(), getBank()));
            }
            if(!rate.getCurrency().equals(getCurrency())) {
                throw new IllegalArgumentException(String.format(
                        "Rate currency(%s) not equals RateList currency(%s)",
                        rate.getCurrency(), getCurrency()));
            }
            
            return removeOrThrow(rate.getDate());
        }
        
        public boolean removeAllOrThrow(Collection<? extends T> c) {
            boolean modified = false;
            for(T r : c) {
                removeOrThrow(r);
                modified = true;
            }
            return modified;
        }
        
        public boolean removeAll(Collection<? extends T> c) {
            boolean modified = false;
            for(T r : c) {
                modified |= remove(r) != null;
            }
            return modified;
        }
        
        public T remove(int index) {
            return index >= 0 || index < size()
                    ? removeOrThrow(index)
                    : null;
        }
        
        public T remove(LocalDate date) {
            return remove(periodBuilder.indexOf(date));
        }
        
        public T remove(T rate) {
            if(rate == null
                    || !periodBuilder.contains(rate.getDate())
                    || !rate.getBank().equals(getBank())
                    || !rate.getCurrency().equals(getCurrency())) {
                return null;
            }
            
            return remove(rate.getDate());
        }
        //</editor-fold>
        
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Unsupported methods">
    @Override
    public final boolean addAll(Collection<? extends R> c) {
        throw new UnsupportedOperationException("addAll");
    }
    
    @Override
    public final boolean addAll(R... elements) {
        throw new UnsupportedOperationException("addAll");
    }
    
    @Override
    public final boolean addAll(int index, Collection<? extends R> c) {
        throw new UnsupportedOperationException("addAll");
    }
    
    @Override
    public final void add(int index, R element) {
        throw new UnsupportedOperationException("add");
    }
    
    @Override
    public final R remove(int index) {
        throw new UnsupportedOperationException("remove");
    }
    
    @Override
    public final void remove(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("remove");
    }
    
    @Override
    public final boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("removeAll");
    }
    
    @Override
    public final boolean removeAll(R... elements) {
        throw new UnsupportedOperationException("removeAll");
    }
    
    @Override
    public final boolean removeIf(Predicate<? super R> filter) {
        throw new UnsupportedOperationException("removeIf");
    }
    
    @Override
    public final void replaceAll(UnaryOperator<R> operator) {
        throw new UnsupportedOperationException("replaceAll");
    }
    
    @Override
    public final boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("retainAll");
    }
    
    @Override
    public final void clear() {
        throw new UnsupportedOperationException("clear");
    }
    
    @Override
    public final boolean retainAll(R... elements) {
        throw new UnsupportedOperationException("retainAll");
    }
    
    @Override
    public final boolean add(R element) {
        throw new UnsupportedOperationException("add");
    }
    
    @Override
    public final boolean remove(Object obj) {
        throw new UnsupportedOperationException("remove");
    }

    @Override
    public final R set(int index, R element) {
        throw new UnsupportedOperationException("set");
    }

    @Override
    public final boolean setAll(Collection<? extends R> col) {
        throw new UnsupportedOperationException("setAll");
    }

    @Override
    public final boolean setAll(R... elements) {
        throw new UnsupportedOperationException("setAll");
    }
    //</editor-fold>

}
