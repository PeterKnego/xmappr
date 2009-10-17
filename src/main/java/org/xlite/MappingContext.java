/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite;

import org.xlite.converters.ElementConverter;
import org.xlite.converters.RootMapper;
import org.xlite.converters.ValueConverter;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//todo write javadoc - IMPORTANT, as this is one of the core classes

/**
 * @author peter
 */
public class MappingContext {

    private List<ElementConverter> elementConverters;
    //    private List<ElementConverter> elementConverterCache = new ArrayList<ElementConverter>();
    private List<ValueConverter> valueConverters;
    private MappingBuilder mappingBuilder;
    private NsContext predefinedNamespaces = new NsContext();

    private Map<Class, RootMapper> classMappings = new HashMap<Class, RootMapper>();
    private Map<QName, RootMapper> nameMappings = new HashMap<QName, RootMapper>();

    private Map<Class, ConfigElement> configElements = new HashMap<Class, ConfigElement>();
    private Map<Class, ConfigRootElement> configRootElements = new HashMap<Class, ConfigRootElement>();

    public MappingContext(List<ElementConverter> elementConverters, List<ValueConverter> valueConverters) {
        this.elementConverters = elementConverters;
        this.valueConverters = valueConverters;
        mappingBuilder = new MappingBuilder(this);
    }

    public NsContext getPredefinedNamespaces() {
        return predefinedNamespaces;
    }

    public void addNamespace(String prefix, String namespace) {
        predefinedNamespaces.addNamespace(prefix, namespace);
    }

    public void addNamespace(String compoundNamespace) {

        int index = compoundNamespace.indexOf('=');
        String prefix, nsURI;
        if (index > 0) {  // with prefix ("prefix:localpart")
            prefix = compoundNamespace.substring(0, index);
            nsURI = compoundNamespace.substring(index + 1, compoundNamespace.length());

        } else if (index == 0) { // empty prefix (no prefix defined - e.g ":elementName")
            prefix = XMLConstants.DEFAULT_NS_PREFIX;
            nsURI = compoundNamespace.substring(1, compoundNamespace.length());

        } else { // no prefix given
            prefix = XMLConstants.DEFAULT_NS_PREFIX;
            nsURI = compoundNamespace;
        }
        predefinedNamespaces.addNamespace(prefix, nsURI);
    }

    public void addConverter(ValueConverter converter) {
        valueConverters.add(0, converter);
    }

    public void addConverter(ElementConverter converter) {
        elementConverters.add(0, converter);
    }

    /**
     * Delegates processing of the next XML element in the reader to the appropriate converter. It finds the right
     * ElementConverter for the given Class and uses it to process current XML element.<br><br>
     * It's mainly used by custom converters to forward processing of a subelement to appropriate converter.
     *
     * @param targetType
     * @param reader
     * @return
     */
    public Object processNextElement(Class targetType, Object targetObject, XMLSimpleReader reader, String defaultValue, String format) {
        // find the converter for given Class
        ElementConverter converter = lookupElementConverter(targetType);
        return converter.fromElement(reader, this, defaultValue, format, targetType, targetObject);
    }

    public void addRootMapper(ConfigRootElement rootConfigElement) {
        RootMapper rootMapper = mappingBuilder.processConfiguration(rootConfigElement);
        nameMappings.put(rootMapper.getRootNodeName(), rootMapper);
        classMappings.put(rootMapper.getRootClass(), rootMapper);
    }

    public RootMapper getRootMapper(Class targetClass) {
        RootMapper mapper = classMappings.get(targetClass);
        return mapper;
    }

    public RootMapper getRootMapper(QName name) {
        RootMapper mapper = nameMappings.get(name);
        return mapper;
    }

    public ValueConverter lookupValueConverter(Class type) {
        for (ValueConverter valueConverter : valueConverters) {
            if (valueConverter.canConvert(type)) {
                return valueConverter;
            }
        }
        return null;
    }

    public boolean isElementConverterDefined(Class type) {
        for (ElementConverter elementConverter : elementConverters) {
            if (elementConverter.canConvert(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calls {@link org.xlite.MappingContext#lookupElementConverter(Class, boolean)} with second parameter <code>true</code>.
     *
     * @param type Class for which ElementConverter is to be found.
     * @return
     */
    public synchronized ElementConverter lookupElementConverter(Class type) {
        return lookupElementConverter(type, true);
    }

    /**
     * Finds the appropriate ElementConverter for the given Class among the registered ElementConverters. If none
     * is found, the it will try to create an instance of ClassConverter. ClassConverter will be returned only if XML mappings exist
     * for given class.
     * <p/>
     * This method can change the internal state of MappingContext, so it needs to be synchronized.
     *
     * @param type                     Class for which ElementConverter is to be found.
     * @param throwExceptionIfNotFound If true, throws an XliteConfigurationException if no ElementConverter is found.
     * @return
     */
    public synchronized ElementConverter lookupElementConverter(Class type, boolean throwExceptionIfNotFound) {

        // look in preconfigured converters
        for (ElementConverter elementConverter : elementConverters) {
            if (elementConverter.canConvert(type)) {
                return elementConverter;
            }
        }

        ElementConverter converter = null;

        // look in configuration elements
        if (configElements.containsKey(type)) {
            converter = mappingBuilder.createClassConverter(type, configElements.get(type));
        } else {
            // Try to use Class directly:
            // 1. create ConfigElement from Class
            // 2. create ClassConverter
            ConfigElement newConfElement = ConfigurationProcessor.processClassAnnotations(type, this);
            converter = mappingBuilder.createClassConverter(type, newConfElement);
        }

        if (converter == null && throwExceptionIfNotFound) {
            throw new XliteConfigurationException("Error: could not find converter for class " + type.getName() +
                    "\nConverters are found in one of four ways:\n" +
                    "1. Class can be converted by one if built in converters.\n" +
                    "2. Custom converter is assigned for this Class.\n" +
                    "3. Class contains XML mapping annotations and is handled directly by Xlite.\n" +
                    "4. Class is mapped via external XML configuration and is handled directly by Xlite.\n");
        }

        return converter;
    }

    /**
     * Creates a QName from compound XML element name. Compound element name is of form "prefix:name".
     * Method searches all defined namespaces and calculates QName.
     *
     * @param elementName Compound name of XML element in "prefix:name" format.
     * @param fieldNS
     * @param classNS
     * @param className
     * @param fieldName
     * @return
     */
    public QName getQName(String elementName, NsContext fieldNS, NsContext classNS, String className, String fieldName) {

        // split xml element name into prefix and local part
        int index = elementName.indexOf(':');
        String prefix, localPart;
        if (index > 0) {  // with prefix ("prefix:localpart")
            prefix = elementName.substring(0, index);
            localPart = elementName.substring(index + 1, elementName.length());

        } else if (index == 0) { // empty prefix (no prefix defined - e.g ":elementName")
            prefix = XMLConstants.DEFAULT_NS_PREFIX;
            localPart = elementName.substring(1, elementName.length());

        } else { // no prefix given
            prefix = XMLConstants.DEFAULT_NS_PREFIX;
            localPart = elementName;
        }

        String fieldNsURI = fieldNS == null ? null : fieldNS.getNamespaceURI(prefix);
        String classNsURI = classNS == null ? null : classNS.getNamespaceURI(prefix);
        String predefinedNsURI = getPredefinedNamespaces().getNamespaceURI(prefix);

        // used prefix must be defined in at least one namespace
        if (prefix.length() != 0 && (fieldNsURI == null && classNsURI == null && predefinedNsURI == null)) {
            throw new XliteConfigurationException("ERROR: used namespace prefix is not defined in any namespace.\n" +
                    "Name prefix '" + prefix + "' used on field '" + fieldName +
                    "' in class " + className + " is not defined in any declared XML namespace.\n");
        }

        // choose the namespaceURI that is not null from field, class, predefined or
        // finally DEFAULT_NS_PREFIX (in that order)
        String theURI = fieldNsURI != null ? fieldNsURI :
                (classNsURI != null ? classNsURI :
                        (predefinedNsURI != null ? predefinedNsURI : XMLConstants.DEFAULT_NS_PREFIX));
//        System.out.println("namespace URI=" + theURI + " local=" + localPart + " prefix=" + prefix);
        return new QName(theURI, localPart, prefix);
    }

    public ConfigRootElement lookupConfigRootElement(Class rootClass) {
        return configRootElements.get(rootClass);
    }

    public boolean configElementExists(Class elementClass) {
        return configElements.containsKey(elementClass);
    }

    public void addConfigElement(Class targetClass, ConfigElement element) {
        configElements.put(targetClass, element);
    }

    public void addConfigElement(Class rootClass, ConfigRootElement element) {
        if (!configRootElements.containsKey(rootClass)) {
            configRootElements.put(rootClass, element);
        } else {
            throw new XliteConfigurationException("Error: Mapping for class " +
                    rootClass.getName() + " is defined more than one time!");
        }
    }

    public void printConfig() {
        for (Map.Entry<Class, ConfigRootElement> entry : configRootElements.entrySet()) {
            System.out.println("Class: " + entry.getKey());
            System.out.println(entry.getValue());
        }
    }
}
