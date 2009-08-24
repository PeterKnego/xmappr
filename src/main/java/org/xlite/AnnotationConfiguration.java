/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite;

import org.xlite.converters.*;

import javax.xml.namespace.QName;
import javax.xml.XMLConstants;
import java.util.List;
import java.util.ArrayList;

public class AnnotationConfiguration implements Configuration {

    private RootMapper rootElementMapper;

    private MappingContext mappingContext;

    private volatile boolean initialized = false;

    private Class rootClass;

    private String rootElementName;

    private String rootElementNS = XMLConstants.NULL_NS_URI;

    private boolean isPrettyPrint = true;

    public AnnotationConfiguration(Class rootClass, String nodeName) {
        this(rootClass, nodeName, null);
    }

    public AnnotationConfiguration(Class rootClass, String nodeName, String namespaceURI) {
        try {
            Class.forName("javax.xml.stream.XMLOutputFactory", false, this.getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new XliteException("Error initializing XMLOutputFactory. If you are using Xlite on a JDK 1.5, " +
                    "then you must have XML stream library (javax.xml.stream) in your path. ", e);
        }

        // setup converters
        List<ElementConverter> elementConverters = null;
        List<ValueConverter> valueConverters = null;
        setupConverters(elementConverters, valueConverters);

        this.rootClass = rootClass;
        this.rootElementName = nodeName;
        this.rootElementNS = namespaceURI;
        this.mappingContext = new MappingContext(elementConverters, valueConverters);
    }

    public RootMapper getRootElementMapper() {
        return rootElementMapper;
    }

    public boolean isPrettyPrint() {
        return isPrettyPrint;
    }

    public synchronized void setPrettyPrint(boolean prettyPrint) {
        if (initialized) {
            throw new XliteConfigurationException("Error: Trying to add configuration parameters after first use. " +
                    "Once Xlite is used (.fromXml() or similar is called), configuration parameters can not be altered.");
        }
        this.isPrettyPrint = prettyPrint;
    }

    public synchronized void initialize() {

        // one-time initialization
        if (!initialized) {

            // split xml node name into prefix and local part
            int index = rootElementName.indexOf(':');
            String rootElementLocalpart;
            String rootElementPrefix;
            if (index > 0) {  // with prefix ("prefix:localpart")
                rootElementPrefix = rootElementName.substring(0, index);
                rootElementLocalpart = rootElementName.substring(index + 1, rootElementName.length());

            } else if (index == 0) { // empty prefix (no prefix defined - e.g ":nodeName")
                rootElementPrefix = XMLConstants.DEFAULT_NS_PREFIX;
                rootElementLocalpart = rootElementName.substring(1, rootElementName.length());

            } else { // no prefix given
                rootElementPrefix = XMLConstants.DEFAULT_NS_PREFIX;
                rootElementLocalpart = rootElementName;
            }

            // namespace  of root element is not defined
            if (rootElementNS == null) {
                rootElementNS = mappingContext.getPredefinedNamespaces().getNamespaceURI(rootElementPrefix);
            }
            this.rootElementMapper = new RootMapper(new QName(rootElementNS, rootElementLocalpart, rootElementPrefix), rootClass, mappingContext);
            initialized = true;
        }
    }

    private void setupConverters(List<ElementConverter> elementConverters, List<ValueConverter> valueConverters) {

        // setup ValueConverters
        valueConverters = new ArrayList<ValueConverter>();

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

        // setup ElementConverters
        elementConverters = new ArrayList<ElementConverter>();
        elementConverters.add(new CollectionConverter());
        elementConverters.add(new DOMelementConverter());

        // wraps every ValueConverter so that it can be used as a ElementConverter
        for (ValueConverter valueConverter : valueConverters) {
            elementConverters.add(new ValueConverterWrapper(valueConverter));
        }
    }

    private void setupValueConverters(List<ValueConverter> valueConverters) {


    }

    public synchronized void addNamespace(String namespace) {
        if (initialized) {
            throw new XliteConfigurationException("Error: Trying to add configuration parameters after first use. " +
                    "Once Xlite is used (.fromXml() or similar is called), configuration parameters can not be altered.");
        }
        mappingContext.addNamespace(namespace);
    }

}
