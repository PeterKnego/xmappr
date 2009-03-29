package org.xlite;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.lang.reflect.Field;
import java.util.Arrays;

import org.xlite.converters.*;

/**
 * User: peter
 * Date: Feb 17, 2008
 * Time: 4:47:34 PM
 */
public class AnnotationProcessor {

    private MappingContext mappingContext;

    public AnnotationProcessor(MappingContext mappingContext) {
        this.mappingContext = mappingContext;
    }

    /**
     * Processes @XMLelement, @XMLattribute and @XMLtext annotations in a given class.
     * If subelements are found (@XMLelement), they are processed recursivelly.
     *
     * @param currentClass
     * @return
     */
    public ElementConverter processClass(Class<?> currentClass) {

        AnnotatedClassConverter annotatedClassConverter = new AnnotatedClassConverter(currentClass);

        // find and process @XMLnamespaces annotation
        processClassNamespaces(currentClass, annotatedClassConverter);
        // find and process @XMLattribute annotations
        processAttributes(currentClass, annotatedClassConverter);
        // find and process @XMLvalue annotation
        processValue(currentClass, annotatedClassConverter);
        // find and process @XMLelement annotations
        processElements(currentClass, annotatedClassConverter);

        mappingContext.elementConverters.add(annotatedClassConverter);

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
//        for (ElementConverter elementConverter : mappingContext.elementConverters) {
//            if (elementConverter.canConvert(type)) {
//                return elementConverter;
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
    private void processElements(Class<? extends Object> currentClass, AnnotatedClassConverter converter) {

        for (Field field : currentClass.getDeclaredFields()) {

            // find the converter by the field type
            ElementConverter converterByType = mappingContext.lookupElementConverter(field.getType());

            // get converter for the class that the field references
            ElementConverter fieldConverter = null;

            // if target field is a collection, then a collection converter must be defined
            CollectionConverting collectionConverter = null;
            if (CollectionConverting.class.isAssignableFrom(converterByType.getClass())) {
                collectionConverter = (CollectionConverting) converterByType;
            }

            // init a mapper
            ElementMapper fieldMapper = new ElementMapper(field, collectionConverter, mappingContext);

            // collect all @XMLelement annotations in a single array for easier processing
            XMLelement[] annotations = new XMLelement[0];
            XMLelements multiAnno = field.getAnnotation(XMLelements.class);
            if (multiAnno != null && multiAnno.value().length != 0) {
                annotations = multiAnno.value();
            }
            XMLelement singleAnno = field.getAnnotation(XMLelement.class);
            if (singleAnno != null) {
                annotations = Arrays.copyOf(annotations, annotations.length + 1);
                annotations[annotations.length - 1] = singleAnno;
            }

            // process @XMLelement annotations
            for (XMLelement annotation : annotations) {
                Class<? extends Object> itemType = annotation.itemType();
                Class<? extends ElementConverter> annotatedConverter = annotation.converter();

                // set to default values according to annotations
                if (itemType.equals(Object.class)) {
                    itemType = null;
                }
                if (annotatedConverter.equals(ElementConverter.class)) {
                    annotatedConverter = null;
                }

                // target field is a collection, so a target converter is a collection converter
                if (collectionConverter != null) {

                    if (annotatedConverter != null) {
                        throw new XliteException("Error: Can  not assign converter for collection " + field.getName() +
                                " in class " + field.getDeclaringClass().getSimpleName() +
                                "When @XMLelement annotation is used on a collection, 'converter' value can not be used. " +
                                "Use 'itemType' instead.");
                    }

                    // if it's a collection, then @XMLelement must have "itemType" value defined
                    if (itemType == null) {
                        throw new XliteException("Error: Can not assign converter for collection " + field.getName() +
                                " in class " + field.getDeclaringClass().getSimpleName() +
                                "When @XMLelement annotation is used on a collection, 'itemType' value must be defined.");
                    }
                    fieldConverter = null;

                } else { // target field is a normal field (not a collection)

                    if (itemType != null) {
                        throw new XliteException("Error: Wrong @XMLelement annotation value on field " + field.getName() +
                                "in class " + field.getDeclaringClass().getName() + ". @XMLelement 'itemType' can only be used on " +
                                "field types that implement Collection.");
                    }

                    // was custom converter assigned via annotation?
                    if (annotatedConverter != null) {
                        try {
                            fieldConverter = annotatedConverter.newInstance();

                            // check that assigned converter can actually convert to the target field type
                            if (!fieldConverter.canConvert(field.getType())) {
                                throw new XliteException("Error: assigned converter type does not match field type.\n" +
                                        "Converter " + fieldConverter.getClass().getName() + " can not be used to convert " +
                                        "data of type " + field.getType() + ".\n" +
                                        "Please check XML annotations on field '" + field.getName() +
                                        "' in class " + field.getDeclaringClass().getName() + ".");
                            }

                        } catch (InstantiationException e) {
                            throw new XliteException("Could not instantiate converter " + annotation.converter().getName() + ". ", e);
                        } catch (IllegalAccessException e) {
                            throw new XliteException("Could not instantiate converter " + annotation.converter().getName() + ". ", e);
                        }

                    } else {
                        // converter was not declared via annotation, so we just use a converter derived from field type
                        fieldConverter = converterByType;
                    }
                }

                // get QName that field maps to
                String elementName = annotation.value().length() != 0 ? annotation.value() :
                        (annotation.name().length() != 0 ? annotation.name() : field.getName());
                NsContext fieldNS = getFieldNamespaces(field);
                NsContext classNS = converter.getClassNamespaces();
                QName qname = getQName(elementName, fieldNS, classNS);

                // get default value of this element
                String defaultValue = annotation.defaultValue();
                if (defaultValue.length() == 0) {
                    defaultValue = null;
                }

                if (fieldConverter != null) {
                    fieldMapper.setConverter(fieldConverter);
                    fieldMapper.setDefaultValue(defaultValue);
                }
                if (itemType != null) {
                    fieldMapper.addMapping(qname, itemType);
                }
                converter.addElementMapper(qname, fieldMapper);

//                String conv = fieldMapper.elementConverter.getClass().equals(ValueConverterWrapper.class) ?
//                        ((ValueConverterWrapper) fieldMapper.elementConverter).valueConverter.getClass().getSimpleName() :
//                        fieldMapper.elementConverter.getClass().getSimpleName();
//
//                System.out.println(currentClass.getSimpleName() + "." + field.getName() + " element:" + elementName + " converter:" + conv);

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
    private QName getQName(String elementName, NsContext fieldNS, NsContext classNS) {

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
     * Searches class for fields that have @XMLattribute annotation and creates a ValueMapper for that field
     *
     * @param converter    AnnotatedClassMapper to which the ValueMapper is referenced
     * @param currentClass Class being inspected for @XMLattribute annotations
     */
    private void processAttributes(Class<? extends Object> currentClass, AnnotatedClassConverter converter) {
        XMLattribute annotation;
        for (Field field : currentClass.getDeclaredFields()) {
            annotation = field.getAnnotation(XMLattribute.class);
            if (annotation != null) {

                // find the appropriate converter
                ValueConverter valueConverter;
                if (annotation.converter().equals(ValueConverter.class)) {  // default converter
                    valueConverter = mappingContext.lookupValueConverter(field.getType());

                } else {  // custom converter assigned via annotation
                    try {
                        valueConverter = annotation.converter().newInstance();

                        // check that assigned converter can actually converto to the target field type
                        if (!valueConverter.canConvert(field.getType())) {
                            throw new XliteException("Error: assigned converter type does not match field type.\n" +
                                    "Converter " + valueConverter.getClass().getName() + " can not be used to convert " +
                                    "data of type " + field.getType() + ".\n" +
                                    "Please check XML annotations on field '" + field.getName() +
                                    "' in class " + field.getDeclaringClass().getName() + ".");
                        }
                    } catch (InstantiationException e) {
                        throw new XliteException("Could not instantiate converter " + annotation.converter().getName() + ". ", e);
                    } catch (IllegalAccessException e) {
                        throw new XliteException("Could not instantiate converter " + annotation.converter().getName() + ". ", e);
                    }
                }

                // get QName that field maps to
                String elementName = annotation.value().length() != 0 ? annotation.value() :
                        (annotation.name().length() != 0 ? annotation.name() : field.getName());
                QName qname = getQName(elementName, getFieldNamespaces(field), converter.getClassNamespaces());

                // SPECIAL CASE!!!
                // XML attributes with empty prefix DO NOT belong to default namespace
                if (qname.getPrefix().equals(XMLConstants.DEFAULT_NS_PREFIX)) {
                    String localPart = qname.getLocalPart();
                    qname = new QName(localPart);
                }

                // get default value of this attribute
                String defaultValue = annotation.defaultValue();
                if (defaultValue.length() == 0) {
                    defaultValue = null;
                }

                converter.addAttributeConverter(qname, new ValueMapper(field, valueConverter, defaultValue));

                System.out.println(currentClass.getSimpleName() + "." + field.getName() + " attribute:" + qname
                        + " converter:" + valueConverter.getClass().getSimpleName());
            }
        }
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
        for (Field field : currentClass.getDeclaredFields()) {
            annotation = field.getAnnotation(XMLtext.class);
            if (annotation != null) {
                found++;
                targetField = field;
                targetAnnotation = annotation;
            }
        }
        if (found > 1) {
            throw new XliteException("Error: Multiple @XMLtext annotations in class "
                    + currentClass.getName() + ". Max one @XMLtext annotation can be present in a class.");
        }
        if (found == 1) {

            // find the appropriate converter
            ValueConverter valueConverter;
            if (targetAnnotation.converter().equals(ValueConverter.class)) {  // default converter
                valueConverter = mappingContext.lookupValueConverter(targetField.getType());

                // check that assigned converter can actually converto to the target field type
                if (!valueConverter.canConvert(targetField.getType())) {
                    throw new XliteException("Error: assigned converter type does not match field type.\n" +
                            "Converter " + valueConverter.getClass().getName() + " can not be used to convert " +
                            "data of type " + targetField.getType() + ".\n" +
                            "Please check XML annotations on field '" + targetField.getName() +
                            "' in class " + targetField.getDeclaringClass().getName() + ".");
                }
            } else {  // custom converter assigned via annotation
                try {
                    valueConverter = targetAnnotation.converter().newInstance();
                } catch (InstantiationException e) {
                    throw new XliteException("Could not instantiate converter " + targetAnnotation.converter().getName() + ". ", e);
                } catch (IllegalAccessException e) {
                    throw new XliteException("Could not instantiate converter " + targetAnnotation.converter().getName() + ". ", e);
                }
            }

            converter.setTextMapper(new ValueMapper(targetField, valueConverter, null));

            System.out.println(currentClass.getSimpleName() + "." + targetField.getName() + " value "
                    + " converter:" + valueConverter.getClass().getSimpleName());
        }
    }


}
