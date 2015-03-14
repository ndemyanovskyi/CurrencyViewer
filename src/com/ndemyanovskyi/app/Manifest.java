package com.ndemyanovskyi.app;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



import com.ndemyanovskyi.map.unmodifiable.UnmodifiableMap;
import com.ndemyanovskyi.reflection.Types;
import com.ndemyanovskyi.throwable.Exceptions;
import com.ndemyanovskyi.util.Unmodifiable;
import com.ndemyanovskyi.app.res.Resources;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.stage.StageStyle;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public final class Manifest extends UnmodifiableMap<String, Object> {
    
    public static final String MAIN_CONTENT_TYPE_KEY = "main_content_type";
    public static final String MAIN_STAGE_STYLE_KEY = "main_stage_style";
    public static final String ERROR_HANDLER_TYPE_KEY = "error_handler_type";
    
    public static final Set<String> KEYS = 
            Unmodifiable.set(MAIN_CONTENT_TYPE_KEY, ERROR_HANDLER_TYPE_KEY, MAIN_STAGE_STYLE_KEY);
    
    private static final Manifest INSTANCE = new Manifest();

    private Manifest() {
	super(new HashMap<>());
	Exceptions.execute(() -> {
	    read(base(), Resources.getStream("/manifest.xml"));
	});
    }

    @Override
    protected Map<String, Object> base() {
	return super.base();
    }
    
    @SuppressWarnings("unchecked")
    public Class<? extends Parent> getMainContentType() {
	return (Class<? extends Parent>) get(MAIN_CONTENT_TYPE_KEY);
    }
    
    @SuppressWarnings("unchecked")
    public Class<EventHandler<? super ErrorEvent>> getErrorHandlerType() {
	return (Class<EventHandler<? super ErrorEvent>>) get(ERROR_HANDLER_TYPE_KEY);
    }
    
    public StageStyle getMainStageStyle() {
	return (StageStyle) get(MAIN_STAGE_STYLE_KEY);
    }

    public static Manifest getInstance() {
	return INSTANCE;
    }
    
    private static void read(Map<String, Object> map, InputStream stream) throws Exception {
	Document db = DocumentBuilderFactory.newInstance().
		newDocumentBuilder().parse(stream);
	
	NodeList list = db.getElementsByTagName("item");
	for(int i = 0; i < list.getLength(); i++) {
	    Node node = list.item(i);
	    String name = node.getAttributes().
		    getNamedItem("name").getTextContent();
	    map.put(name, convert(name, node.getTextContent()));
	}
    }
    
    private static Object convert(String key, String text) throws Exception {
	switch(key) {
	    
	    case MAIN_CONTENT_TYPE_KEY: {
		Class<?> clazz = Class.forName(text);
                
		if(Parent.class.isAssignableFrom(clazz)) {
                    return clazz;
                }
                
                throw new ClassCastException(
                        "Class " + clazz.getName() + " can`t be cast to Class<? extends Parent>.");
	    } 
	    
	    case MAIN_STAGE_STYLE_KEY: {
		return StageStyle.valueOf(text.toUpperCase());
	    } 
	    
	    case ERROR_HANDLER_TYPE_KEY: {
		Class<?> clazz = Class.forName(text);
                
                Type type = Types.resolveGenericType(EventHandler.class, clazz);
                if(type instanceof ParameterizedType) {
                    ParameterizedType pType = (ParameterizedType) type;
                    Type arg = pType.getActualTypeArguments()[0];
                    if(arg instanceof Class
                            && ((Class<?>) arg).isAssignableFrom(ErrorEvent.class)) {
                        return clazz;
                    }
                }
                
                throw new ClassCastException(
                        "Class " + clazz.getName() + " can`t be cast to Class<EventHandler<? super ErrorEvent>>.");
	    } 
	    
	    default: return text;
	}
    }

}
