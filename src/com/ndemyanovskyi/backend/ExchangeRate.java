/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ndemyanovskyi.backend;

import com.ndemyanovskyi.reflection.Types;
import com.ndemyanovskyi.util.Unmodifiable;
import com.ndemyanovskyi.util.number.Numbers;
import com.ndemyanovskyi.util.number.Numbers.Floats;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;


public class ExchangeRate extends Rate {
    
    private static final Set<Field> FIELDS = Unmodifiable.set(Field.BUY, Field.SALE, Field.RATE);
    
    private final Float sale;
    private final Float buy;

    public ExchangeRate(Bank<? extends ExchangeRate> bank, Currency currency, LocalDate date, Float buy, Float sale) {
	this(null, bank, currency, date, buy, sale);
    }

    <T extends ExchangeRate> ExchangeRate(RateList<T> list, Bank<T> bank, Currency currency, LocalDate date, float buy, float sale) {
	super(list, bank, currency, date, (buy + sale) / 2);
        
	this.sale = Numbers.require(Objects.requireNonNull(sale, "sale"), 
                v -> v > 0.0 || v.isNaN(), "sale <= 0 && sale != NaN");
	this.buy = Numbers.require(Objects.requireNonNull(buy, "buy"), 
                v -> v > 0.0 || v.isNaN(), "buy <= 0 && buy != NaN");
    }
    
    private Float getSupportedValue(Rate rate, Field field) {
        return rate instanceof ExchangeRate 
                ? rate.get(field)
                : rate.get(Field.RATE);
    }

    @Override
    public Bank<? extends ExchangeRate> getBank() {
        return (Bank<? extends ExchangeRate>) super.getBank();
    }

    @Override
    public ExchangeRate merge(Rate rate) {
        super.merge(rate);
        
        Float mergedBuy = getBuy().isNaN() 
                ? getSupportedValue(rate, Field.BUY) 
                : getBuy();
        
        Float mergedSale = getBuy().isNaN() 
                ? getSupportedValue(rate, Field.SALE) 
                : getSale();
        
        return !mergedBuy.equals(getBuy()) || mergedSale.equals(getSale())
                ? new ExchangeRate(getBank(), getCurrency(), getDate(), mergedBuy, mergedSale)
                : this;        
    }

    @Override
    public boolean isNaN() {
        return buy.isNaN() && sale.isNaN();
    }
    
    @Override
    public Float get(Field field) {
        switch(field) {
            
            case RATE: 
                return getRate();
            
            case BUY: 
                return getBuy();
            
            case SALE: 
                return getSale();
            
            default: 
                throw new IllegalArgumentException(
                        "Field '" + field + "' is unsupported by ExchangeRate.");
        }
    }
    
    @Override
    public Set<Field> getFields() {
        return FIELDS;
    }

    public Float getBuy() {
	return buy;
    }

    public Float getSale() {
	return sale;
    }
    
    public boolean is(Bank<?> bank, Currency currency, LocalDate date, double buy, double sale) {
	return super.is(bank, currency, date, getRate())
		&& this.sale == sale 
		&& this.buy == buy;
    }
   
    public static Builder builder() {
	return new Builder();
    }
	
    private int hash;

    @Override
    public int hashCode() {
	if(hash == 0) {
	    hash = super.hashCode();
	    hash = 67 * hash + (int) 
		    (Double.doubleToLongBits(this.sale) ^ 
		    (Double.doubleToLongBits(this.sale) >>> 32));
	    hash = 67 * hash + (int) 
		    (Double.doubleToLongBits(this.buy) ^ 
		    (Double.doubleToLongBits(this.buy) >>> 32));
	}
	return hash;
    }

    @Override
    public boolean equals(Object obj) {
	if(obj == null || !(obj instanceof ExchangeRate)) return false;
	
	final ExchangeRate other = (ExchangeRate) obj;
	return super.equals(other) 
		&& getSale().equals(other.getSale())
		&& getBuy().equals(other.getBuy());
    }

    @Override
    public String toString() {
	return "Rate [" + "bank=" + getBank() + ", currency=" + getCurrency() + ", date=" + getDate() + ", sale=" + sale + ", buy=" + buy + ", rate=" + getRate() + ']';
    }
    
    public static class Builder extends Rate.Builder {
	
	private float sale;
	private float buy;
	
	Builder() {}
	
	@Override
	public ExchangeRate build() {
	    return new ExchangeRate(getBank(), getCurrency(), getLocalDate(), buy, sale);
	}
	
	//<editor-fold defaultstate="collapsed" desc="Getters and setters">
	
	private static boolean isValidBank(Bank<?> bank) {
	    if(!(bank instanceof CommercialBank)) {
		Type type = Types.resolveGenericType(Bank.class, bank.getClass());
		if(type != null && type instanceof ParameterizedType) {
		    Type parameter = ((ParameterizedType) type).getActualTypeArguments()[0];
		    if(parameter.equals(ExchangeRate.class)) return true;
		}
	    }
	    return false;
	}

	@Override
	public Bank<? extends ExchangeRate> getBank() {
	    return (Bank<? extends ExchangeRate>) super.getBank();
	}

	@Override
	public Builder setBank(Bank<?> bank) {
	    if(!isValidBank(Objects.requireNonNull(bank, "bank"))) {
		throw new IllegalArgumentException(
			"Bank parameter must be instance of ExchangeRate.");
	    }
		
	    return (Builder) super.setBank(bank);
	}

	@Override
	public Builder setCurrency(Currency currency) {
	    return (Builder) super.setCurrency(currency);
	}

	@Override
	public Builder setRate(float rate) {
	    throw new UnsupportedOperationException(
		    "Method setRate(float) is unsupported in ExchangeRate.Builder "
			    + "because rate value is autocalculatable in ExchangeRate. "
			    + "Use setBuy(float) and setSale(float) methods.");
	}
	
	@Override
	public Builder setLocalDate(LocalDate date) {
	    return (Builder) super.setLocalDate(date);
	}
	
	public float getSale() {
	    return sale;
	}
	
	public Builder setSale(float sale) {
	    this.sale = Floats.require(sale, d -> d > 0.0, "sale <= 0");
	    super.setRate((buy + sale) / 2);
	    return this;
	}
	
	public float getBuy() {
	    return buy;
	}
	
	public Builder setBuy(float buy) {
	    this.buy = Floats.require(buy, d -> d > 0.0, "buy <= 0");
	    super.setRate((buy + sale) / 2);
	    return this;
	}
	//</editor-fold>

    }

}
