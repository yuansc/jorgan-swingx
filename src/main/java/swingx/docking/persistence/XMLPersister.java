/*
 * swingx - Swing eXtensions
 * Copyright (C) 2004 Sven Meier
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package swingx.docking.persistence;

import java.awt.Rectangle;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.swing.JComponent;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import swingx.docking.Bridge;
import swingx.docking.Dock;
import swingx.docking.Dockable;
import swingx.docking.Docking;
import swingx.docking.DockingPane;
import swingx.docking.Persister;
import swingx.docking.Slice;

/**
 * <code>Persister</code> implementation that persists the state of a
 * {@link DockingPane} to XML.
 */
public class XMLPersister extends Persister {

    private static String NONE = "";
    
    private static Attributes EMPTY_ATTRIBUTES = new AttributesImpl();           
	
    private String version;
    	
    /**
     * The writer to write XML to.
     */
    private Writer writer;
    
    /**
     * The reader to read XML from.
     */
    private Reader reader;
    
    private List<Docking> dockings;
    
    /**
     * Create a new persister that parses the state of the given <code>DockingPane</code>
     * from the given reader.
     * 
     * @param dockingPane   the <code>DockingPane</code> to load state for 
     * @param reader        the reader to parse from
     */
    public XMLPersister(DockingPane dockingPane, Reader reader, String version) {
        super(dockingPane);
        
        this.reader = reader;
        this.version = version;
    }
    
    /**
     * Create a new persister that writes the state of the given <code>DockingPane</code>
     * to the given writer.
     * 
     * @param dockingPane   the <code>DockingPane</code> to save state for 
     * @param writer        the writer to write to
     */
    public XMLPersister(DockingPane dockingPane, Writer writer, String version) {
        super(dockingPane);
        
        this.writer = writer;
        this.version = version;
    }

    protected List<Docking> loadDockings() throws IOException {
        if (reader == null ) {
            throw new IllegalStateException("no reader");
        }

        try {
			InputSource source = new InputSource(reader);        
			
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(source, new SAXIn());
		} catch (ParserConfigurationException e) {
			throw new Error(e);
		} catch (SAXException e) {
			IOException ex = new IOException(e.getMessage());
			ex.initCause(e);
			throw ex;
		}
        
        return dockings;
    }
    
    protected void saveDockings(List<Docking> dockings) throws IOException {
        if (writer == null ) {
            throw new IllegalStateException("no writer");
        }
        
        this.dockings = dockings;
        
        try {
			Source source = new SAXSource(new SAXOut(), new InputSource());
			Result target = new StreamResult(writer);
			
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(source, target);
		} catch (TransformerException e) {
			IOException ex = new IOException(e.getMessage());
			ex.initCause(e);
			throw ex;
		}
    }

    /**
     * Format the given key, may be overriden by subclasses that want to
     * support arbitrary key classes.
     * 
     * @param key   key to format, must be an instance of <code>String</code>
     * @return      formatted key
     * @throws SAXException if key is no <code>String</code> instance
     */
    protected String formatKey(Object key) throws SAXException {
        if (key instanceof String) {
            return (String)key;
        } else {
            throw new SAXException("String keys supported only");
        }
    }
    
    /**
     * Parse the given key, may be overriden by subclasses that want to
     * support arbitrary key classes.
     * 
     * @param key   key to parse
     * @return      parsed key
     * @throws SAXException never
     */
    protected Object parseKey(String key) throws SAXException {
        return key;
    }
    
    private class SAXOut implements XMLReader {

    	/**
         * The <code>ContentHandler</code> of the <code>Transformer</code>.
         */
        private ContentHandler handler;
        
        public void parse(InputSource input) throws IOException, SAXException {
            handler.startDocument();

            AttributesImpl attributes = new AttributesImpl();
            if (version != null) {
                attributes.addAttribute(NONE, NONE, "version", NONE, version);
            }

            handler.startElement(NONE, NONE, "dockingPane", attributes);

            for (int d = 0; d < dockings.size(); d++) {
                write(dockings.get(d), handler);
            }

            handler.endElement(NONE, NONE, "dockingPane");

            handler.endDocument();
        }        

        protected void write(JComponent component, ContentHandler handler) throws SAXException {
            if (component instanceof Docking) {
                Docking docking = (Docking)component;

                AttributesImpl attributes = new AttributesImpl();
                Rectangle screenBounds = docking.getScreenBounds();
                attributes.addAttribute(NONE, NONE, "x"     , NONE, Integer.toString(screenBounds.x));
                attributes.addAttribute(NONE, NONE, "y"     , NONE, Integer.toString(screenBounds.y));
                attributes.addAttribute(NONE, NONE, "width" , NONE, Integer.toString(screenBounds.width));
                attributes.addAttribute(NONE, NONE, "height", NONE, Integer.toString(screenBounds.height));

                handler.startElement(NONE, NONE, "docking", attributes);
                
                write(docking.getRoot(), handler);          

                handler.endElement(NONE, NONE, "docking");
            } else if (component instanceof Slice) {
                Slice slice = (Slice)component;

                AttributesImpl attributes = new AttributesImpl();
                attributes.addAttribute(NONE, NONE, "orientation", NONE, Integer.toString(slice.getOrientation()));
                attributes.addAttribute(NONE, NONE, "weight"  , NONE, Double.toString(slice.getWeight()));

                handler.startElement(NONE, NONE, "slice", attributes);
                
                write(slice.getMain(), handler);          
                write(slice.getRemainder(), handler);

                handler.endElement(NONE, NONE, "slice");
            } else if (component instanceof Dock) {
                Dock dock = (Dock)component;
              
                handler.startElement(NONE, NONE, "dock", EMPTY_ATTRIBUTES);

                List<Object> keys = dock.getDockableKeys();
                for (int k = keys.size() - 1; k >= 0; k--) {
                    Object   key      = keys.get(k);
                    Dockable dockable = dock.getDockable(key);
                    
                    AttributesImpl attributes = new AttributesImpl();
                    attributes = new AttributesImpl();
                    attributes.addAttribute(NONE, NONE, "key", NONE, formatKey(key));
                    if (dockable == null) {
                        attributes.addAttribute(NONE, NONE, "null", NONE, "true");
                    }
                    if (dockable != null && dockable == dock.getSelectedDockable()) {
                        attributes.addAttribute(NONE, NONE, "selected", NONE, "true");
                    }

                    handler.startElement(NONE, NONE, "dockable", attributes);
                    handler.endElement(NONE, NONE, "dockable");
                }
                
                handler.endElement(NONE, NONE, "dock");
            } else if (component instanceof Bridge) {
                Bridge bridge = (Bridge)component;
                Object     key     = bridge.getKey();
                JComponent bridged = bridge.getBridged();
                  
                AttributesImpl attributes = new AttributesImpl();
                attributes.addAttribute(NONE, NONE, "key", NONE, formatKey(key));
                if (bridged == null) {
                    attributes.addAttribute(NONE, NONE, "null", NONE, "true");
                }

                handler.startElement(NONE, NONE, "bridge", attributes);
                handler.endElement(NONE, NONE, "bridge");
            } else {
                throw new SAXException("unknown component " + component);
            }        
        }

        public ContentHandler getContentHandler() {
            return handler;
        }

        public void setContentHandler(ContentHandler handler) {
            this.handler = handler;
        }
        
        public DTDHandler getDTDHandler() { return null; }

        public EntityResolver getEntityResolver() { return null; }

        public ErrorHandler getErrorHandler() { return null; }

        public boolean getFeature(String name) { return false; }

        public Object getProperty(String name) { return null; }

        public void parse(String systemId) throws IOException, SAXException { }
        
        public void setDTDHandler(DTDHandler handler) { }

        public void setEntityResolver(EntityResolver resolver) { }

        public void setErrorHandler(ErrorHandler handler) { }

        public void setFeature(String name, boolean value) { }

        public void setProperty(String name, Object value) { }
    }
    
    private class SAXIn extends DefaultHandler {
        
        private Stack<Object> stack = new Stack<Object>();
        
        public void startElement(String namespaceURI, String localName,
                String qName, Attributes atts) throws SAXException {

            if ("dockingPane".equals(qName)) {
            	if (version != null && !version.equals(atts.getValue("version"))) {
            		throw new SAXException("invalid version " + atts.getValue("version") + " != " + version);
            	}
                dockings = new ArrayList<Docking>();
                stack.push(dockings);
            } else if ("docking".equals(qName)) {
                Docking docking = createDocking();

                Rectangle screenBounds = new Rectangle();
                screenBounds.x      = Integer.parseInt(atts.getValue("x"));
                screenBounds.y      = Integer.parseInt(atts.getValue("y"));
                screenBounds.width  = Integer.parseInt(atts.getValue("width"));
                screenBounds.height = Integer.parseInt(atts.getValue("height"));

                docking.setScreenBounds(screenBounds);
                
                dockings.add(docking);
                
                stack.push(docking);
            } else if ("slice".equals(qName)) {
                Slice slice = createSlice();

                slice.setOrientation(Integer.parseInt(atts.getValue("orientation")));
                slice.setWeight  (Float.parseFloat(atts.getValue("weight")));
                
                add(slice);
                
                stack.push(slice);
            } else if ("dock".equals(qName)) {
                Dock dock = createDock();

                add(dock);

                stack.push(dock);
            } else if ("bridge".equals(qName)) {
                Object key = parseKey(atts.getValue("key"));
                Bridge bridge = createBridge();

                JComponent bridged = null;
                if (!new Boolean(atts.getValue("null")).booleanValue()) {
                    bridged = resolveComponent(key);
                }
                bridge.setBridged(key, bridged);
                
                add(bridge);
                
                stack.push(bridge);
            } else if ("dockable".equals(qName)) {
                Object key = parseKey(atts.getValue("key"));
                Dockable dockable = null;
                if (!new Boolean(atts.getValue("null")).booleanValue()) {
                    dockable = resolveDockable(key);
                }
                
                Dock dock = (Dock)stack.peek();
                dock.putDockable(key, dockable);
                if (dockable != null && new Boolean(atts.getValue("selected")).booleanValue()) {
                    dock.setSelectedDockable(dockable);
                }
                
                stack.push(dockable);
            } else {
                throw new SAXException("unkown qName '" + qName + "'");
            }
        }
       
        private void add(JComponent component) throws SAXException {
            JComponent parent = (JComponent)stack.peek();
                
            if (parent instanceof Docking) {
                Docking docking = (Docking)parent;
                docking.setRoot(component);
            } else if (parent instanceof Slice) {
                Slice slice = (Slice)parent;
                if (slice.getMain() == null) {
                    slice.setMain(component);
                } else if (slice.getRemainder() == null) {
                    slice.setRemainder(component);
                } else {
                    throw new SAXException("unexpected additional child");
                }
            } else {
                throw new SAXException("unexpected parent");
            }
        }
  
        public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
            stack.pop();
        }
    }
}
