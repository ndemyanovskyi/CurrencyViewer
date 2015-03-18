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
import com.ndemyanovskyi.util.beans.FinalNonNullProperty;
import java.time.Instant;
import java.util.Objects;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.paint.Color;


public class Intent<R extends Rate> { 
    
    private final ReadOnlyObjectProperty<Bank<R>> bank;
    private final ReadOnlyObjectProperty<Currency> currency;
    private final ReadOnlyObjectProperty<Field> field;
    private final ReadOnlyObjectProperty<Color> color;
    private final ReadOnlyObjectProperty<Instant> instant;
    
    private final BooleanProperty showing = new SimpleBooleanProperty(this, "showing", false);
    private final BooleanProperty shown = new SimpleBooleanProperty(this, "shown", false);
    private final BooleanProperty hidden = new SimpleBooleanProperty(this, "hidden", false);
    private final BooleanProperty loading = new SimpleBooleanProperty(this, "loading", false);
    private final BooleanProperty attached = new SimpleBooleanProperty(this, "attached", false);

    public Intent(Bank<R> bank, Currency currency) {
        this(bank, currency, Field.RATE);
    }

    public Intent(Bank<R> bank, Currency currency, Field field) {
        this(bank, currency, field, randomColor());
    }
    
    public Intent(Bank<R> bank, Currency currency, Field field, Color color) {
	this.bank = new FinalNonNullProperty<>(this, "bank", bank);
	this.currency = new FinalNonNullProperty<>(this, "currency", currency);
	this.field = new FinalNonNullProperty<>(this, "field", field);
	this.color = new FinalNonNullProperty<>(this, "color", color);
        this.instant = new FinalNonNullProperty<>(this, "instant", Instant.now());
        
        if(!bank.getCurrencys().contains(currency)) {
            throw new IllegalArgumentException(
                    "Bank " + bank + " doesn`t support currency " + currency + ".");
        }
        
        if(!bank.getFields().contains(field)) {
            throw new IllegalArgumentException(
                    "Bank " + bank + " doesn`t support rate field " + field + ".");
        }
    }

    public Intent(Intent<R> intent) {
        this(Objects.requireNonNull(intent, "intent").getBank(), 
                intent.getCurrency(), intent.getField());
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

    public Instant getInstant() {
        return instant.get();
    }

    public ReadOnlyObjectProperty<Instant> instantProperty() {
        return instant;
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
    
    public boolean is(Bank<?> bank, Currency currency, Field field) {
        return getBank().equals(bank) 
                && getCurrency().equals(currency) 
                && getField().equals(field);
    }
    
    public boolean isAttached() {
        return attached.get();
    }

    public boolean isShown() {
        return shown.get();
    }

    public boolean isShowing() {
        return showing.get();
    }

    public boolean isLoading() {
        return loading.get();
    }

    public boolean isHidden() {
        return hidden.get();
    }

    public void setAttached(boolean attached) {
        this.attached.set(attached);
    }

    public void setShown(boolean shown) {
        this.shown.set(shown);
    }

    public void setShowing(boolean showing) {
        this.showing.set(showing);
    }

    public void setLoading(boolean loading) {
        this.loading.set(loading);
    }

    public void setHidden(boolean hidden) {
        this.hidden.set(hidden);
    }
    
    public BooleanProperty showingProperty() {
        return showing;
    }
    
    public BooleanProperty hiddenProperty() {
        return hidden;
    }
    
    public BooleanProperty shownProperty() {
        return shown;
    }
    
    public BooleanProperty loadingProperty() {
        return loading;
    }
    
    public BooleanProperty attachedProperty() {
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
                    && this.isAttached() == other.isAttached()
                    && this.isLoading() == other.isLoading()
                    && this.isShowing() == other.isShowing()
                    && this.isHidden() == other.isHidden()
                    && this.isShown() == other.isShown();
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
        hash = 13 * hash + Objects.hashCode(this.showing);
        hash = 13 * hash + Objects.hashCode(this.shown);
        hash = 13 * hash + Objects.hashCode(this.hidden);
        hash = 13 * hash + Objects.hashCode(this.loading);
        hash = 13 * hash + Objects.hashCode(this.attached);
        return hash;
    }
    
}
