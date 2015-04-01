package com.ndemyanovskyi.app;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



import com.ndemyanovskyi.app.Manifest.Key;
import com.ndemyanovskyi.app.localization.binding.ImageResourceBindings;
import com.ndemyanovskyi.app.localization.binding.ResourceBindings;
import com.ndemyanovskyi.app.res.Resources;
import com.ndemyanovskyi.collection.set.ArrayListedSet;
import com.ndemyanovskyi.map.unmodifiable.UnmodifiableMap;
import com.ndemyanovskyi.map.unmodifiable.UnmodifiableMapWrapper;
import com.ndemyanovskyi.reflection.Types;
import com.ndemyanovskyi.throwable.Exceptions;
import com.ndemyanovskyi.util.Converter;
import com.ndemyanovskyi.util.ThrowableConverter;
import com.ndemyanovskyi.util.Unmodifiable;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public final class Manifest extends UnmodifiableMap<Key<?>, Object> {
    
    public static final Key<Class<? extends Parent>> MAIN_CONTENT = 
            new Key<>("main_content", text -> {
        Class<?> type = Class.forName(text);
        if(Parent.class.isAssignableFrom(type)) {
            return (Class<? extends Parent>) type;
        }
        throw new ClassCastException(
                "Class " + type.getName() + " can`t be cast to Class<? extends Parent>.");
    });
    
    public static final Key<Class<EventHandler<? super ErrorEvent>>> ERROR_HANDLER = 
            new Key<>("error_handler", text -> {
        Class<?> clazz = Class.forName(text);
        Type type = Types.resolveGenericType(EventHandler.class, clazz);
        if(type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            Type arg = pType.getActualTypeArguments()[0];
            if(arg instanceof Class
                    && ((Class<?>) arg).isAssignableFrom(ErrorEvent.class)) {
                return (Class) clazz;
            }
        }
        throw new ClassCastException(
                "Class " + clazz.getName() + " can`t be cast to Class<EventHandler<? super ErrorEvent>>.");
    });
    
    public static final Key<Set<ReadOnlyObjectProperty<Image>>> APP_ICON
	    = new Key<>("app_icon", text -> {
		Set<ReadOnlyObjectProperty<Image>> set = new ArrayListedSet<>();
		ImageResourceBindings images = ResourceBindings.images();
		int[] sizes = { 8, 16, 32, 36, 16, 64, 72, 128, 256 };

		for(int size : sizes) {
		    String name = text + size + "x" + size;
		    if(images.containsKey(name)) {
			set.add(images.get(name));
		    }
		}
		if(images.containsKey(text)) {
		    set.add(images.get(text));
		}

		if(images.isEmpty()) {
		    throw new IllegalArgumentException(
			    "Images with prefix '" + text + "' does not found.");
		}
		return Unmodifiable.set(set);
	    });
    
    public static final Key<ReadOnlyObjectProperty<String>> APP_NAME = 
            new Key<>("app_name", text -> ResourceBindings.strings().get(text));
    
    private static final Manifest INSTANCE = new Manifest();

    private Manifest() {
	super(new HashMap<>());
	Exceptions.execute(() -> {
	    read(base(), Resources.getStream("/manifest.xml"));
	});
    }

    @Override
    protected Map<Key<?>, Object> base() {
	return super.base();
    }
    
    public <T> T get(Key<T> key) {
        return (T) get((Object) key);
    }

    public static Manifest getInstance() {
	return INSTANCE;
    }
    
    private static void read(Map<Key<?>, Object> map, InputStream stream) throws Throwable {
	Document db = DocumentBuilderFactory.newInstance().
		newDocumentBuilder().parse(stream);
	
	NodeList list = db.getElementsByTagName("item");
	for(int i = 0; i < list.getLength(); i++) {
	    Node node = list.item(i);
	    String keyName = node.getAttributes().
		    getNamedItem("name").getTextContent();
            Key<?> key = Key.values().get(keyName);
            if(key == null) {
                throw new IllegalArgumentException(String.format(
                        "Key(%s) does not defined. His values can`t be converted.", keyName));
            }
            
	    map.put(key, key.convert(node.getTextContent()));
	}
    }
    
    public static class Key<T> {
        
        private static final UnmodifiableMapWrapper<String, Key<?>> VALUES = 
                new UnmodifiableMapWrapper<>(new HashMap<>());
        
        private final String value;
        private final ThrowableConverter<String, T, Throwable> converter;

        private Key(String value) {
            this(value, Converter.unsupported());
        }

        private Key(String value, ThrowableConverter<String, T, Throwable> converter) {
            this.value = Objects.requireNonNull(value, "value");
            this.converter = Objects.requireNonNull(converter, "converter");
            VALUES.put(value, this);
        }

        public String get() {
            return value;
        }
        
        public T convert(String text) throws Throwable {
            return converter.to(text);
        }

        @Override
        public String toString() {
            return get();
        }
        
        public static Map<String, Key<?>> values() {
            return VALUES.unmodifiable();
        }
        
    }

}
