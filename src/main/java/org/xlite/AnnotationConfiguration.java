/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite;

import org.xlite.converters.*;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main configuration interface to Xlite.
 * Configures mappings between Classes and XML via a set of annotations that map XML elements to class fields.
 * <p/>
 * <b>Example usage:</b>
 * <pre>
 * Configuration conf = new AnnotationConfiguration(One.class);  // intialize and define first class mapping
 * conf.addMapping(Two.class);                                   // add second class mapping
 * </pre>
 * Please look at Xlite documentation for more detailed explanation of mapping configuration via annotations.
 */
public class AnnotationConfiguration implements Configuration {

    private MappingContext mappingContext;

    private volatile boolean initialized = false;

    private Map<Class, QName> rootMappings = new HashMap<Class, QName>();

    private Map<QName, RootMapper> mappers = new HashMap<QName, RootMapper>();

    private boolean isPrettyPrint = true;

    private List<ValueConverter> customValueConverters;

    private List<ElementConverter> customElementConverters;


    /**
     * Initializes an empty AnnotationConfiguration
     */
    public AnnotationConfiguration() {
        this(null);
    }

    /**
     * Initializes AnnotationConfiguration and adds a class mapping.
     *
     * @param rootClass
     */
    public AnnotationConfiguration(Class rootClass) {
        try {
            Class.forName("javax.xml.stream.XMLOutputFactory", false, this.getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new XliteException("Error initializing XMLOutputFactory. If you are using Xlite on a JDK 1.5, " +
                    "then you must have XML stream library (javax.xml.stream) in your path. ", e);
        }

        // setup converters
        List<ValueConverter> valueConverters = setupValueConverters();
        List<ElementConverter> elementConverters = setupElementConverters(valueConverters);

        // add a mapping
        if (rootClass != null) {
            rootMappings.put(rootClass, null);
        }

        this.mappingContext = new MappingContext(elementConverters, valueConverters);
    }

    /**
     * Returns a RootMapper mapped to given root XML element.
     *
     * @param rootElementName
     * @return
     */
    public RootMapper getRootElementMapper(QName rootElementName) {
        return mappers.get(rootElementName);
    }

    /**
     * Returns a RootMapper mapped to given root class.
     *
     * @param rootClass
     * @return
     */
    public RootMapper getRootElementMapper(Class rootClass) {
        QName rootName = rootMappings.get(rootClass);
        if (rootName == null) {
            return null;
        }
        return mappers.get(rootName);
    }

    /**
     * Is output XML formatted in a human-readable form?
     *
     * @return
     */
    public boolean isPrettyPrint() {
        return isPrettyPrint;
    }

    /**
     * Sets output XML to be formatted in a human-readable form.
     *
     * @param prettyPrint
     */
    public synchronized void setPrettyPrint(boolean prettyPrint) {
        if (initialized) {
            throw new XliteConfigurationException("Error: Trying to add configuration parameters " +
                    "after first use. Once Xlite is used (.fromXml() or similar is called)," +
                    " configuration parameters can not be altered.");
        }
        this.isPrettyPrint = prettyPrint;
    }

    /**
     * Called internally on first use of Xlite class.
     * No configuration changes are allowed after initialization.
     */
    public synchronized void initialize() {

        // one-time initialization
        if (!initialized) {

            for (Class mappedClass : rootMappings.keySet()) {
                QName elementName = processRootElementName(mappedClass);
                rootMappings.put(mappedClass, elementName);
                mappers.put(elementName, new RootMapper(elementName, mappedClass, mappingContext));
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
     * @param namespace
     */
    public synchronized void addNamespace(String prefix, String namespace) {
        if (initialized) {
            throw new XliteConfigurationException("Error: Trying to add configuration parameters after first use. " +
                    "Once Xlite is used (.fromXml() or similar is called), configuration parameters can not be altered.");
        }
        mappingContext.addNamespace(prefix, namespace);
    }

    /**
     * Adds a class to the mapping confuguration.
     *
     * @param rootClass
     */
    public void addMapping(Class rootClass) {
        // check for duplicate mappings
        if (rootMappings.containsKey(rootClass)) {
            throw new XliteConfigurationException("Error: Duplicate mapping of root class. Class " +
                    rootClass.getName() + " is already mapped.");
        }
        rootMappings.put(rootClass, null);
    }

    public void addConverter(ValueConverter converter) {
        mappingContext.addConverter(converter);
        mappingContext.addConverter(new ValueConverterWrapper(converter));
    }

    public void addConverter(ElementConverter converter) {
        mappingContext.addConverter(converter);
    }

    /**
     * Inspects a class for the @RootElement annotations
     *
     * @param currentClass Class to be processed.
     * @return QName that the class maps to.
     */
    private QName processRootElementName(Class currentClass) {

        RootElement rootElement = (RootElement) currentClass.getAnnotation(RootElement.class);

        // read the name value from @RootElement annotation
        String elementName = rootElement.name().length() != 0 ? rootElement.name() : rootElement.value();

        // if element name is not defined via annotations then construct it from class name
        // e,g, Root.class yields 'root' element name
        if (elementName.length() == 0) {
            elementName = currentClass.getSimpleName().toLowerCase();
        }

        Namespaces nsAnnotation = (Namespaces) currentClass.getAnnotation(Namespaces.class);

        return getRootQName(nsAnnotation, elementName);
    }

    private QName getRootQName(Namespaces namespaceAnnotations, String elementName) {

        // split xml node name into prefix and local part
        int index = elementName.indexOf(':');
        String rootElementLocalpart;
        String rootElementPrefix;
        if (index > 0) {  // with prefix ("prefix:localpart")
            rootElementPrefix = elementName.substring(0, index);
            rootElementLocalpart = elementName.substring(index + 1, elementName.length());

        } else if (index == 0) { // empty prefix (no prefix defined - e.g ":nodeName")
            rootElementPrefix = XMLConstants.DEFAULT_NS_PREFIX;
            rootElementLocalpart = elementName.substring(1, elementName.length());

        } else { // no prefix given
            rootElementPrefix = XMLConstants.DEFAULT_NS_PREFIX;
            rootElementLocalpart = elementName;
        }

        // search for namespaces defined on root class
        NsContext classNS = new NsContext();
        if (namespaceAnnotations != null && namespaceAnnotations.value().length != 0) {
            for (int i = 0; i < namespaceAnnotations.value().length; i++) {
                classNS.addNamespace(namespaceAnnotations.value()[i]);
            }
        }

        // look for namespace with given prefix
        String rootElementNS = classNS.getNamespaceURI(rootElementPrefix);

        // if namespace not defined on class, look within predefined namespaces
        if (rootElementNS == null) {
            rootElementNS = mappingContext.getPredefinedNamespaces().getNamespaceURI(rootElementPrefix);
        }

        QName rootName = new QName(rootElementNS, rootElementLocalpart, rootElementPrefix);
        return rootName;
    }

}
