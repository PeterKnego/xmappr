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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * User: peter
 * Date: Feb 17, 2008
 * Time: 4:47:34 PM
 */
public class MappingTreeBuilder {

    private final MappingContext mappingContext;

    public MappingTreeBuilder(final MappingContext mappingContext) {
        this.mappingContext = mappingContext;
    }

    /**
     * Processes given mapping configuration and builds in-memory tree of mappers and converters.
     *
     * @param currentClass
     * @return
     */
    public ElementConverter processClass(final Class<?> currentClass) {

        ClassConverter classConverter = new ClassConverter(currentClass);
//        mappingContext.addToElementConverterCache(classConverter);

        // find and process @Namespaces annotation
        processClassNamespaces(currentClass, classConverter);
        // find and process @Attribute annotations
        processAttributes(currentClass, classConverter);
        // find and process @Text annotation
        processText(currentClass, classConverter);
        // find and process @Element annotations
        processElements(currentClass, classConverter);

        return classConverter;
    }

    /**
     * Processes @XMLnamespaces annotations defined on a Class.
     *
     * @param currentClass
     * @param classConverter
     */
    private void processClassNamespaces(Class<?> currentClass, ClassConverter classConverter) {
        NsContext classNS = new NsContext();
        Namespaces nsAnnotation = (Namespaces) currentClass.getAnnotation(Namespaces.class);
        if (nsAnnotation != null && nsAnnotation.value().length != 0) {
            for (int i = 0; i < nsAnnotation.value().length; i++) {
                classNS.addNamespace(nsAnnotation.value()[i]);
            }
        }
        classConverter.setClassNamespaces(classNS);
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
    private void processElements(Class<?> currentClass, ClassConverter converter) {
        boolean isElementCatcher = false;

        for (Field field : getAllFields(currentClass)) {

            // collect all @Element annotations in a single array for easier processing
            Element[] annotations = new Element[0];
            Elements multiAnno = field.getAnnotation(Elements.class);
            if (multiAnno != null && multiAnno.value().length != 0) {
                annotations = multiAnno.value();
            }
            Element singleAnno = field.getAnnotation(Element.class);
            if (singleAnno != null) {
//                annotations = Arrays.copyOf(annotations, annotations.length + 1);
                Element[] copy = new Element[annotations.length + 1];
                System.arraycopy(annotations, 0, copy, 0, annotations.length);
                annotations = copy;
                annotations[annotations.length - 1] = singleAnno;
            }

            // are there any @Element annotations on this field?
            if (annotations.length != 0) {

                // find the converter by the field type
                ElementConverter converterByType = mappingContext.lookupElementConverter(field.getType());

                // getValue converter for the class that the field references
                ElementConverter fieldConverter = null;

                // type to convert to
                Class targetType;

                // if target field is a collection, then a collection converter must be defined
                boolean isCollectionConverter = false;
                CollectionConverting collectionConverter = null;
                if (CollectionConverting.class.isAssignableFrom(converterByType.getClass())) {
                    collectionConverter = (CollectionConverting) converterByType;
                    isCollectionConverter = true;
                }

                // init a mapper
                ElementMapper fieldMapper = new ElementMapper(field, collectionConverter, mappingContext);

                // process @Element annotations
                for (Element annotation : annotations) {
                    Class<?> itemType = annotation.itemType();
                    Class<? extends Converter> annotatedConverter = annotation.converter();

                    // setValue to default values according to annotations
                    if (itemType.equals(Object.class)) {
                        itemType = null;
                    }
                    if (annotatedConverter == null || annotatedConverter.equals(ElementConverter.class)) {
                        annotatedConverter = null;
                    }

                    // getValue QName that field maps to
                    String elementName = annotation.value().length() != 0 ? annotation.value() :
                            (annotation.name().length() != 0 ? annotation.name() : field.getName());
                    QName qname = getQName(elementName, getFieldNamespaces(field), converter.getClassNamespaces(),
                            currentClass.getName(), field.getName());

                    // element catcher for wildcard names
                    if (elementName.equals("*")) {
                        // double use of element catcher @Element("*") within a single class
                        if (isElementCatcher) {
                            throw new XliteConfigurationException("Error: Incorrect use of Element(\"*\")" +
                                    field.getName() + " in class " + field.getDeclaringClass().getSimpleName() +
                                    "Wildcard name mapping @Element(\"*\") can be used only one time within a class");
                        }
                        isElementCatcher = true;


                        if (annotatedConverter != null || itemType != null) {
                            throw new XliteConfigurationException("Error: Can  not assign converter for " +
                                    "collection " + field.getName() +
                                    " in class " + field.getDeclaringClass().getSimpleName() +
                                    "When @Element annotation name is a wildcard name \"*\", 'converter' or " +
                                    "'itemType' values can not be used.");
                        }

                        // default converter for element catcher
                        fieldConverter = mappingContext.lookupElementConverter(DOMelement.class);

                        // default itemType for element catcher (in case that target field is a Colleciton)
                        itemType = DOMelement.class;

                    } else { // normal name-to-field mapping

                        // target field is a collection
                        if (isCollectionConverter) {

                            // if it's a collection, then @Element must have either "itemType" or 'converter' value defined
                            if (annotatedConverter == null && itemType == null) {
                                throw new XliteConfigurationException("Error: Can  not assign converter " +
                                        "for collection " + field.getName() +
                                        " in class " + field.getDeclaringClass().getSimpleName() +
                                        "When @Element annotation is used on a collection, " +
                                        "either 'converter' value or 'itemType' value must be declared.");
                            }

                            // was custom converter assigned via annotation?
                            if (annotatedConverter != null) {
                                fieldConverter = initializeConverter(annotatedConverter);

                                // check if item type is set and
                                // that assigned converter can actually convert to this type
                                if (itemType != null && !fieldConverter.canConvert(itemType)) {
                                    throw new XliteConfigurationException("Error: assigned converter type does not " +
                                            "match field type.\n" +
                                            "Converter " + fieldConverter.getClass().getName() +
                                            " can not be used to convert data of type " + field.getType() + ".\n" +
                                            "Please check XML annotations on field '" + field.getName() +
                                            "' in class " + field.getDeclaringClass().getName() + ".");
                                }


                            } else {
                                // converter was not declared via annotation, so we just use a converter derived from field type
                                fieldConverter = null;
                            }

                        } else { // target field is a normal field (not a collection)

//                            if (itemType != null) {
//                                throw new XliteConfigurationException("Error: Wrong @Element annotation value on " +
//                                        "field " + field.getName() +
//                                        "in class " + field.getDeclaringClass().getName() + ". " +
//                                        "@Element 'itemType' can only be used on field types that implement Collection.");
//                            }

                            // was custom converter assigned via annotation?
                            if (annotatedConverter != null) {
                                fieldConverter = initializeConverter(annotatedConverter);

                                // check that assigned converter can actually convert to the target field type
                                if (!fieldConverter.canConvert(field.getType())) {
                                    throw new XliteConfigurationException("Error: assigned converter type does not " +
                                            "match field type.\nConverter " + fieldConverter.getClass().getName() +
                                            " can not be used to convert " +
                                            "data of type " + field.getType() + ".\n" +
                                            "Please check XML annotations on field '" + field.getName() +
                                            "' in class " + field.getDeclaringClass().getName() + ".");
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
                        fieldMapper.addMapping(qname, fieldConverter, itemType);
                    } else {
                        fieldMapper.setConverter(fieldConverter);
                        fieldMapper.setDefaultValue(defaultValue);
                        fieldMapper.setFormat(annotation.format());
                        fieldMapper.setTargetType(field.getType());
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

    private ElementConverter initializeConverter(Class<? extends Converter> annotatedConverter) {
        ElementConverter fieldConverter;
        try {
            //check type of custom converter
            if (ElementConverter.class.isAssignableFrom(annotatedConverter)) {
                fieldConverter = ((Class<? extends ElementConverter>) annotatedConverter).newInstance();
            } else if (ValueConverter.class.isAssignableFrom(annotatedConverter)) {
                ValueConverter vc = ((Class<? extends ValueConverter>) annotatedConverter).newInstance();
                fieldConverter = new ValueConverterWrapper(vc);
            } else {
                throw new XliteConfigurationException("Error: Can not instantiate Converter " + annotatedConverter +
                        ", beacuse it's of wrong type: Converters defined on @Element annotation must" +
                        "implement either ElementCoverter or ValueConverter");
            }

        } catch (Exception e) {
            throw new XliteException("Could not instantiate converter " + annotatedConverter.getName() + ". ", e);
        }
        return fieldConverter;
    }

    /**
     * Searches class for fields that have @XMLattribute annotation and creates a ValueMapper for that field
     *
     * @param converter    AnnotatedClassMapper to which the ValueMapper is referenced
     * @param currentClass Class being inspected for @XMLattribute annotations
     */

    private void processAttributes(Class<?> currentClass, ClassConverter converter) {

        for (Field field : getAllFields(currentClass)) {

            // collect all @Attribute annotations in a single array for easier processing
            Attribute[] annotations = new Attribute[0];
            Attributes multiAnno = field.getAnnotation(Attributes.class);
            if (multiAnno != null && multiAnno.value().length != 0) {
                annotations = multiAnno.value();
            }
            Attribute singleAnno = field.getAnnotation(Attribute.class);
            if (singleAnno != null) {
//                annotations = Arrays.copyOf(annotations, annotations.length + 1);
                Attribute[] copy = new Attribute[annotations.length + 1];
                System.arraycopy(annotations, 0, copy, 0, annotations.length);
                annotations = copy;
                annotations[annotations.length - 1] = singleAnno;
            }

            // are there any @Attribute annotations on this field
            if (annotations.length != 0) {
                // find the converter by the field type
                ValueConverter converterByType = mappingContext.lookupValueConverter(field.getType());

                // getValue converter for the class that the field references
                ValueConverter fieldConverter;

                // process @Element annotations
                for (Attribute annotation : annotations) {
                    Class<?> itemType = annotation.itemType();
                    // the type of the target object
                    Class targetType = itemType;

                    Class<? extends ValueConverter> annotatedConverter = annotation.converter();
                    boolean isAttributeCatcher = false;

                    // set to default values according to annotations
                    if (annotatedConverter.equals(ValueConverter.class)) {
                        annotatedConverter = null;
                    }

                    // getValue QName that field maps to
                    String elementName = annotation.value().length() != 0 ? annotation.value()
                            : (annotation.name().length() != 0 ? annotation.name() : field.getName());
                    QName qname = getQName(elementName, getFieldNamespaces(field), converter.getClassNamespaces(),
                            currentClass.getName(), field.getName());

                    // Is target field a Map?
                    if (Map.class.isAssignableFrom(field.getType())) {

                        if (annotatedConverter != null) { // 'converter' defined
                            try {
                                fieldConverter = annotatedConverter.newInstance();
                            } catch (InstantiationException e) {
                                throw new XliteException("Could not instantiate converter " +
                                        annotation.converter().getName() + ". ", e);
                            } catch (IllegalAccessException e) {
                                throw new XliteException("Could not instantiate converter " +
                                        annotation.converter().getName() + ". ", e);
                            }
                        } else {
                            // if targetType is not defined we assign it a default type: String.class
                            if (targetType.equals(Object.class)) {
                                targetType = String.class;
                            }

                            fieldConverter = mappingContext.lookupValueConverter(targetType);
                        }

                        // attribute catcher
                        if (elementName.equals("*")) {
                            isAttributeCatcher = true;
                        }

                    } else { // target field is a normal field (not a collection)

                        // if targetType is not defined we assign it based on field type
                        if (targetType.equals(Object.class)) {
                            targetType = field.getType();
                        }

                        // was custom converter assigned via annotation?
                        if (annotatedConverter != null) {
                            try {
                                fieldConverter = annotatedConverter.newInstance();
                            } catch (Exception e) {
                                throw new XliteException("Could not instantiate converter " +
                                        annotation.converter().getName() + ". ", e);
                            }

                            // check that assigned converter can actually convert to the target field type
                            if (!fieldConverter.canConvert(field.getType())) {
                                throw new XliteConfigurationException("Error: assigned converter type " +
                                        "does not match field type.\n" +
                                        "Converter " + fieldConverter.getClass().getName() + " can not be used " +
                                        "to convert data of type " + field.getType() + ".\n" +
                                        "Please check XML annotations on field '" + field.getName() +
                                        "' in class " + field.getDeclaringClass().getName() + ".");
                            }

                        } else {
                            // converter was not declared via annotation, so we just use a converter derived from field type
                            fieldConverter = converterByType;
                        }

                        // attribute catcher
                        if (elementName.equals("*")) {
                            throw new XliteConfigurationException("Error: Wrong @Attribute annotation value " +
                                    "on field " + field.getName() + " in class " + field.getDeclaringClass().getName() +
                                    ". @Attribute wildcard name \"*\" can only be used on " +
                                    "field types that implement Map.");
                        }
                    }

                    // ValueConverter is needed - converting can't be done without it.
                    if (fieldConverter == null) {
                        throw new XliteConfigurationException("Error: No converter can be found for class " +
                                field.getType().getName()+
                                " on field " + field.getName() + " in class " + field.getDeclaringClass().getName() +
                                ". Either there must be a custom converter assigned via @Attribute annotation or " +
                                "target class must contain further XML mapping annotations.");
                    }


                    if (isAttributeCatcher) {
                        // assign an attribute catcher
                        converter.setAttributeCatcher(
                                new AttributeMapper(field, targetType, fieldConverter, null, annotation.format())
                        );
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

                        converter.addAttributeMapper(
                                qname,
                                new AttributeMapper(field, targetType, fieldConverter, defaultValue, annotation.format())
                        );
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

    private NsContext getFieldNamespaces(Field field) {

        NsContext fieldNS = new NsContext();
        Namespaces nsAnnotation = field.getAnnotation(Namespaces.class);
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
    private void processText(Class currentClass, ClassConverter converter) {
        Field targetField = null;
        int found = 0;
        Text annotation, targetAnnotation = null;
        for (Field field : getAllFields(currentClass)) {
            annotation = field.getAnnotation(Text.class);
            if (annotation != null) {
                found++;
                targetField = field;
                targetAnnotation = annotation;
            }
        }
        if (found > 1) {
            throw new XliteConfigurationException("Error: Multiple @Text annotations in class "
                    + currentClass.getName() + ". Max one @Text annotation can be present in a class.");
        }

        if (found == 1) {

            // find the appropriate converter
            ValueConverter valueConverter;
            CollectionConverting collectionConverter = null;
            Class targetType = null;
            if (targetAnnotation.converter().equals(ValueConverter.class)) {  // default converter
                // is target type a collection?
                if (Collection.class.isAssignableFrom(targetField.getType())) {

                    collectionConverter = (CollectionConverting) mappingContext.lookupElementConverter(targetField.getType());
                    targetType = String.class;
//                    throw new XliteConfigurationException("Error: @Text annotation on a collection field of "
//                            + currentClass.getName() + ". No converter parameter provided.");
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
                    throw new XliteException("Could not instantiate converter " +
                            targetAnnotation.converter().getName() +
                            ". ", e);
                }
            }

            // check if this field also has @Element(s) annotation (intermixed)
            boolean isIntermixed = targetField.getAnnotation(Elements.class) != null
                    || targetField.getAnnotation(Element.class) != null;

            converter.setTextMapper(new TextMapper(targetField, targetType, valueConverter,
                    collectionConverter, isIntermixed, targetAnnotation.format()));

//            System.out.println(currentClass.getSimpleName() + "." + targetField.getName() + " value "
//                    + " converter:" + valueConverter.getClass().getSimpleName());
        }
    }

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
