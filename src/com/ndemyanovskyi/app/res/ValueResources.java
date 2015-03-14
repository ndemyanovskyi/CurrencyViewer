/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ndemyanovskyi.app.res;

import com.ndemyanovskyi.collection.Collections;
import com.ndemyanovskyi.collection.list.UniqueArrayList;
import com.ndemyanovskyi.app.localization.Language;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public abstract class ValueResources<T> extends Resources<T> {

    static final String EXTENSION = "xml";
    
    private UniqueArrayList<Entry<String, T>> data;
    
    private final String name;
    private final UniqueArrayList<String> elementTags;

    ValueResources(Language language, String name, Collection<String> elementTags) {
	super(language, Paths.get("res", "values", 
		language.tag().toLowerCase(), name + "." + EXTENSION));
	this.elementTags = new UniqueArrayList<>(
		Collections.requireNonEmpty(elementTags));

	this.name = Objects.requireNonNull(name);
	read();
    }

    private UniqueArrayList<Entry<String, T>> data() {
	return data != null ? data : (data = new UniqueArrayList<>());
    }

    protected abstract T convert(String nodeName, String text);
    
    protected String convert(T value) {
	return Objects.toString(value);
    }

    @Override
    protected Set<Entry<String, T>> defaultEntrySet() {
	return getDefaultResources() != this 
		? getDefaultResources().data 
		: data;
    }

    /*@Override
    public void flush() {
	try {
	    Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

	    Element root = doc.createElement(name);
	    for(Entry<String, T> entry : entrySet()) {
		Element e = doc.createElement(getTag(elementTags.unmodifiable(), entry.getValue()));
		e.appendChild(doc.createTextNode(convert(entry.getValue())));
		e.setAttribute("name", entry.getKey());
		root.appendChild(e);
	    }
	    doc.appendChild(root);
	    Transformer tr = TransformerFactory.newInstance().newTransformer();
	    tr.setOutputProperty(OutputKeys.INDENT, "yes");
	    tr.setOutputProperty(OutputKeys.METHOD, "xml");
	    tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	    //tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "roles.dtd");
	    tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

	    // send DOM to file
	    tr.transform(new DOMSource(doc),
		    new StreamResult(new FileOutputStream(getPath().toFile())));

	} catch(TransformerException | IOException | ParserConfigurationException ex) {
	    throw new RuntimeException(ex);
	}
    }*/

    @Override
    public abstract ValueResources<T> getParentResources();

    @Override
    public abstract ValueResources<T> getDefaultResources();

    public final void read() {
	if(Files.exists(getPath())) {
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    try {
		Document db = dbf.newDocumentBuilder().parse(getPath().toFile());

		for(String tag : elementTags) {
		    NodeList list = db.getElementsByTagName(tag);
		    for(int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			String key = node.getAttributes().getNamedItem("name").getTextContent();
			T value = convert(node.getNodeName(), node.getTextContent());
			data().add(new ValueEntry(key, value));
		    }
		}
	    } catch(ParserConfigurationException | SAXException | IOException ex) {
		throw new RuntimeException(ex);
	    }
	}
    }

    @Override
    protected Entry<String, T> getEntry(String key) {
	if(data != null && !data.isEmpty()) {
	    for(int i = 0; i < data.size(); i++) {
		Entry<String, T> e = data.get(i);
		if(e.getKey().equals(key)) {
		    return e;
		}
	    }
	}
	
	return getParentResources().getEntry(key);
    }

    @Override
    public final int size() {
	ValueResources<?> res = getDefaultResources();
	return res.data != null ? res.data.size() : 0;
    }
    
    protected final class ValueEntry implements Entry<String, T> {
	
	private final String key;
	private final T value;

	public ValueEntry(String key, T value) {
	    this.key = key;
	    this.value = value;
	}

	@Override
	public T getValue() {
	    return value;
	}

	@Override
	public String getKey() {
	    return key;
	}

	@Override
	public T setValue(T value) {
	    throw new UnsupportedOperationException("setValue");
	}

	@Override
	public boolean equals(Object obj) {
	    return obj instanceof Entry && key.equals(((Entry) obj).getKey());
	}
	
    }

}
