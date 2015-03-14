/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ndemyanovskyi.app.res;

import com.ndemyanovskyi.collection.set.unmodifiable.AbstractUnmodifiableSet;
import com.ndemyanovskyi.map.unmodifiable.AbstractUnmodifiableMap;
import com.ndemyanovskyi.reflection.Reflection;
import com.ndemyanovskyi.app.localization.Language;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import static org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength.HARD;
import static org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength.SOFT;
import org.apache.commons.collections4.map.ReferenceMap;


public abstract class Resources<T> extends AbstractUnmodifiableMap<String, T> {
    
    private static Map<Language, ImageResources> images;
    private static Map<Language, NumberResources> numbers;
    private static Map<Language, StringResources> strings;
    private static Map<Language, BooleanResources> booleans;
    
    private final Language language;
    private final Path path;
    private EntrySet entrySet;

    protected Resources(Language language, Path path) {
	this.language = Objects.requireNonNull(language, "language");
	this.path = Objects.requireNonNull(path, "path").toAbsolutePath();
    }
    
    protected abstract Set<Entry<String, T>> defaultEntrySet();

    @Override
    public final Set<Entry<String, T>> entrySet() {
	return entrySet != null ? entrySet : (entrySet = new EntrySet());
    }

    @Override
    public final T get(Object key) {
	Objects.requireNonNull(key, "key");
	
	if(!(key instanceof String)) {
	    throw new IllegalArgumentException(
		    "Key must be instance of String.");
	}
	
	if(!getDefaultResources().containsKey(key)) {
	    throw new IllegalArgumentException(
		    "Key '" + key + "' not defined in default resources.");
	}
	
	return getEntry((String) key).getValue();
    }

    public Path getPath() {
	return path;
    }

    public Language getLanguage() {
	return language;
    }

    public abstract Resources<T> getParentResources();

    public abstract Resources<T> getDefaultResources();
    
    protected abstract Entry<String, T> getEntry(String key);
    
    public static StringResources strings() {
	return strings(Language.getDefault());
    }
    
    public static NumberResources numbers() {
	return numbers(Language.getDefault());
    }
    
    public static ImageResources images() {
	return images(Language.getDefault());
    }
    
    public static BooleanResources booleans() {
	return booleans(Language.getDefault());
    }
    
    public static StringResources strings(String languageTag) {
	return strings(Language.valueOfTag(languageTag));
    }
    
    public static NumberResources numbers(String languageTag) {
	return numbers(Language.valueOfTag(languageTag));
    }
    
    public static ImageResources images(String languageTag) {
	return images(Language.valueOfTag(languageTag));
    }
    
    public static BooleanResources booleans(String languageTag) {
	return booleans(Language.valueOfTag(languageTag));
    }
    
    public static StringResources strings(Language language) {
	if(strings == null) strings = new ReferenceMap<>(HARD, SOFT);
	
	StringResources rs = strings.get(language);
	if(rs == null) {
	    rs = new StringResources(language);
	    strings.put(language, rs);
	}
	return rs;
    }
    
    public static NumberResources numbers(Language language) {
	if(numbers == null) numbers = new ReferenceMap<>(HARD, SOFT);
	
	NumberResources rs = numbers.get(language);
	if(rs == null) {
	    rs = new NumberResources(language);
	    numbers.put(language, rs);
	}
	return rs;
    }
    
    public static ImageResources images(Language language) {
	if(images == null) images = new ReferenceMap<>(HARD, SOFT);
	
	ImageResources rs = images.get(language);
	if(rs == null) {
	    rs = new ImageResources(language);
	    images.put(language, rs);
	}
	return rs;
    }
    
    public static BooleanResources booleans(Language language) {
	if(booleans == null) booleans = new ReferenceMap<>(HARD, SOFT);
	
	BooleanResources rs = booleans.get(language);
	if(rs == null) {
	    rs = new BooleanResources(language);
	    booleans.put(language, rs);
	}
	return rs;
    }
    
    public static InputStream getStream(String path) {
	Class<?> c = path.startsWith("/")
		? Resources.class 
		: Reflection.getCallerClass();
	
	return c.getResourceAsStream(path);
    }
    
    public static URL getUrl(String path) {
	Class<?> c = path.startsWith("/")
		? Resources.class 
		: Reflection.getCallerClass();
	
	return c.getResource(path);
    }
    
    private class EntrySet extends AbstractUnmodifiableSet<Entry<String, T>> {

	@Override
	public Iterator<Entry<String, T>> iterator() {
	    return new EntryIterator();
	}

	@Override
	public int size() {
	    return getDefaultResources().size();
	}
	
	private class EntryIterator implements Iterator<Entry<String, T>> {
	    
	    private final Iterator<Entry<String, T>> it = defaultEntrySet().iterator();

	    @Override
	    public Entry<String, T> next() {
		return getEntry(it.next().getKey());
	    }

	    @Override
	    public boolean hasNext() {
		return it.hasNext();
	    }
	    
	}
	
    }

}
