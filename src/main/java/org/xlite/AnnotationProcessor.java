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
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

/**
 * User: peter
 * Date: Feb 17, 2008
 * Time: 4:47:34 PM
 */
public class AnnotationProcessor {

    private final MappingContext mappingContext;

    public AnnotationProcessor(final MappingContext mappingContext) {
        this.mappingContext = mappingContext;
    }

    /**
     * Processes @XMLelement, @XMLattribute and @XMLtext annotations in a given class.
     * If subelements are found (@XMLelement), they are processed recursivelly.
     *
     * @param currentClass
     * @return
     */
    public ElementConverter processClass(final Class<?> currentClass) {

        AnnotatedClassConverter annotatedClassConverter = new AnnotatedClassConverter(currentClass);
        mappingContext.addToElementConverterCache(annotatedClassConverter);

        // find and process @XMLnamespaces annotation
        processClassNamespaces(currentClass, annotatedClassConverter);
        // find and process @XMLattribute annotations
        processAttributes(currentClass, annotatedClassConverter);
        // find and process @XMLvalue annotation
        processValue(currentClass, annotatedClassConverter);
        // find and process @XMLelement annotations
        processElements(currentClass, annotatedClassConverter);

//        mappingContext.elementConverters.add(annotatedClassConverter);

        return annotatedClassConverter;
    }

    /**
     * Processes @XMLnamespaces annotations defined on a Class.
     *
     * @param currentClass
     * @param annotatedClassConverter
     */
    private void processClassNamespaces(Class<?> currentClass, AnnotatedClassConverter annotatedClassConverter) {
        NsContext classNS = new NsContext();
        XMLnamespaces nsAnnotation = (XMLnamespaces) currentClass.getAnnotation(XMLnamespaces.class);
        if (nsAnnotation != null && nsAnnotation.value().length != 0) {
            for (int i = 0; i < nsAnnotation.value().length; i++) {
                classNS.addNamespace(nsAnnotation.value()[i]);
            }
        }
        annotatedClassConverter.setClassNamespaces(classNS);
    }

//    private ElementConverter lookupElementConverter(Class type) {
//        for (ElementConverter valueConverter : mappingContext.elementConverters) {
//            if (valueConverter.canConvert(type)) {
//                return valueConverter;
//            }
//        }
//        return null;
//    }

    /**
     * Searches given class for fields that have @XMLelement annotation.
     *
     * @param currentClass A class to be inspected for @XMLelement annotations.
     * @param converter    AnnotatedClassMapper that coresponds in
     */
    private void processElements(Class<?> currentClass, AnnotatedClassConverter converter) {
        boolean isElementCatcher = false;

        for (Field field : getAllFields(currentClass)) {

            // collect all @XMLelement annotations in a single array for easier processing
            XMLelement[] annotations = new XMLelement[0];
            XMLelements multiAnno = field.getAnnotation(XMLelements.class);
            if (multiAnno != null && multiAnno.value().length != 0) {
                annotations = multiAnno.value();
            }
            XMLelement singleAnno = field.getAnnotation(XMLelement.class);
            if (singleAnno != null) {
//                annotations = Arrays.copyOf(annotations, annotations.length + 1);
                XMLelement[] copy = new XMLelement[annotations.length + 1];
                System.arraycopy(annotations, 0, copy, 0, annotations.length);
                annotations = copy;
                annotations[annotations.length - 1] = singleAnno;
            }

            // are there any @XMLelement annotations on this field?
            if (annotations.length != 0) {

                // find the converter by the field type
                ElementConverter converterByType = mappingContext.lookupElementConverter(field.getType());

                // getValue converter for the class that the field references
                ElementConverter fieldConverter = null;

                // if target field is a collection, then a collection converter must be defined
                boolean isCollectionConverter = false;
                CollectionConverting collectionConverter = null;
                if (CollectionConverting.class.isAssignableFrom(converterByType.getClass())) {
                    collectionConverter = (CollectionConverting) converterByType;
                    isCollectionConverter = true;
                }

                // init a mapper
                ElementMapper fieldMapper = new ElementMapper(field, collectionConverter, mappingContext);

                // process @XMLelement annotations
                for (XMLelement annotation : annotations) {
                    Class<?> itemType = annotation.itemType();
                    Class<? extends ElementConverter> annotatedConverter = annotation.converter();

                    // setValue to default values according to annotations
                    if (itemType.equals(Object.class)) {
                        itemType = null;
                    }
                    if (annotatedConverter.equals(ElementConverter.class)) {
                        annotatedConverter = null;
                    }

                    // getValue QName that field maps to
                    String elementName = annotation.value().length() != 0 ? annotation.value() :
                            (annotation.name().length() != 0 ? annotation.name() : field.getName());
                    QName qname = getQName(elementName, getFieldNamespaces(field), converter.getClassNamespaces(),
                            currentClass.getName(), field.getName());

                    // element catcher for wildcard names
                    if (elementName.equals("*")) {
                        // double use of element catcher @XMLelement("*") within a single class
                        if (isElementCatcher) {
                            throw new XliteConfigurationException("Error: Incorrect use of XMLelement(\"*\")" + field.getName() +
                                    " in class " + field.getDeclaringClass().getSimpleName() +
                                    "Wildcard name mapping @XMLelement(\"*\") can be used only one time within a class");
                        }
                        isElementCatcher = true;


                        if (annotatedConverter != null || itemType != null) {
                            throw new XliteConfigurationException("Error: Can  not assign converter for collection " + field.getName() +
                                    " in class " + field.getDeclaringClass().getSimpleName() +
                                    "When @XMLelement annotation name is a wildcard name \"*\", 'converter' or " +
                                    "'itemType' values can not be used.");
                        }

                        // default converter for element catcher
                        fieldConverter = mappingContext.lookupElementConverter(DOMelement.class);

                        // default itemType for element catcher (in case that target field is a Colleciton)
                        itemType = DOMelement.class;

                    } else { // normal name-to-field mapping

                        // target field is a collection
                        if (isCollectionConverter) {

                            if (annotatedConverter != null) {
                                throw new XliteConfigurationException("Error: Can  not assign converter for collection " + field.getName() +
                                        " in class " + field.getDeclaringClass().getSimpleName() +
                                        "When @XMLelement annotation is used on a collection, 'converter' value can not be used. " +
                                        "Use 'itemType' instead.");
                            }

                            // if it's a collection, then @XMLelement must have "itemType" value defined
                            if (itemType == null) {
                                throw new XliteConfigurationException("Error: Can not assign converter for collection " + field.getName() +
                                        " in class " + field.getDeclaringClass().getSimpleName() +
                                        "When @XMLelement annotation is used on a collection, 'itemType' value must be defined.");
                            }
                            fieldConverter = null;

                        } else { // target field is a normal field (not a collection)

                            if (itemType != null) {
                                throw new XliteConfigurationException("Error: Wrong @XMLelement annotation value on field " + field.getName() +
                                        "in class " + field.getDeclaringClass().getName() + ". @XMLelement 'itemType' can only be used on " +
                                        "field types that implement Collection.");
                            }

                            // was custom converter assigned via annotation?
                            if (annotatedConverter != null) {
                                try {
                                    fieldConverter = annotatedConverter.newInstance();

                                    // check that assigned converter can actually convert to the target field type
                                    if (!fieldConverter.canConvert(field.getType())) {
                                        throw new XliteConfigurationException("Error: assigned converter type does not match field type.\n" +
                                                "Converter " + fieldConverter.getClass().getName() + " can not be used to convert " +
                                                "data of type " + field.getType() + ".\n" +
                                                "Please check XML annotations on field '" + field.getName() +
                                                "' in class " + field.getDeclaringClass().getName() + ".");
                                    }

                                } catch (Exception e) {
                                    throw new XliteException("Could not instantiate converter " + annotation.converter().getName() + ". ", e);
                                }

                            } else {
                                // converter was not declared via annotation, so we just use a converter derived from field type
                                fieldConverter = converterByType;
                            }
                        }
                    }

                    // getValue default value of this element
                    String defaultValue = annotation.defaultValue();
                    if (defaultValue.length() == 0) {
                        defaultValue = null;
                    }

                    if (isCollectionConverter) {
                        fieldMapper.addMapping(qname, itemType);
                    } else {
                        fieldMapper.setConverter(fieldConverter);
                        fieldMapper.setDefaultValue(defaultValue);
                        fieldMapper.setFormat(annotation.format());
                    }

                    if (isElementCatcher) {
                        fieldMapper.setConverter(fieldConverter);
                        converter.setElementCatcher(fieldMapper);
                        converter.addElementMapper(new QName("*"), fieldMapper);
                    } else { // normal element mapping
                        converter.addElementMapper(qname, fieldMapper);
                    }

//                System.out.println(currentClass.getSimpleName() + "." + field.getName() + " element:" +
//                          elementName + " converter:" + conv);

                }
            }
        }
    }

    /**
     * Searches class for fields that have @XMLattribute annotation and creates a ValueMapper for that field
     *
     * @param converter    AnnotatedClassMapper to which the ValueMapper is referenced
     * @param currentClass Class being inspected for @XMLattribute annotations
     */

    private void processAttributes(Class<?> currentClass, AnnotatedClassConverter converter) {

        for (Field field : getAllFields(currentClass)) {

            // collect all @XMLattribute annotations in a single array for easier processing
            XMLattribute[] annotations = new XMLattribute[0];
            XMLattributes multiAnno = field.getAnnotation(XMLattributes.class);
            if (multiAnno != null && multiAnno.value().length != 0) {
                annotations = multiAnno.value();
            }
            XMLattribute singleAnno = field.getAnnotation(XMLattribute.class);
            if (singleAnno != null) {
//                annotations = Arrays.copyOf(annotations, annotations.length + 1);
                XMLattribute[] copy = new XMLattribute[annotations.length + 1];
                System.arraycopy(annotations, 0, copy, 0, annotations.length);
                annotations = copy;
                annotations[annotations.length - 1] = singleAnno;
            }

            // are there any @XMLattribute annotations on this field
            if (annotations.length != 0) {
                // find the converter by the field type
                ValueConverter converterByType = mappingContext.lookupValueConverter(field.getType());

                // getValue converter for the class that the field references
                ValueConverter fieldConverter;

                // process @XMLelement annotations
                for (XMLattribute annotation : annotations) {
                    Class<?> itemType = annotation.itemType();
                    Class<? extends ValueConverter> annotatedConverter = annotation.converter();
                    boolean isAttributeCatcher = false;

                    // setValue to default values according to annotations
                    if (annotatedConverter.equals(ValueConverter.class)) {
                        annotatedConverter = null;
                    }

                    // getValue QName that field maps to
                    String elementName = annotation.value().length() != 0 ? annotation.value()
                            : (annotation.name().length() != 0 ? annotation.name() : field.getName());
                    QName qname = getQName(elementName, getFieldNamespaces(field), converter.getClassNamespaces(),
                            currentClass.getName(), field.getName());

                    // target field is a Map so a converter must be choosen by it's "itemType" property
                    if (Map.class.isAssignableFrom(field.getType())) {

                        // both 'itemType' and 'converter' are defined on @XMLattribute
                        if (annotatedConverter != null && itemType != String.class) {
                            throw new XliteException("Error: Can  not assign converter for Map " + field.getName() +
                                    " in class " + field.getDeclaringClass().getSimpleName() +
                                    "When @XMLattribute annotation is used on a Map, 'converter' and 'itemType' values" +
                                    " can not be used at the same time.");

                        } else if (annotatedConverter != null) { // 'converter' defined
                            try {
                                fieldConverter = annotatedConverter.newInstance();
                            } catch (InstantiationException e) {
                                throw new XliteException("Could not instantiate converter " + annotation.converter().getName() + ". ", e);
                            } catch (IllegalAccessException e) {
                                throw new XliteException("Could not instantiate converter " + annotation.converter().getName() + ". ", e);
                            }

                        } else { // 'itemType' either defined or default 'String.class'
                            fieldConverter = mappingContext.lookupValueConverter(itemType);
                        }

                        // attribute catcher
                        if (elementName.equals("*")) {
                            isAttributeCatcher = true;

                            // attribute catcher must not have 'itemType' defined
                            if (itemType != String.class) {
                                throw new XliteException("Error: Can  not assign converter for Map " + field.getName() +
                                        " in class " + field.getDeclaringClass().getSimpleName() +
                                        "When @XMLattribute annotation with a wildcard name (\"*\") is used on a Map, 'itemType' value" +
                                        " must not be defined.");

                            }
                            if (annotatedConverter == null) {
                                // default converter for attribute catcher
                                fieldConverter = mappingContext.lookupValueConverter(String.class);
                            }
                        }

                    } else { // target field is a normal field (not a collection)

                        if (!itemType.equals(String.class)) {
                            throw new XliteException("Error: Wrong @XMLattribute annotation value on field " + field.getName() +
                                    " in class " + field.getDeclaringClass().getName() + ". @XMLattribute 'itemType' can only be used on " +
                                    "field types that implement Map.");
                        }

                        // was custom converter assigned via annotation?
                        if (annotatedConverter != null) {
                            try {
                                fieldConverter = annotatedConverter.newInstance();
                            } catch (Exception e) {
                                throw new XliteException("Could not instantiate converter " + annotation.converter().getName() + ". ", e);
                            }
                            
                            // check that assigned converter can actually convert to the target field type
                            if (!fieldConverter.canConvert(field.getType())) {
                                throw new XliteConfigurationException("Error: assigned converter type does not match field type.\n" +
                                        "Converter " + fieldConverter.getClass().getName() + " can not be used to convert " +
                                        "data of type " + field.getType() + ".\n" +
                                        "Please check XML annotations on field '" + field.getName() +
                                        "' in class " + field.getDeclaringClass().getName() + ".");
                            }

                        } else {
                            // converter was not declared via annotation, so we just use a converter derived from field type
                            fieldConverter = converterByType;
                        }

                        // attribute catcher
                        if (elementName.equals("*")) {
                            throw new XliteConfigurationException("Error: Wrong @XMLattribute annotation value on field " + field.getName() +
                                    " in class " + field.getDeclaringClass().getName() + ". @XMLattribute wildcard name \"*\" can only be used on " +
                                    "field types that implement Map.");
                        }
                    }


                    if (isAttributeCatcher) {
                        // assign an attribute catcher
                        converter.setAttributeCatcher(new AttributeMapper(field, fieldConverter, null, annotation.format()));
                    } else {
                        // normal one-to-one attribute mapping

                        // SPECIAL CASE!!!
                        // XML attributes with empty prefix DO NOT belong to default namespace
                        if (qname.getPrefix().equals(XMLConstants.DEFAULT_NS_PREFIX)) {
                            String localPart = qname.getLocalPart();
                            qname = new QName(localPart);
                        }

                        // getValue default value of this element
                        String defaultValue = annotation.defaultValue();
                        if (defaultValue.length() == 0) {
                            defaultValue = null;
                        }

                        converter.addAttributeMapper(qname, new AttributeMapper(field, fieldConverter, defaultValue,
                                annotation.format()));
                    }

//                String conv = fieldMapper.valueConverter.getClass().equals(ValueConverterWrapper.class) ?
//                        ((ValueConverterWrapper) fieldMapper.valueConverter).valueConverter.getClass().getSimpleName() :
//                        fieldMapper.valueConverter.getClass().getSimpleName();
//
//                System.out.println(currentClass.getSimpleName() + "." + field.getName() + " element:" + elementName + " converter:" + conv);

                }
            }
        }
    }


    //todo write javadoc
    /**
     * @param elementName
     * @param fieldNS
     * @param classNS
     * @return
     */
    private QName getQName(String elementName, NsContext fieldNS, NsContext classNS, String className, String fieldName) {

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


        String fieldNsURI = fieldNS.getNamespaceURI(prefix);
        String classNsURI = classNS.getNamespaceURI(prefix);
        String predefinedNsURI = mappingContext.getPredefinedNamespaces().getNamespaceURI(prefix);

        // used prefix must be defined in at least one namespace
        if (prefix.length() != 0 && (fieldNsURI == null && classNsURI == null && predefinedNsURI == null)) {
            throw new XliteConfigurationException("ERROR: used name prefix is not defined in any namespace.\n" +
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

    private NsContext getFieldNamespaces(Field field) {

        NsContext fieldNS = new NsContext();
        XMLnamespaces nsAnnotation = field.getAnnotation(XMLnamespaces.class);
        if (nsAnnotation != null && nsAnnotation.value().length != 0) {
            for (int i = 0; i < nsAnnotation.value().length; i++) {
                fieldNS.addNamespace(nsAnnotation.value()[i]);
            }
        }
        return fieldNS;
    }

    /**
     * Searches class for a field that has @XMLtext annotation.
     *
     * @param currentClass
     * @param converter
     */
    private void processValue(Class currentClass, AnnotatedClassConverter converter) {
        Field targetField = null;
        int found = 0;
        XMLtext annotation, targetAnnotation = null;
        for (Field field : getAllFields(currentClass)) {
            annotation = field.getAnnotation(XMLtext.class);
            if (annotation != null) {
                found++;
                targetField = field;
                targetAnnotation = annotation;
            }
        }
        if (found > 1) {
            throw new XliteConfigurationException("Error: Multiple @XMLtext annotations in class "
                    + currentClass.getName() + ". Max one @XMLtext annotation can be present in a class.");
        }

        if (found == 1) {

            // find the appropriate converter
            ValueConverter valueConverter;
            CollectionConverting collectionConverter = null;
            if (targetAnnotation.converter().equals(ValueConverter.class)) {  // default converter
                // is target tye a collection?
                Class targetType;
                if (Collection.class.isAssignableFrom(targetField.getType())) {

                    collectionConverter = (CollectionConverting) mappingContext.lookupElementConverter(targetField.getType());
                    // choose converter according to 'itemType' value in @XMLtext annotation
                    targetType = targetAnnotation.itemType();
                } else {
                    // choose converter according to field type
                    targetType = targetField.getType();
                }
                valueConverter = mappingContext.lookupValueConverter(targetType);


                // check that assigned converter can actually convert to the target field type
                if (!valueConverter.canConvert(targetType)) {
                    throw new XliteConfigurationException("Error: assigned converter type does not match field type.\n" +
                            "Converter " + valueConverter.getClass().getName() + " can not be used to convert " +
                            "data of type " + targetField.getType() + ".\n" +
                            "Please check XML annotations on field '" + targetField.getName() +
                            "' in class " + targetField.getDeclaringClass().getName() + ".");
                }
            } else {  // custom converter assigned via annotation
                try {
                    valueConverter = targetAnnotation.converter().newInstance();
                } catch (Exception e) {
                    throw new XliteException("Could not instantiate converter " + targetAnnotation.converter().getName() + ". ", e);
                }
            }

            // check if this field also has @XMLelement(s) annotation (intermixed)
            boolean isIntermixed = targetField.getAnnotation(XMLelements.class) != null
                    || targetField.getAnnotation(XMLelement.class) != null;

            converter.setTextMapper(new TextMapper(targetField, valueConverter, targetAnnotation.itemType(),
                    collectionConverter, isIntermixed, targetAnnotation.format()));

//            System.out.println(currentClass.getSimpleName() + "." + targetField.getName() + " value "
//                    + " converter:" + valueConverter.getClass().getSimpleName());
        }
    }

    /**
     * Checks if there are incompatible annotations set on given Field. Throws XliteException if they are.
     *
     * @param field
     */
//    private void checkClashingAnnotations(Field field) {
//        // todo finish this
//
//    }

    // todo Check if this method returns duplicate field if a field is overriden.

    /**
     * Collects all fields (public and private) in a given class and its superclasses.
     *
     * @param clazz Child class
     * @return List of fields
     */
    private List<Field> getAllFields(Class clazz) {
        List<Field> fields = new ArrayList<Field>();
        Class cl = clazz;
        do {
            for (Field field : cl.getDeclaredFields()) {
                if (!field.isSynthetic()) {
                    fields.add(field);
                }
            }
            cl = cl.getSuperclass();
        } while (cl != Object.class);
        return fields;
    }


}
