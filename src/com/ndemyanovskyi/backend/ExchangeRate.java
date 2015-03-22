/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ndemyanovskyi.backend;

import com.ndemyanovskyi.util.Unmodifiable;
import com.ndemyanovskyi.util.number.Numbers;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;


public class ExchangeRate extends Rate {
    
    public static final Field BUY = new Field("BUY");
    public static final Field SALE = new Field("SALE");
    
    private static final Set<Field> FIELDS = Unmodifiable.set(BUY, SALE, RATE);
    
    private final BigDecimal sale;
    private final BigDecimal buy;

    public ExchangeRate(Bank<? extends ExchangeRate> bank, Currency currency, LocalDate date, BigDecimal buy, BigDecimal sale) {
	this(null, bank, currency, date, buy, sale);
    }

    <T extends ExchangeRate> ExchangeRate(RateList<T> list, Bank<T> bank, Currency currency, LocalDate date, BigDecimal buy, BigDecimal sale) {
	super(list, bank, currency, date, calcRate(buy, sale));
        
        this.buy = Numbers.require(Objects.requireNonNull(buy, "buy"),
                v -> v.compareTo(BigDecimal.ZERO) >= 0, "buy < 0");
        this.sale = Numbers.require(Objects.requireNonNull(sale, "sale"),
                v -> v.compareTo(BigDecimal.ZERO) >= 0, "sale < 0");
    }
    
    private static BigDecimal calcRate(BigDecimal buy, BigDecimal sale) {
        if(buy == null || buy.equals(BigDecimal.ZERO)) return sale;
        if(sale == null || sale.equals(BigDecimal.ZERO)) return buy;
        return buy.add(sale).divide(BigDecimal.valueOf(2));
    }
    
    @Override
    public boolean isZero() {
        return getBuy().equals(BigDecimal.ZERO) 
                && getSale().equals(BigDecimal.ZERO);
    }

    @Override
    public Bank<? extends ExchangeRate> getBank() {
        return (Bank<? extends ExchangeRate>) super.getBank();
    }
    
    @Override
    public BigDecimal get(Field field) {
        if(BUY.equals(field)) return getBuy();
        if(SALE.equals(field)) return getSale();
        if(RATE.equals(field)) return getRate();
        
        throw new IllegalArgumentException(
                "Field '" + field + "' is unsupported by ExchangeRate.");
    }
    
    @Override
    public Set<Field> getFields() {
        return FIELDS;
    }

    public BigDecimal getBuy() {
	return buy;
    }

    public BigDecimal getSale() {
	return sale;
    }
    
    public boolean is(Bank<?> bank, Currency currency, LocalDate date, BigDecimal buy, BigDecimal sale) {
	return super.is(bank, currency, date, getRate())
		&& this.sale.compareTo(sale) == 0
		&& this.buy.compareTo(buy) == 0;
    }
   
    /*public static Builder builder() {
	return new Builder();
    }*/
	
    private int hash;

    @Override
    public int hashCode() {
	if(hash == 0) {
	    hash = super.hashCode();
	    hash = 67 * hash + Objects.hashCode(buy);
	    hash = 67 * hash + Objects.hashCode(sale);
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
    
    /*public static class Builder extends Rate.Builder {
	
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

    }*/

}
