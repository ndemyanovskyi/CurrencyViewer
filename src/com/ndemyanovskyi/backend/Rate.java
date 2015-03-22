/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.backend;

import com.ndemyanovskyi.app.localization.Language;
import com.ndemyanovskyi.app.localization.binding.ResourceBindings;
import com.ndemyanovskyi.app.res.Resources;
import com.ndemyanovskyi.util.Unmodifiable;
import com.ndemyanovskyi.util.number.Numbers;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import javafx.beans.property.ReadOnlyProperty;

public class Rate implements Comparable<Rate> {
    
    public static final Field RATE = new Field("RATE");
    
    private static final Set<Field> FIELDS = Unmodifiable.set(RATE);

    private RateList<? extends Rate> list;
    private final Bank<? extends Rate> bank;
    private final Currency currency;
    private final LocalDate localDate;
    private final BigDecimal rate;

    public Rate(Bank<Rate> bank, Currency currency, LocalDate localDate, BigDecimal rate) {
        this(null, bank, currency, localDate, rate);
    }

    <T extends Rate> Rate(RateList<T> list, Bank<T> bank, Currency currency, LocalDate localDate, BigDecimal rate) {
        this.list = list;
        this.bank = Objects.requireNonNull(bank, "bank");
        this.currency = Objects.requireNonNull(currency, "currency");
        this.localDate = Objects.requireNonNull(localDate, "localDate");
        this.rate = Numbers.require(Objects.requireNonNull(rate, "rate"),
                v -> v.compareTo(BigDecimal.ZERO) >= 0, "rate < 0");
    }

    public RateList<? extends Rate> getList() {
        return list;
    }

    void setList(RateList<? extends Rate> list) {
        this.list = list;
    }
    
    public boolean isZero() {
        return getRate().equals(BigDecimal.ZERO);
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

    public BigDecimal getRate() {
        return rate;
    }

    public BigDecimal get(Field field) {
        if(RATE.equals(field)) {
                return getRate();
        }
        
        throw new IllegalArgumentException(
                "Field '" + field + "' is unsupported by Rate.");
    }
    
    public Set<Field> getFields() {
        return FIELDS;
    }

    public boolean is(Bank<?> bank, Currency currency, LocalDate localDate, BigDecimal rate) {
        return this.bank.equals(bank)
                && this.currency.equals(currency)
                && this.localDate.equals(localDate)
                && this.rate.compareTo(rate) == 0;
    }

    private int hash;

    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = 7;
            hash = 67 * hash + Objects.hashCode(this.bank);
            hash = 67 * hash + Objects.hashCode(this.currency);
            hash = 67 * hash + Objects.hashCode(this.localDate);
            hash = 67 * hash + Objects.hashCode(this.rate);
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
    public int compareTo(Rate other) {
        return getRate().compareTo(other.getRate());
    }

    /*public static class Builder {

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

    }*/

    public static final class Field {

        private final String name;
        private final String resourceName;
        private ReadOnlyProperty<String> displayNameProperty;

        public Field(String name) {
            this(name, name.toLowerCase());
        }

        public Field(String name, String resourceName) {
            this.name = Objects.requireNonNull(name, "name");
            this.resourceName = Objects.requireNonNull(resourceName, "resourceName");
        }

        public ReadOnlyProperty<String> displayNameProperty() {
            return displayNameProperty != null ? displayNameProperty 
                    : (displayNameProperty = ResourceBindings.strings().get(resourceName));
        }

        public String getDisplayName(Language language) {
            return Resources.strings(language).get(resourceName);
        }
    
        public String getDisplayName() {
            return displayNameProperty().getValue();
        }

        public String getName() {
            return name;
        }

        public String getResourceName() {
            return resourceName;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 83 * hash + Objects.hashCode(this.name);
            hash = 83 * hash + Objects.hashCode(this.resourceName);
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            if(o == this) return true;
            if(o == null || !(o instanceof Field)) return false;
            final Field other = (Field) o;
            return Objects.equals(getResourceName(), other.getName()) 
                    && Objects.equals(getResourceName(), other.getResourceName());
        }

    }

}
