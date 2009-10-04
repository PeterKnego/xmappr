/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite;

import org.xlite.converters.ElementConverter;
import org.xlite.converters.ValueConverter;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.util.List;
import java.util.Stack;
import java.util.ArrayList;

//todo write javadoc - IMPORTANT, as this is one of the core classes

/**
 * @author peter
 */
public class MappingContext {

    private List<ElementConverter> elementConverters;
    private List<ElementConverter> elementConverterCache = new ArrayList<ElementConverter>();
    private List<ValueConverter> valueConverters;
    private MappingBuilder mappingBuilder;
    private NsContext predefinedNamespaces = new NsContext();

    private Stack<Class> classTreeWalker = new Stack<Class>();

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

    public void addConverter(ValueConverter converter) {
       valueConverters.add(0,converter);
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

    public ValueConverter lookupValueConverter(Class type) {
        for (ValueConverter valueConverter : valueConverters) {
            if (valueConverter.canConvert(type)) {
                return valueConverter;
            }
        }
        return null;
    }

    /**
     * Finds the appropriate ElementConverter for the given Class among the registered ElementConverters. If none
     * is found, an instance of ClassConverter is returned. ClassConverter will be returned only if XML mappings exist
     * for given class.
     * <p/>
     * This method can change the internal state of MappingContext, so it needs to be synchronized.
     *
     * @param type
     * @return
     */
    public synchronized ElementConverter lookupElementConverter(Class type) {
        return lookupElementConverter(type, true);
    }

    /**
     * Finds the appropriate ElementConverter for the given Class among the registered ElementConverters. If none
     * is found, an instance of ClassConverter is returned. ClassConverter will be returned only if XML mappings exist
     * for given class.
     * <p/>
     * This method can change the internal state of MappingContext, so it needs to be synchronized.
     *
     * @param type
     * @param lookupClassConverter Whether to try to use ClassConverter for given class.
     * @return
     */
    public synchronized ElementConverter lookupElementConverter(Class type, boolean lookupClassConverter) {

        // lookup in preconfigured converters
        for (ElementConverter elementConverter : elementConverters) {
            if (elementConverter.canConvert(type)) {
                return elementConverter;
            }
        }

        // checking if this Class was already processed in this tree trunk = this means loop exists in annotated classes
//        if (classTreeWalker.contains(type)) {
////            throw new XliteConfigurationException("ERROR: Loop detected in annotated classes. Class being processed for the second time: " + type.getName());
//        }
        classTreeWalker.push(type);

        // check cache for this EC
        ElementConverter ec = null;
        for (ElementConverter elementConverter : elementConverterCache) {
            if (elementConverter.canConvert(type)) {
                ec = elementConverter;
                break;
            }
        }
        // not found in cache?
        if (ec == null && lookupClassConverter) {
            // process it
            ec = mappingBuilder.processClass(type);
        }
        classTreeWalker.pop();
        return ec;
    }

    public void addToElementConverterCache(ElementConverter elementConverter) {
        elementConverterCache.add(elementConverter);
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
}
