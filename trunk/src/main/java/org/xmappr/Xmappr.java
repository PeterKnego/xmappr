/*
 * This software is released under the BSD license. Full license available at http://xmappr.googlecode.com
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xmappr contributors
 * All rights reserved.
 */
package org.xmappr;

import org.xmappr.converters.*;

import javax.xml.namespace.QName;
import javax.xml.stream.*;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * A facade to Xmappr library, through which all serialization/deserialization calls are made.
 * <p/>
 * <b>Example usage:</b>
 * <pre>
 * Xmappr xmappr = new Xmappr(One.class);                 // initialize Xmappr & define mapping for class One
 * One one = (One) xmappr.fromXml(inputXml);            // deserialize XML to object
 * </pre>
 * Deserialization (XML-to-object) is done via fromXML() and fromXMLwithUnknown() methods.
 * <p/>
 * Serialization (object-to-XML) is done via toXML() method.
 * <h3>Thread safety</h3>
 * Xmappr instance is thread-safe after it is initialized with given configuration.
 * Afterwards no changes to configuration are allowed.
 * Being thread-safe means that Xmappr can be called concurrently from multiple threads, allowing objects to be
 * serialized/deserialized concurrently.
 * <h3>Preserving unmapped XML elements</h3>
 * Xmappr can be configured to internally store all XML subelements that are not mapped to concrete class fields.
 * This way only a small part of a larger XML tree may be mapped to Java object, while preserving the rest of
 * XML from input to output.
 * <p/>
 * <b>Example of storing unamapped elements:</b>
 * <pre>
 * Xmappr xmappr = new Xmappr(One.class);                         // initialize Xmappr & define mapping for class One
 * Xmappr.Result result = xmappr.fromXMLwithUnmapped(inputXml);  // deserialize XML to object while storing unmapped XML
 * <p/>
 * One one = result.getObject();                               // get deserialized object from Result object
 * ObjectStore store = result.getObjectStore()                 // unmapped XML is stored in binary form to ObjectStore
 * <p/>
 * xmappr.toXML(one, store, outputXmlWriter)                    // on serialization, stored objects must be provided
 * </pre>
 * <em>Note: only whole XML subelements are stored and not XML attributes or text elements.
 * XML attributes and text elements must be mapped explicitly via configuration to be preserved from input to output.</em>
 * </pre>
 */
public class Xmappr {

    private final XMLInputFactory xmlInputFactory;
    private final XMLOutputFactory xmlOutputFactory;

    private List<Reader> xmlConfigurations = new ArrayList<Reader>();
    private List<Class> classConfigurations = new ArrayList<Class>();

    public MappingContext mappingContext;
    private MappingBuilder mappingBuilder;

    private volatile boolean initialized = false;

    private boolean isPrettyPrint = false;

    /**
     * Creates a new Xmappr instance.
     */
    public Xmappr() {

        try {
            Class.forName("javax.xml.stream.XMLOutputFactory", false, this.getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new XmapprException("Error initializing XMLOutputFactory. If you are using Xmappr on a JDK 1.5, " +
                    "then you must have XML stream library (javax.xml.stream) in your path. ", e);
        }

        // setup converters
        List<ValueConverter> valueConverters = setupValueConverters();
        List<ElementConverter> elementConverters = setupElementConverters(valueConverters);

        this.mappingContext = new MappingContext(elementConverters, valueConverters);
        this.mappingBuilder = new MappingBuilder(mappingContext);

        XMLInputFactory xmlInputFactory1;
        XMLOutputFactory xmlOutputFactory1;

        try {
            Class<?> clazz = Class.forName("com.ctc.wstx.stax.WstxInputFactory");
            Method newInstanceMethod = clazz.getMethod("newInstance");
            xmlInputFactory1 = (XMLInputFactory) newInstanceMethod.invoke(null);

            Class<?> clazz2 = Class.forName("com.ctc.wstx.stax.WstxOutputFactory");
            Method newInstanceMethod2 = clazz2.getMethod("newInstance");
            xmlOutputFactory1 = (XMLOutputFactory) newInstanceMethod2.invoke(null);

        } catch (Exception e) {
            xmlInputFactory1 = XMLInputFactory.newInstance();
            xmlOutputFactory1 = XMLOutputFactory.newInstance();
        }

        this.xmlInputFactory = xmlInputFactory1;
        this.xmlOutputFactory = xmlOutputFactory1;
        this.xmlOutputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", true);
    }

    public Xmappr(Reader xmlConfiguration) {
        this();
        addMapping(xmlConfiguration);
    }

    public Xmappr(Class rootClass) {
        this();
        addMapping(rootClass);
    }

    /**
     * Reads XML data from provided Reader and returns a new object.
     *
     * @param reader
     * @return A newly instantiated object containing XML mapped data..
     */
    public Object fromXML(Reader reader) {

        initialize();

        XMLStreamReader rdr = getXmlStreamReader(reader);
        XMLSimpleReader simpleReader = new XMLSimpleReader(rdr, false);

        return getRootMapper(simpleReader).getRootObject(simpleReader);
    }

    /**
     * Reads XML data from provided Reader and maps this data to provided object.
     *
     * @param reader XMLStreamReader where XML data will be read from.
     * @param targetObject Object to which XML will be mapped.
     * @return Provided target object. If provided target object was null then new object is created.
     */
    public Object fromXML(Reader reader, Object targetObject) {

        initialize();

        XMLStreamReader rdr = getXmlStreamReader(reader);
        XMLSimpleReader simpleReader = new XMLSimpleReader(rdr, false);

        return getRootMapper(simpleReader).getRootObject(simpleReader, targetObject);
    }

    /**
     * Reads XML data from provided Reader and returns Xmappr.Result.
     *
     * @param reader
     * @return An instance of Xmappr.Reader, containing deserialized object and stored unmapped XML elements.
     */
    public Result fromXMLwithUnmapped(Reader reader) {

        initialize();

        XMLSimpleReader simpleReader = new XMLSimpleReader(getXmlStreamReader(reader), true);

        Object object = getRootMapper(simpleReader).getRootObject(simpleReader);
        return new Result(object, simpleReader.getObjectStore());
    }

    /**
     * Serializes source object to XML and writes it to Writer.
     *
     * @param source Object to be serialized.
     * @param writer Writer from which XML data is read.
     */
    public void toXML(Object source, Writer writer) {

        initialize();

        XMLStreamWriter parser = getXmlStreamWriter(writer);
        XMLSimpleWriter simpleWriter = new XMLSimpleWriter(parser, new XmlStreamSettings(), isPrettyPrint());

        Class sourceClass = source.getClass();
        RootMapper rootMapper = mappingContext.getRootMapper(sourceClass);

        // was the the right RootMapper found?
        if (rootMapper == null) {
            throw new XmapprConfigurationException("Error: No class mapping found for " +
                    "root class: " + sourceClass.getName());
        }
        rootMapper.toXML(source, simpleWriter);
    }

    /**
     * Serializes source object to XML and writes it to Writer.
     * During serialization process also writes stored XML elements so that unmapped XML may be preserved.
     *
     * @param source Object to be serialized.
     * @param store  ObjectStore where unmapped XML elements are stored.
     * @param writer Writer from which XML data is read.
     */
    public void toXML(Object source, ObjectStore store, Writer writer) {

        initialize();

        XMLStreamWriter parser = getXmlStreamWriter(writer);
        XMLSimpleWriter simpleWriter = new XMLSimpleWriter(parser, store, new XmlStreamSettings(), isPrettyPrint());

        Class sourceClass = source.getClass();
        RootMapper rootMapper = mappingContext.getRootMapper(sourceClass);

        // was the the right RootMapper found?
        if (rootMapper == null) {
            throw new XmapprConfigurationException("Error: No class mapping found for " +
                    "root class: " + sourceClass.getName());
        }
        rootMapper.toXML(source, simpleWriter);
    }

    private RootMapper getRootMapper(XMLSimpleReader simpleReader) {
        // read the root XML element name and lookup the right RootMapper
        QName rootName = simpleReader.getRootName();
        RootMapper rootMapper = mappingContext.getRootMapper(rootName);

        // was the the right RootMapper found?
        if (rootMapper == null) {
            throw new XmapprConfigurationException("Error: No class mapping found for " +
                    "root XML element <" + (rootName.getPrefix().length() == 0 ? "" : rootName.getPrefix() + ":")
                    + rootName.getLocalPart() + ">");
        }
        return rootMapper;
    }

    private synchronized XMLStreamReader getXmlStreamReader(Reader reader) {
        XMLStreamReader xmlreader;
        try {
            xmlreader = xmlInputFactory.createXMLStreamReader(reader);
        } catch (XMLStreamException e) {
            throw new XmapprException("Error initalizing XMLStreamReader", e);
        }
        return xmlreader;
    }


    private synchronized XMLStreamWriter getXmlStreamWriter(Writer writer) {
        try {
            return xmlOutputFactory.createXMLStreamWriter(writer);
        } catch (XMLStreamException e) {
            throw new XmapprException("Error initalizing XMLStreamWriter", e);
        }
    }

    /**
     * Is output XML formatted in a human-readable form?
     *
     * @return
     */
    private boolean isPrettyPrint() {
        return isPrettyPrint;
    }

    /**
     * Sets output XML to be formatted in a human-readable form.
     * Pretty print slows down Xmappr considerably, so it should only be used when testing or
     * where human-readable output is needed.
     *
     * @param prettyPrint
     */
    public synchronized void setPrettyPrint(boolean prettyPrint) {
        checkConfigFinished();
        this.isPrettyPrint = prettyPrint;
    }

    /**
     * Called internally on first use of Xmappr class.
     * No configuration changes are allowed after initialization.
     */
    private synchronized void initialize() {

        // one-time initialization
        if (!initialized) {

            // process XML configurations
            for (Reader xmlConfiguration : xmlConfigurations) {
                ConfigRootElement rootConf = ConfigurationProcessor.processConfiguration(xmlConfiguration, mappingContext);
                mappingContext.addRootMapper(rootConf);
            }

            // process annotated Class configurations
            for (Class classConfiguration : classConfigurations) {
                ConfigRootElement rootConf = ConfigurationProcessor.processRootClassAnnotations(classConfiguration, mappingContext);
                mappingContext.addRootMapper(rootConf);
            }

            initialized = true;
        }
    }

    private List<ElementConverter> setupElementConverters(List<ValueConverter> valueConverters) {

        List<ElementConverter> elementConverters = new ArrayList<ElementConverter>();
        elementConverters.add(new CollectionConverter());
        elementConverters.add(new DOMelementConverter());

        // wraps every ValueConverter so that it can be used as a ElementConverter
        for (ValueConverter valueConverter : valueConverters) {
            elementConverters.add(new ValueConverterWrapper(valueConverter));
        }
        return elementConverters;
    }

    private List<ValueConverter> setupValueConverters() {

        List<ValueConverter> valueConverters = new ArrayList<ValueConverter>();

        valueConverters.add(new StringConverter());
        valueConverters.add(new IntConverter());
        valueConverters.add(new DoubleConverter());
        valueConverters.add(new FloatConverter());
        valueConverters.add(new LongConverter());
        valueConverters.add(new ShortConverter());
        valueConverters.add(new BooleanConverter());
        valueConverters.add(new ByteConverter());
        valueConverters.add(new CharConverter());
        valueConverters.add(new BigDecimalConverter());
        valueConverters.add(new BigIntegerConverter());
        valueConverters.add(new ByteArrayConverter());
        valueConverters.add(new DateConverter());
        valueConverters.add(new EnumConverter());

        return valueConverters;
    }

    /**
     * Adds a XML namespace to the list of predefined namespaces.
     * Predefined namespaces apply to all classes/fields in the mapping configuration.
     *
     * @param prefix
     * @param namespace
     */
    public synchronized void addNamespace(String prefix, String namespace) {
        checkConfigFinished();
        mappingContext.addNamespace(prefix, namespace);
    }

    /**
     * Adds a XML namespace to the list of predefined namespaces.
     * Predefined namespaces apply to all classes/fields in the mapping configuration.
     *
     * @param compoundNamespace Namespace definition must be form "prefix=namespaceURI"
     */
    public synchronized void addNamespace(String compoundNamespace) {
        checkConfigFinished();
        mappingContext.addNamespace(compoundNamespace);
    }

    private void checkConfigFinished() {
        if (initialized) {
            throw new XmapprConfigurationException("Error: Trying to change configuration parameters after first use. " +
                    "Once Xmappr is used (.fromXml() or similar is called), configuration parameters can not be altered.");
        }
    }

    /**
     * Adds a class to the mapping configuration.
     *
     * @param rootClass
     */
    public void addMapping(Class rootClass) {
        checkConfigFinished();
        classConfigurations.add(rootClass);
    }

    /**
     * Processes XML mapping configuration.
     *
     * @param xmlConfiguration
     */
    public void addMapping(Reader xmlConfiguration) {
        checkConfigFinished();
        xmlConfigurations.add(xmlConfiguration);
    }

    public void addConverter(ValueConverter converter) {
        checkConfigFinished();
        mappingContext.addConverter(converter);
        mappingContext.addConverter(new ValueConverterWrapper(converter));
    }

    public void addConverter(ElementConverter converter) {
        checkConfigFinished();
        mappingContext.addConverter(converter);
    }

    public String getXmlConfiguration(Class rootClass) {

        initialize();

        ConfigRootElement confElement = mappingContext.lookupConfigRootElement(rootClass);

        if (confElement == null) {
            throw new XmapprConfigurationException("Error: No XML mappings found for class " + rootClass.getName());
        }

        Xmappr xmappr = new Xmappr(ConfigRootElement.class);
        xmappr.setPrettyPrint(true);
        StringWriter sw = new StringWriter();
        xmappr.toXML(confElement, sw);

        return sw.toString();
    }

    /**
     * Container class to hold deserialized Object and unmapped XML elements.
     */
    public static class Result {
        private ObjectStore store;
        private Object object;

        public Result(Object object, ObjectStore store) {
            this.store = store;
            this.object = object;
        }

        public ObjectStore getStore() {
            return store;
        }

        public Object getObject() {
            return object;
        }

    }

}





