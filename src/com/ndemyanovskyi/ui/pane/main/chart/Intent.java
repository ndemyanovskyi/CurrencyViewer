/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ndemyanovskyi.ui.pane.main.chart;

import com.ndemyanovskyi.backend.Bank;
import com.ndemyanovskyi.backend.Currency;
import com.ndemyanovskyi.backend.Rate;
import com.ndemyanovskyi.backend.Rate.Field;
import com.ndemyanovskyi.util.beans.Properties;
import java.util.Objects;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.paint.Color;


public class Intent<R extends Rate> { 
    
    private final ReadOnlyObjectProperty<Bank<R>> bank;
    private final ReadOnlyObjectProperty<Currency> currency;
    private final ReadOnlyObjectProperty<Field> field;
    private final ReadOnlyObjectProperty<Color> color;
    
    private final ReadOnlyBooleanWrapper attached = new ReadOnlyBooleanWrapper(this, "attached", false);

    public Intent(Bank<R> bank, Currency currency) {
        this(bank, currency, Rate.RATE);
    }

    public Intent(Bank<R> bank, Currency currency, Field field) {
        this(bank, currency, field, randomColor());
    }
    
    public Intent(Bank<R> bank, Currency currency, Field field, Color color) {
	this.bank = Properties.readOnlyNonNull(this, "bank", bank);
	this.currency = Properties.readOnlyNonNull(this, "currency", currency);
	this.field = Properties.readOnlyNonNull(this, "field", field);
	this.color = Properties.readOnlyNonNull(this, "color", color);
        
        if(!bank.getCurrencies().contains(currency)) {
            throw new IllegalArgumentException(
                    "Bank " + bank + " doesn`t support currency " + currency + ".");
        }
        
        if(!bank.getFields().contains(field)) {
            throw new IllegalArgumentException(
                    "Bank " + bank + " doesn`t support rate field " + field + ".");
        }
    }
    
    private static Color randomColor() {
        return Color.rgb(
                (int) (Math.random() * 200),
                (int) (Math.random() * 200),
                (int) (Math.random() * 200));
    }

    public Currency getCurrency() {
	return currency.get();
    }

    public Color getColor() {
	return color.get();
    }

    public Bank<R> getBank() {
	return bank.get();
    }

    public Field getField() {
        return field.get();
    }
    
    public ReadOnlyObjectProperty<Bank<R>> bankProperty() {
        return bank;
    }
    
    public ReadOnlyObjectProperty<Currency> currencyProperty() {
        return currency;
    }
    
    public ReadOnlyObjectProperty<Field> fieldProperty() {
        return field;
    }
    
    public ReadOnlyObjectProperty<Color> colorProperty() {
        return color;
    }
    
    public boolean is(Bank<?> bank, Currency currency) {
        return getBank().equals(bank) 
                && getCurrency().equals(currency);
    }
    
    public boolean is(Bank<?> bank, Currency currency, Field field) {
        return is(bank, currency) && getField().equals(field);
    }
    
    public boolean is(Bank<?> bank, Currency currency, Field field, Color color) {
        return is(bank, currency, field) && getColor().equals(color);
    }
    
    public boolean isAttached() {
        return attachedPropertyImpl().get();
    }

    void setAttached(boolean attached) {
        attachedPropertyImpl().set(attached);
    }
    
    public ReadOnlyBooleanProperty attachedProperty() {
        return attachedPropertyImpl().getReadOnlyProperty();
    }
    
    private ReadOnlyBooleanWrapper attachedPropertyImpl() {
        return attached;
    }

    @Override
    public boolean equals(Object o) {
        if(o == this) return true;
        if(o != null && o instanceof Intent) {
            Intent<?> other = (Intent<?>) o;
            return this.getBank().equals(other.getBank())
                    && this.getCurrency().equals(other.getCurrency())
                    && this.getField().equals(other.getField())
                    && this.isAttached() == other.isAttached();
        }
        return false;
    }

    @Override
    public String toString() {
        return "Intent[" + "bank=" + getBank() + ", currency=" + getCurrency() + ", field=" + getField() + ']';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 13 * hash + Objects.hashCode(this.bank);
        hash = 13 * hash + Objects.hashCode(this.currency);
        hash = 13 * hash + Objects.hashCode(this.field);
        hash = 13 * hash + Objects.hashCode(this.attached);
        return hash;
    }
    
}
