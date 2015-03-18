package com.ndemyanovskyi.app;

import com.ndemyanovskyi.app.localization.Language;
import com.ndemyanovskyi.map.unmodifiable.UnmodifiableMap;
import com.ndemyanovskyi.throwable.Exceptions;
import com.ndemyanovskyi.util.BiConverter;
import com.ndemyanovskyi.util.Converter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleObjectProperty;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public final class Settings extends UnmodifiableMap<String, Settings.Setting<?>> implements Flushable {
    
    private static final Logger LOG = Logger.getLogger(Settings.class.getName());

    private static final String ROOT_TAG = "settings";
    private static final String ELEMENT_TAG = "setting";

    private static final Settings INSTANCE = new Settings("settings.xml");

    public static final Setting<Language> LANGUAGE = new Setting<>(
            INSTANCE, "language", Language.getDefault(), Language::valueOfTag, Language::tag);

    public static final Setting<Integer> STORED_DATA_YEARS_COUNT = new Setting<>(
	    INSTANCE, "stored_data_years_count", 1, Integer::parseInt, (Integer i) -> i >= 1);

    static {
	getInstance().read();
    }

    private final File file;
    private final File tempFile;

    private Set<String> keySet;
    private Set<Entry<String, Setting<?>>> entrySet;
    private Collection<Setting<?>> values;
    
    private FlushingThread flushingThread;

    private Settings(String path) {
        super(new HashMap<>());
	this.file = new File(path);
	this.tempFile = new File(path + ".tmp");
    }

    public File getFile() {
	return file;
    }

    File getTempFile() {
        return tempFile;
    }

    private FlushingThread getFlushingThread() {
	return flushingThread != null ? flushingThread : 
		(flushingThread = new FlushingThread());
    }

    @Override
    public void flush() {
	getFlushingThread().flush();
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            flush();
        }finally {
            super.finalize();
        }
    }

    public void read() {
	if(getFile().exists()) {
	    try {
		Document db = DocumentBuilderFactory.
                        newInstance().newDocumentBuilder().parse(getFile());
		NodeList list = db.getElementsByTagName(ELEMENT_TAG);
		for(int i = 0; i < list.getLength(); i++) {
		    Node node = list.item(i);
		    String name = node.getAttributes().
			    getNamedItem("name").getTextContent();

                    if(containsKey(name)) {
                        get(name).set(node.getTextContent());
                    }
		}
	    } catch(IOException | ParserConfigurationException | SAXException ex) {
		LOG.log(Level.WARNING, "Error reading settings file", ex);
                getFile().delete();
	    }
	}
    }

    @Override
    public Setting<?> get(Object key) {
	Objects.requireNonNull(key, "key");

	if(!(key instanceof String)) {
	    throw new IllegalArgumentException(
		    "Key must be instance of String.");
	}

	Setting<?> value = super.get(key.toString());
	if(value == null) {
	    throw new IllegalArgumentException(
		    "Key '" + key + "' not defined in settings.");
	}
	return value;
    }

    public static class Setting<T> extends SimpleObjectProperty<T> {
        
        private static final Predicate NON_NULL_PREDICATE = v -> v != null;

	private final BiConverter<String, T> converter;
	private final T defaultValue;
        private final Predicate<? super T> predicate;

	private Setting(Settings parent, String name, T value, Converter<String, T> to) {
	    this(parent, name, value, to, Object::toString, v -> true);
	}

	private Setting(Settings parent, String name, T value, Converter<String, T> to, Predicate<? super T> predicate) {
	    this(parent, name, value, to, Object::toString, predicate);
	}

	private Setting(Settings parent, String name, T value, Converter<String, T> to, Converter<T, String> from) {
	    this(parent, name, value, to, from, v -> true);
	}

        @SuppressWarnings("unchecked")
	private Setting(Settings parent, String name, T value, Converter<String, T> to, Converter<T, String> from, Predicate<? super T> predicate) {
	    super(parent, name, value);
	    this.converter = BiConverter.of(to, from);
	    this.defaultValue = value;
            this.predicate = NON_NULL_PREDICATE.and(predicate);

	    if(parent.containsKey(name)) {
		throw new IllegalArgumentException(
			"Setting with name '" + name + "' alredy contains in Settings.");
	    }
            
            parent.base().put(name, this);
	}

        @Override
        public void set(T newValue) {
            T oldValue = get();
            if(getPredicate().test(newValue)) {
                super.set(Objects.requireNonNull(newValue, "newValue")); 
            } else {
                throw new IllegalArgumentException(
                        "Setting " + getName() + " can`t be setted: " + newValue);
            }
	    getBean().getFlushingThread().requestFlush();
        }

	public T getDefault() {
	    return defaultValue;
	}

	public BiConverter<String, T> getConverter() {
	    return converter;
	}

        public Predicate<? super T> getPredicate() {
            return predicate;
        }

	private void set(String data) {
	    set(converter.to(data));
	}

	@Override
	public String toString() {
	    return converter.from(get());
	}

        @Override
        public Settings getBean() {
            return (Settings) super.getBean(); 
        }

    }

    public static Settings getInstance() {
	return INSTANCE;
    }

    private class FlushingThread extends Thread {
	
	private DocumentBuilder documentBuilder;
	private Transformer transformer;

	private volatile boolean building = false;
	private volatile boolean writing = false;
	private volatile boolean cancelled = false;
	
	private volatile long time = -1;

	public FlushingThread() {
	    Exceptions.execute(() -> {
		documentBuilder = DocumentBuilderFactory.
			newInstance().newDocumentBuilder();
		transformer = TransformerFactory.
			newInstance().newTransformer();
		
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
	    });
	    start();
	}

	@Override
	public void run() {
	    while(true) {
		if(time != -1) {
		    if(System.currentTimeMillis() - time > 100) {
			DOMSource ds = build();
			if(!cancelled) {
                            write(ds);
                        }
			time = -1;
		    }
		}
		try { sleep(100); } catch(InterruptedException ex) {}
	    }
	}

	public boolean isWriting() {
	    return writing;
	}

	public boolean isBuilding() {
	    return building;
	}
	
	public void requestFlush() {
	    time = System.currentTimeMillis();
	}
	
	public void flush() {
	    if(isBuilding()) {
		cancelled = true;
	    }
	    
	    write(build());
	    time = -1;
	}

	private DOMSource build() {
	    building = true;
	    Document doc = documentBuilder.newDocument();
	    Element root = doc.createElement(ROOT_TAG);
	    for(Setting<?> s : values()) {
		Element e = doc.createElement(ELEMENT_TAG);
		e.appendChild(doc.createTextNode(s.toString()));
		e.setAttribute("name", s.getName());
		root.appendChild(e);
	    }
	    doc.appendChild(root);
	    DOMSource ds = new DOMSource(doc);
	    building = false;
	    return ds;
	}

	private void write(DOMSource ds) {
	    try {
		writing = true;
                
                try (FileOutputStream fos = new FileOutputStream(getTempFile())) {
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                    transformer.transform(build(), new StreamResult(fos));
                    transformer.reset();
                }
                
                getFile().delete();
                getTempFile().renameTo(getFile());
                getTempFile().delete();
                
                LOG.log(Level.INFO, "Setting flushed");
                
		writing = false;
	    } catch(TransformerException | IOException ex) {
		throw new RuntimeException(ex);
	    }
	}

    }

}
