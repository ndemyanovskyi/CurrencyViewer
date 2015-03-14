/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ndemyanovskyi.app.res;

import com.ndemyanovskyi.collection.list.UniqueArrayList;
import com.ndemyanovskyi.throwable.RuntimeIOException;
import com.ndemyanovskyi.app.localization.Language;
import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;


public abstract class FileResources<T> extends Resources<T> {
    
    private UniqueArrayList<Entry<String, T>> data;
    
    FileResources(Language language, String folder) {
	super(language, Paths.get("res", "files", 
		Objects.requireNonNull(folder, "folder"), language.tag().toLowerCase()));
	if(Files.exists(getPath())) {
	    for(String name : getPath().toFile().list()) {
		data().add(new FileEntry(name));
	    }
	}
    }

    @Override
    protected Set<Entry<String, T>> defaultEntrySet() {
	return getDefaultResources() != this 
		? getDefaultResources().data 
		: data;
    }

    @Override
    public boolean containsKey(Object keyObject) {
	if(keyObject == null || !(keyObject instanceof String)) return false;
	return findEntry((String) keyObject) != null;
    }
    
    protected abstract T read(File file) throws IOException;

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

    private UniqueArrayList<Entry<String, T>> data() {
	return data != null ? data : (data = new UniqueArrayList<>());
    }
   
    private class FileEntry implements Entry<String, T> {
	
	private final String key;
	private SoftReference<T> value;

	public FileEntry(String key) {
	    this.key = key;
	}

	@Override
	public String getKey() {
	    return key;
	}

	@Override
	public T getValue() {
	    T resource = (value != null) ? value.get() : null;
	    if(resource == null) {
		try {
		    resource = read(Paths.get(getPath().toString(), key).toFile());
		} catch(IOException ex) {
		    throw new RuntimeIOException(ex);
		}
		value = new SoftReference<>(resource);
	    }
	    return resource;
	}

	@Override
	public T setValue(T value) {
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
