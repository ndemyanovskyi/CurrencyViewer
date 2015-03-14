/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.backend;

import com.ndemyanovskyi.util.Unmodifiable;
import com.ndemyanovskyi.util.number.Numbers;
import com.ndemyanovskyi.util.number.Numbers.Floats;
import com.ndemyanovskyi.app.localization.Language;
import com.ndemyanovskyi.app.localization.binding.ResourceBindings;
import com.ndemyanovskyi.app.res.Resources;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import javafx.beans.property.ReadOnlyProperty;

public class Rate implements Comparable<Rate> {
    
    private static final Set<Field> FIELDS = Unmodifiable.set(Field.RATE);

    private RateList<? extends Rate> list;
    private final Bank<? extends Rate> bank;
    private final Currency currency;
    private final LocalDate localDate;
    private final Float rate;

    public Rate(Bank<Rate> bank, Currency currency, LocalDate localDate, Float rate) {
        this(null, bank, currency, localDate, rate);
    }

    <T extends Rate> Rate(RateList<T> list, Bank<T> bank, Currency currency, LocalDate localDate, Float rate) {
        this.list = list;
        this.bank = Objects.requireNonNull(bank, "bank");
        this.currency = Objects.requireNonNull(currency, "currency");
        this.localDate = Objects.requireNonNull(localDate, "localDate");
        this.rate = Numbers.require(Objects.requireNonNull(rate, "rate"), 
                v -> v > 0.0 || Double.isNaN(v), "rate <= 0 && rate != NaN");
    }

    public RateList<? extends Rate> getList() {
        return list;
    }

    void setList(RateList<? extends Rate> list) {
        this.list = list;
    }

    public Currency getCurrency() {
        return currency;
    }

    public LocalDate getDate() {
        return localDate;
    }

    public Bank<? extends Rate> getBank() {
        return bank;
    }

    public Float getRate() {
        return rate;
    }

    public boolean isNaN() {
        return rate.isNaN();
    }
    
    public Rate merge(Rate rate) {
        Objects.requireNonNull(rate, "rate");
        if(!rate.getBank().equals(getBank())) {
            throw new IllegalArgumentException("Banks is different.");
        }
        if(!rate.getCurrency().equals(getCurrency())) {
            throw new IllegalArgumentException("Currencys is different.");
        }
        if(!rate.getDate().equals(getDate())) {
            throw new IllegalArgumentException("Dates is different.");
        }
        return !isNaN() ? this : !rate.isNaN() ? rate : this;
    }

    public Float get(Field field) {
        switch (field) {

            case RATE:
                return getRate();

            default:
                throw new IllegalArgumentException(
                        "Field '" + field + "' is unsupported by Rate.");
        }
    }
    
    public Set<Field> getFields() {
        return FIELDS;
    }

    public boolean is(Bank<?> bank, Currency currency, LocalDate localDate, float rate) {
        return this.bank.equals(bank)
                && this.currency.equals(currency)
                && this.localDate.equals(localDate)
                && Float.compare(this.rate, rate) == 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    private int hash;

    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = 7;
            hash = 67 * hash + Objects.hashCode(this.bank);
            hash = 67 * hash + Objects.hashCode(this.currency);
            hash = 67 * hash + Objects.hashCode(this.localDate);
            hash = 67 * hash + (int) (Double.doubleToLongBits(this.rate)
                    ^ (Double.doubleToLongBits(this.rate) >>> 32));
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj != null && obj instanceof Rate
                && ((Rate) obj).is(bank, currency, localDate, rate));
    }

    @Override
    public String toString() {
        return "Rate [" + "bank=" + getBank() + ", currency=" + getCurrency() + ", localDate=" + localDate + ", rate=" + rate + ']';
    }

    @Override
    public int compareTo(Rate o) {
        return Float.compare(rate, o.rate);
    }

    public static class Builder {

        private Bank<?> bank;
        private Currency currency;
        private LocalDate localDate;
        private float rate;

        Builder() {
        }

        public Rate build() {
            return new Rate((Bank<Rate>) bank, currency, localDate, rate);
        }

	//<editor-fold defaultstate="collapsed" desc="Getters and setters">
        public Bank<?> getBank() {
            return bank;
        }

        public Currency getCurrency() {
            return currency;
        }

        public Builder setBank(Bank<?> bank) {
            this.bank = Objects.requireNonNull(bank, "bank");
            return this;
        }

        public Builder setCurrency(Currency currency) {
            this.currency = Objects.requireNonNull(currency, "currency");
            return this;
        }

        public LocalDate getLocalDate() {
            return localDate;
        }

        public Builder setLocalDate(LocalDate localDate) {
            this.localDate = Objects.requireNonNull(localDate, "localDate");
            return this;
        }

        public float getRate() {
            return rate;
        }

        public Builder setRate(float rate) {
            this.rate = Floats.require(rate, d -> d > 0.0, "rate <= 0");
            return this;
        }
        //</editor-fold>

    }

    public enum Field {

        BUY("buy"), SALE("sale"), RATE("rate");

        private final String resourceKey;
        private ReadOnlyProperty<String> displayNameProperty;

        public ReadOnlyProperty<String> displayNameProperty() {
            return displayNameProperty != null 
                    ? displayNameProperty 
                    : (displayNameProperty = ResourceBindings.strings().get(resourceKey));
        }

        public String getDisplayName(Language language) {
            return Resources.strings(language).get(resourceKey);
        }
    
        public String getDisplayName() {
            return displayNameProperty().getValue();
        }

        private Field(String resourceKey) {
            this.resourceKey = resourceKey;
        }

    }

}
