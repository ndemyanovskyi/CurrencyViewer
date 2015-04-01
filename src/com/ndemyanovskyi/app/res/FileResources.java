/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ndemyanovskyi.app.res;

import com.ndemyanovskyi.app.localization.Language;
import com.ndemyanovskyi.collection.set.ArrayListedSet;
import com.ndemyanovskyi.collection.set.ListedSet;
import com.ndemyanovskyi.throwable.Exceptions;
import com.ndemyanovskyi.util.BiConverter;
import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;


public abstract class FileResources<T> extends Resources<T> {
    
    private static final Predicate<String> DEFAULT_PREDICATE = key -> true;
    
    private static final BiConverter<String, String> DEFAULT_KEY_CONVERTER = 
            BiConverter.of(key -> key, key -> key);
    
    private ListedSet<Entry<String, T>> data;
    private final Predicate<? super String> predicate;
    private final BiConverter<String, String> keyConverter;  

    public FileResources(Language language, String folder, Predicate<? super String> predicate) {
        this(language, folder, predicate, DEFAULT_KEY_CONVERTER);
    }   

    public FileResources(Language language, String folder, BiConverter<String, String> keyConverter) {
        this(language, folder, DEFAULT_PREDICATE, keyConverter);
    }  

    public FileResources(Language language, String folder) {
        this(language, folder, DEFAULT_PREDICATE, DEFAULT_KEY_CONVERTER);
    }   
    
    FileResources(Language language, String folder, Predicate<? super String> predicate, BiConverter<String, String> keyConverter) {
	super(language, Paths.get("res", "files", 
		Objects.requireNonNull(folder, "folder"), language.tag().toLowerCase()));
        
        this.predicate = Objects.requireNonNull(predicate, "predicate");
        this.keyConverter = Objects.requireNonNull(keyConverter, "keyConverter");
	
        if(Files.exists(getPath())) {
	    for(String name : getPath().toFile().list()) {
                if(predicate.test(name)) {
                    getData().add(new FileEntry(name));
                }
	    }
	}
    }
    
    public Predicate<? super String> getPredicate() {
        return predicate;
    }    

    public BiConverter<String, String> getKeyConverter() {
        return keyConverter;
    }

    @Override
    protected Set<Entry<String, T>> defaultEntrySet() {
	return getDefaultResources() != this 
		? getDefaultResources().getData() 
		: getData();
    }

    @Override
    public boolean containsKey(Object keyObject) {
	if(keyObject == null || !(keyObject instanceof String)) return false;
	return findEntry((String) keyObject) != null;
    }
    
    protected abstract T readFile(File file) throws IOException;
    
    protected String convertKey(String key) {
        return key;
    }

    @Override
    protected Entry<String, T> getEntry(String key) {
	Entry<String, T> e = findEntry(key);
	return e != null ? e : getParentResources().getEntry(key);
    } 

    @Override
    public abstract FileResources<T> getDefaultResources();

    @Override
    public abstract FileResources<T> getParentResources();

    @Override
    public final int size() {
	FileResources<?> res = getDefaultResources();
	return res.data != null ? res.data.size() : 0;
    }
    
    private Entry<String, T> findEntry(String key) {
	if(data != null && !data.isEmpty()) {
	    boolean extension = key.lastIndexOf('.') != -1;
	    
	    for(int i = 0; i < data.size(); i++) {
		Entry<String, T> e = data.get(i);
		String fileName = extension ? e.getKey() : removeExtension(e.getKey());
		if(fileName.equalsIgnoreCase(key)) {
		    return e;
		}
	    }
	}
	return null;
    }
    
    private static String removeExtension(String name) {
	int index = name.lastIndexOf('.');
	return index != -1 ? name.substring(0, index) : name;
    }
    
    /*private static boolean fileNamesEquals(String a, String b) {
	int dotA = a.lastIndexOf('.');
	int dotB = b.lastIndexOf('.');
	
	if(dotA != dotB) return false;
	for(int i = 0; i < a.length(); i++) {
	    char charA = a.charAt(i);
	    char charB = b.charAt(i);
	    
	    if(Character.toLowerCase(charA) != Character.toLowerCase(charB)) {
		return false;
	    }
	}
    }*/

    private ListedSet<Entry<String, T>> getData() {
	return data != null ? data : (data = new ArrayListedSet<>());
    }
   
    class FileEntry implements Entry<String, T> {
	
	private final String key;
	private SoftReference<T> value;

	public FileEntry(String key) {
	    this.key = getKeyConverter().to(key);
	}

	@Override
	public String getKey() {
	    return key;
	}

	@Override
	public T getValue() {
	    T resource = (value != null) ? value.get() : null;
	    if(resource == null) {
		resource = initValue();
		value = new SoftReference<>(resource);
	    }
	    return resource;
	}
        
        protected T initValue() {
            return Exceptions.execute(() -> 
                    readFile(Paths.get(getPath().toString(), getKeyConverter().from(key)).toFile()));
        }

	@Override
	public final T setValue(T value) {
	    throw new UnsupportedOperationException("setValue");
	}

    }

    /*@Override
    public T put(String key, T value) {
	T old = map.put(key, value);
	
	File file = findFile(key);
	if(file == null) {
	    file = Paths.get(getPath().toString(), key).toFile();
	}
	
	try {
	    write(file, value);
	} catch(IOException ex) {
	    throw new RuntimeIOException(ex);
	}
	return old;
    }*/

}
