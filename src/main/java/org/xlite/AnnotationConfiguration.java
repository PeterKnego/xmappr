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

public class AnnotationConfiguration implements Configuration {

    private MappingContext mappingContext;

    private volatile boolean initialized = false;

    private Map<Class, QName> rootMappings = new HashMap<Class, QName>();

    private Map<QName, RootMapper> mappers = new HashMap<QName, RootMapper>();

    private boolean isPrettyPrint = true;

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
        rootMappings.put(rootClass, null);

        this.mappingContext = new MappingContext(elementConverters, valueConverters);
    }

    public RootMapper getRootElementMapper(QName name) {
        return mappers.get(name);
    }

    public RootMapper getRootElementMapper(Class sourceClass) {
        QName rootName = rootMappings.get(sourceClass);
        if (rootName == null) {
            return null;
        }
        return mappers.get(rootName);
    }

    public boolean isPrettyPrint() {
        return isPrettyPrint;
    }

    public synchronized void setPrettyPrint(boolean prettyPrint) {
        if (initialized) {
            throw new XliteConfigurationException("Error: Trying to add configuration parameters " +
                    "after first use. Once Xlite is used (.fromXml() or similar is called)," +
                    " configuration parameters can not be altered.");
        }
        this.isPrettyPrint = prettyPrint;
    }

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

    public synchronized void addNamespace(String namespace) {
        if (initialized) {
            throw new XliteConfigurationException("Error: Trying to add configuration parameters after first use. " +
                    "Once Xlite is used (.fromXml() or similar is called), configuration parameters can not be altered.");
        }
        mappingContext.addNamespace(namespace);
    }

    public void addMapping(Class rootClass) {
        // check for duplicate mappings
        if (rootMappings.containsKey(rootClass)) {
            throw new XliteConfigurationException("Error: Duplicate mapping of root class. Class " +
                    rootClass.getName() + " is already mapped.");
        }
        rootMappings.put(rootClass, null);
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

        // Root class must have an element name defined
        if (elementName.length() == 0) {
            throw new XliteConfigurationException("Error: Mapped class " + currentClass.getName() +
                    "has empty @RootElement annotation: value or 'name' field must not be empty.");
        }

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
        Namespaces nsAnnotation = (Namespaces) currentClass.getAnnotation(Namespaces.class);
        if (nsAnnotation != null && nsAnnotation.value().length != 0) {
            for (int i = 0; i < nsAnnotation.value().length; i++) {
                classNS.addNamespace(nsAnnotation.value()[i]);
            }
        }

        // look for namespace with given prefix
        String rootElementNS = classNS.getNamespaceURI(rootElementPrefix);

        // if namespace not defined on class, look within predefined namespaces
        if (rootElementNS == null) {
            rootElementNS = mappingContext.getPredefinedNamespaces().getNamespaceURI(rootElementPrefix);
        }

        return new QName(rootElementNS, rootElementLocalpart, rootElementPrefix);
    }

}
