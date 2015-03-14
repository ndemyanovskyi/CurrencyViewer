/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ndemyanovskyi.app.res;

import com.ndemyanovskyi.app.localization.Language;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;


public final class NumberResources extends ValueResources<Number> {
    
    static final File ROOT = new File("res\\values\\numbers.xml");
    
    static final String INTEGER_TAG = "integer";
    static final String FLOAT_TAG = "float";
    static final String DOUBLE_TAG = "double";
    static final String LONG_TAG = "long";
    static final String BYTE_TAG = "byte";
    static final String SHORT_TAG = "short";
    static final String BIG_INTEGER_TAG = "big_integer";
    static final String BIG_DECIMAL_TAG = "big_decimal";
    
    private static final List<String> TAGS = Arrays.asList(INTEGER_TAG, FLOAT_TAG, DOUBLE_TAG, 
	LONG_TAG, SHORT_TAG, BYTE_TAG, BIG_DECIMAL_TAG, BIG_INTEGER_TAG);
    
    NumberResources(Language language) {
	super(language, "numbers", TAGS);
    }

    @Override
    protected Number convert(String nodeName, String text) {
	switch(nodeName) {
	    case LONG_TAG: return Long.parseLong(text);
	    case BYTE_TAG: return Byte.parseByte(text);
	    case FLOAT_TAG: return Float.parseFloat(text);
	    case SHORT_TAG: return Short.parseShort(text);
	    case INTEGER_TAG: return Integer.parseInt(text);
	    case BIG_DECIMAL_TAG: return new BigDecimal(text);
	    case BIG_INTEGER_TAG: return new BigInteger(text);
	    default: return Double.parseDouble(text);
	}
    }

    /*@Override
    protected String getTag(Set<String> tags, Number value) {
	Class<?> c = value.getClass();
	
	if(c.isAssignableFrom(Long.class)) return LONG_TAG;
	if(c.isAssignableFrom(Byte.class)) return BYTE_TAG;
	if(c.isAssignableFrom(Short.class)) return SHORT_TAG;
	if(c.isAssignableFrom(Float.class)) return FLOAT_TAG;
	if(c.isAssignableFrom(Integer.class)) return INTEGER_TAG;
	if(c.isAssignableFrom(BigInteger.class)) return BIG_INTEGER_TAG;
	if(c.isAssignableFrom(BigDecimal.class)) return BIG_DECIMAL_TAG;
	
	return DOUBLE_TAG;
    }*/

    @Override
    public NumberResources getParentResources() {
	Language parent = getLanguage().parent();
	return parent != null ? Resources.numbers(parent) : null;
    }

    @Override
    public NumberResources getDefaultResources() {
	return Resources.numbers();
    }

    @Override
    protected String convert(Number value) {
	Class<?> c = value.getClass();
	
	return Long.class.isAssignableFrom(c) 
                || Byte.class.isAssignableFrom(c) 
                || Short.class.isAssignableFrom(c) 
                || Float.class.isAssignableFrom(c) 
                || Integer.class.isAssignableFrom(c) 
                || BigInteger.class.isAssignableFrom(c) 
                || BigDecimal.class.isAssignableFrom(c) 
                ? value.toString() 
                : String.valueOf(value.doubleValue());
    }
    
    public Integer getAsInteger(Object key) {
	Number num = get(key);
	return num instanceof Integer ? 
                (Integer) num : num.intValue();
    }
    
    public Double getAsDouble(Object key) {
	Number num = get(key);
	return num instanceof Double ? 
                (Double) num : num.doubleValue();
    }
    
    public Float getAsFloat(Object key) {
	Number num = get(key);
	return num instanceof Float ? 
                (Float) num : num.floatValue();
    }
    
    public Long getAsLong(Object key) {
	Number num = get(key);
	return num instanceof Long ? 
                (Long) num : num.longValue();
    }
    
    public Byte getAsByte(Object key) {
	Number num = get(key);
	return num instanceof Byte ? 
                (Byte) num : num.byteValue();
    }
    
    public Short getAsShort(Object key) {
	Number num = get(key);
	return num instanceof Short ? 
                (Short) num : num.shortValue();
    }
    
    public BigInteger getAsBigInteger(Object key) {
	Number num = get(key);
	return num instanceof BigInteger ? (BigInteger) num : num instanceof BigDecimal ? 
		((BigDecimal) num).toBigInteger() : BigInteger.valueOf(num.longValue());
    }
    
    public BigDecimal getAsBigDecimal(Object key) {
	Number num = get(key);
	return num instanceof BigDecimal ? (BigDecimal) num : num instanceof BigInteger ? 
		new BigDecimal(((BigInteger) num)) : BigDecimal.valueOf(num.doubleValue());
    }

}
