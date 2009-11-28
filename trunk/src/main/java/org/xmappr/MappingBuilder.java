package org.xmappr;

import org.xmappr.converters.*;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.lang.reflect.Field;
import java.util.*;

public class MappingBuilder {

    private final MappingContext mappingContext;

    public MappingBuilder(final MappingContext mappingContext) {
        this.mappingContext = mappingContext;
    }

    /**
     * Processes given mapping configuration and builds in-memory tree of mappers and converters.
     *
     * @param config
     * @return
     */
    public RootMapper processConfiguration(ConfigRootElement config) {

        ElementConverter rootConverter;

        // If custom converter is defined then use it
        if (config.converter != null && !config.converter.equals(ElementConverter.class)) {
            rootConverter = initializeConverter(config.converter);
        } else { // try looking in predefined converters
            rootConverter = mappingContext.lookupElementConverter(config.classType);
        }

        // find and process class namespace context
        NsContext rootNs = processClassNamespaces(config.namespace);

        // ElementConverter was not found - use ClassConverter
        if (rootConverter == null) {
            ClassConverter classConverter = new ClassConverter(config.classType);

            // Set class namespace context - needed for resolving all names of elements and attributes.
            // Must be set before processing XML attributes or elements.
            classConverter.setClassNamespaces(rootNs);

            // find and process @Attribute annotations
            processAttributes(config.attribute, classConverter);
            // find and process @Text annotation
            processText(config.text, config.element, classConverter);
            // find and process @Element annotations
            processElements(config.element, classConverter);

            rootConverter = classConverter;
        }

        QName rootQName = mappingContext.getQName(config.name, null, rootNs, config.classType.getName(), "");

        return new RootMapper(rootQName, config.classType, rootConverter, mappingContext);
    }

    public ClassConverter createClassConverter(Class targetClass, ConfigElement config) {

        // Check that configuration contains subnodes
        if (config.element == null && config.attribute == null && config.text == null) {
            return null;
        }

        ClassConverter classConverter = new ClassConverter(targetClass);
        mappingContext.addConverter(classConverter);

        NsContext namespaces = processClassNamespaces(config.namespace);

        /*
        * Set class namespace context - needed for resolving all names of elements and attributes.
        * Must be set before processing XML attributes or elements.
        */
        classConverter.setClassNamespaces(namespaces);

        // find and process @Attribute annotations
        processAttributes(config.attribute, classConverter);
        // find and process @Text annotation
        processText(config.text, config.element, classConverter);
        // find and process @Element annotations
        processElements(config.element, classConverter);

        return classConverter;
    }

    /**
     * Processes @XMLnamespaces annotations defined on a Class.
     *
     * @param namespaces
     */
    private NsContext processClassNamespaces(List<ConfigNamespace> namespaces) {
        NsContext classNS = new NsContext();
        if (namespaces != null) {
            for (ConfigNamespace namespace : namespaces) {
                classNS.addNamespace(namespace.prefix, namespace.uri);
            }
        }
        return classNS;
    }

    /**
     * Searches given class for fields that have @XMLelement annotation.
     *
     * @param configElements A class to be inspected for @XMLelement annotations.
     * @param classConverter AnnotatedClassMapper that coresponds in
     */
    private void processElements(List<ConfigElement> configElements, ClassConverter classConverter) {

        if (configElements == null) return;

        boolean isElementCatcher = false;
        Map<String, ElementMapper> fieldMapperCache = new HashMap<String, ElementMapper>();

        for (ConfigElement configElement : configElements) {

            Field field = getFieldFromName(configElement.field, classConverter.getTargetClass());

            // find the converter by the field type
            ElementConverter converterByType = mappingContext.lookupElementConverter(field.getType(), false);

            // getValue converter for the class that the field references
            ElementConverter fieldConverter = null;

            // if target field is a collection, then a collection converter must be defined
            boolean isCollectionConverter = false;
            CollectionConverting collectionConverter = null;
            if (converterByType != null && CollectionConverting.class.isAssignableFrom(converterByType.getClass())) {
                collectionConverter = (CollectionConverting) converterByType;
                isCollectionConverter = true;
            }

            // Is ElementMapper for this field already initialized?
            ElementMapper fieldMapper = fieldMapperCache.get(configElement.field);

            // If not then initialize it
            if (fieldMapper == null) {
                fieldMapper = new ElementMapper(field, collectionConverter, mappingContext);
                // then save it
                fieldMapperCache.put(configElement.field, fieldMapper);
            }

            Class<?> targetType = configElement.targetType;
            Class<? extends Converter> annotatedConverter = configElement.converter;

            // setValue to default values according to annotations
            if (Object.class.equals(targetType)) {
                targetType = null;
            }
            if (annotatedConverter == null || annotatedConverter.equals(ElementConverter.class)) {
                annotatedConverter = null;
            }

            // getValue QName that field maps to
            String elementName = configElement.name;
            QName qname = mappingContext.getQName(elementName, getFieldNamespaces(field), classConverter.getClassNamespaces(),
                    classConverter.getTargetClass().getName(), field.getName());

            // element catcher for wildcard names
            if (elementName.equals("*")) {
                // double use of element catcher @Element("*") within a single class
                if (isElementCatcher) {
                    throw new XmapprConfigurationException("Error: Incorrect use of Element(\"*\")" +
                            field.getName() + " in class " + field.getDeclaringClass().getSimpleName() +
                            "Wildcard name mapping @Element(\"*\") can be used only one time within a class");
                }
                isElementCatcher = true;


                if (annotatedConverter != null || targetType != null) {
                    throw new XmapprConfigurationException("Error: Can  not assign converter for " +
                            "collection " + field.getName() +
                            " in class " + field.getDeclaringClass().getSimpleName() +
                            "When @Element annotation name is a wildcard name \"*\", 'converter' or " +
                            "'targetType' values can not be used.");
                }

                // default converter for element catcher
                fieldConverter = mappingContext.lookupElementConverter(DOMelement.class);

                // default targetType for element catcher (in case that target field is a Collection)
                targetType = DOMelement.class;

            } else { // normal name-to-field mapping

                // target field is a collection
                if (isCollectionConverter) {

                    // if it's a collection, then @Element must have either "targetType" or 'converter' value defined
                    if (annotatedConverter == null && targetType == null) {
                        throw new XmapprConfigurationException("Error: Can  not assign converter " +
                                "for collection " + field.getName() +
                                " in class " + field.getDeclaringClass().getSimpleName() +
                                ". When @Element annotation is used on a collection, " +
                                "either 'converter' value or 'targetType' value must be declared.");
                    }

                    // was custom converter assigned via annotation?
                    if (annotatedConverter != null) {
                        fieldConverter = initializeConverter(annotatedConverter);

                        // check if item type is set and
                        // that assigned converter can actually convert to this type
                        if (targetType != null && !fieldConverter.canConvert(targetType)) {
                            throw new XmapprConfigurationException("Error: assigned converter type does not " +
                                    "match field type.\n" +
                                    "Converter " + fieldConverter.getClass().getName() +
                                    " can not be used to convert data of type " + field.getType() + ".\n" +
                                    "Please check XML annotations on field '" + field.getName() +
                                    "' in class " + field.getDeclaringClass().getName() + ".");
                        }

                    } else {
                        // converter was not declared via annotation, so we just use a converter derived from field type
                        fieldConverter = mappingContext.lookupElementConverter(targetType, true);
//                        fieldConverter = null;
                    }

                } else { // target field is a normal field (not a collection)

//                            if (targetType != null) {
//                                throw new XmapprConfigurationException("Error: Wrong @Element annotation value on " +
//                                        "field " + field.getName() +
//                                        "in class " + field.getDeclaringClass().getName() + ". " +
//                                        "@Element 'targetType' can only be used on field types that implement Collection.");
//                            }

                    // was custom converter assigned via annotation?
                    if (annotatedConverter != null) {
                        fieldConverter = initializeConverter(annotatedConverter);

                        // check that assigned converter can actually convert to the target field type
                        if (!fieldConverter.canConvert(field.getType())) {
                            throw new XmapprConfigurationException("Error: assigned converter type does not " +
                                    "match field type.\nConverter " + fieldConverter.getClass().getName() +
                                    " can not be used to convert " +
                                    "data of type " + field.getType() + ".\n" +
                                    "Please check XML annotations on field '" + field.getName() +
                                    "' in class " + field.getDeclaringClass().getName() + ".");
                        }

                    } else {
                        // converter was not declared via annotation
                        if (converterByType == null) {

                            // This is a hack, that will trigger an appropriate exception.
                            // Since we already know that converterByType was not found we do the lookup again,
                            // this time allowing exception to be thrown
                            fieldConverter = mappingContext.lookupElementConverter(field.getType(), true);
                        } else {
//                            // use a converter derived from field type
                            fieldConverter = converterByType;
                        }
                    }
                }
            }

            // getValue default value of this element
            String defaultValue = configElement.defaultvalue;
            if ("".equals(defaultValue)) {
                defaultValue = null;
            }

            if (isCollectionConverter) {
                fieldMapper.addMapping(qname, fieldConverter, targetType);
            } else {
                fieldMapper.setConverter(fieldConverter);
                fieldMapper.setDefaultValue(defaultValue);
                fieldMapper.setFormat(configElement.format);
                fieldMapper.setTargetType(field.getType());
            }

            if (isElementCatcher) {
                fieldMapper.setConverter(fieldConverter);
                classConverter.setElementCatcher(fieldMapper);
                classConverter.addElementMapper(new QName("*"), fieldMapper);
            } else { // normal element mapping
                classConverter.addElementMapper(qname, fieldMapper);
            }

//                System.out.println(currentClass.getSimpleName() + "." + field.getName() + " element:" +
//                          elementName + " converter:" + conv);

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
                throw new XmapprConfigurationException("Error: Can not instantiate Converter " + annotatedConverter +
                        ", beacuse it's of wrong type: Converters defined on @Element annotation must" +
                        "implement either ElementCoverter or ValueConverter");
            }

        } catch (Exception e) {
            throw new XmapprException("Could not instantiate converter " + annotatedConverter.getName() + ". ", e);
        }
        return fieldConverter;
    }

    /**
     * @param configAttributes
     * @param classConverter
     */

    private void processAttributes(List<ConfigAttribute> configAttributes, ClassConverter classConverter) {

        if (configAttributes == null) return;

        for (ConfigAttribute configAttribute : configAttributes) {

            Field field = getFieldFromName(configAttribute.field, classConverter.getTargetClass());
            Class fieldType = field.getType();
            String fieldName = configAttribute.name;

            // find the converter by the field type
            ValueConverter converterByType = mappingContext.lookupValueConverter(fieldType);

            // get ValueConverter for the class that the field references
            ValueConverter fieldConverter;

            // the type of the target object
            Class targetType = (configAttribute.targetType == null || configAttribute.targetType.equals(Object.class))
                    ? null : configAttribute.targetType;

            Class<? extends ValueConverter> annotatedConverter = configAttribute.converter;
            boolean isAttributeCatcher = false;

            // set to default values according to annotations
            if (annotatedConverter == null || annotatedConverter.equals(ValueConverter.class)) {
                annotatedConverter = null;
            }

            QName qname = mappingContext.getQName(fieldName, null, classConverter.getClassNamespaces(),
                    classConverter.getTargetClass().getName(), configAttribute.field);

            // Is target field a Map?
            if (Map.class.isAssignableFrom(fieldType)) {

                if (annotatedConverter != null) { // 'converter' defined
                    try {
                        fieldConverter = annotatedConverter.newInstance();
                    } catch (InstantiationException e) {
                        throw new XmapprException("Could not instantiate converter " +
                                annotatedConverter.getName() + ". ", e);
                    } catch (IllegalAccessException e) {
                        throw new XmapprException("Could not instantiate converter " +
                                annotatedConverter.getName() + ". ", e);
                    }
                } else {
                    // if targetType is not defined we assign it a default type: String.class
                    if (targetType == null) {
                        targetType = String.class;
                    }
                    fieldConverter = mappingContext.lookupValueConverter(targetType);
                }

                // attribute catcher
                if (fieldName.equals("*")) {
                    isAttributeCatcher = true;
                }

            } else { // target field is a normal field (not a collection)

                // if targetType is not defined we assign it based on field type
                if (targetType == null) {
                    targetType = fieldType;
                }

                // was custom converter assigned via annotation?
                if (annotatedConverter != null) {
                    try {
                        fieldConverter = annotatedConverter.newInstance();
                    } catch (Exception e) {
                        throw new XmapprException("Could not instantiate converter " +
                                annotatedConverter.getName() + ". ", e);
                    }

                    // check that assigned converter can actually convert to the target field type
                    if (!fieldConverter.canConvert(fieldType)) {
                        throw new XmapprConfigurationException("Error: assigned converter type " +
                                "does not match field type.\n" +
                                "Converter " + fieldConverter.getClass().getName() + " can not be used " +
                                "to convert data of type " + fieldType + ".\n" +
                                "Please check XML annotations on field '" + fieldName +
                                "' in class " + field.getDeclaringClass().getName() + ".");
                    }

                } else {
                    // converter was not declared via annotation, so we just use a converter derived from field type
                    fieldConverter = converterByType;
                }

                // attribute catcher
                if (fieldName.equals("*")) {
                    throw new XmapprConfigurationException("Error: Wrong @Attribute annotation value " +
                            "on field " + fieldName + " in class " + fieldType.getDeclaringClass().getName() +
                            ". @Attribute wildcard name \"*\" can only be used on " +
                            "field types that implement Map.");
                }
            }


            if (isAttributeCatcher) {
                // assign an attribute catcher
                classConverter.setAttributeCatcher(
                        new AttributeMapper(field, targetType, fieldConverter, null, configAttribute.format)
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
                String defaultValue = (configAttribute.defaultvalue == null || configAttribute.defaultvalue.length() == 0)
                        ? null : configAttribute.defaultvalue;

                classConverter.addAttributeMapper(
                        qname,
                        new AttributeMapper(field, targetType, fieldConverter, defaultValue, configAttribute.format)
                );
            }
        }
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
     * Processes Text element
     *
     * @param configText
     * @param classConverter
     */
    private void processText(ConfigText configText, List<ConfigElement> configElements, ClassConverter classConverter) {

        if (configText == null) return;

        Field targetField = getFieldFromName(configText.field, classConverter.getTargetClass());

        // find the appropriate converter
        ValueConverter valueConverter;
        CollectionConverting collectionConverter = null;
        Class targetType = null;

        // custom converter assigned via annotation?
        if (configText.converter != null && !configText.converter.equals(ValueConverter.class)) {
            try {
                valueConverter = configText.converter.newInstance();
            } catch (Exception e) {
                throw new XmapprException("Could not instantiate converter " +
                        configText.converter.getName() + ". ", e);
            }

        } else {  // default converter derived from field type

            // is target type a collection?
            if (Collection.class.isAssignableFrom(targetField.getType())) {

                collectionConverter = (CollectionConverting) mappingContext.lookupElementConverter(targetField.getType());
                targetType = String.class;
//                    throw new XmapprConfigurationException("Error: @Text annotation on a collection field of "
//                            + currentClass.getName() + ". No converter parameter provided.");
            } else {
                // choose converter according to field type
                targetType = targetField.getType();
            }
            valueConverter = mappingContext.lookupValueConverter(targetType);


            // check that assigned converter can actually convert to the target field type
            if (!valueConverter.canConvert(targetType)) {
                throw new XmapprConfigurationException("Error: assigned converter type does not match field type.\n" +
                        "Converter " + valueConverter.getClass().getName() + " can not be used to convert " +
                        "data of type " + targetField.getType() + ".\n" +
                        "Please check XML annotations on field '" + targetField.getName() +
                        "' in class " + targetField.getDeclaringClass().getName() + ".");
            }

        }

        // check if this field is mapped both to XML text and XML elements
        boolean isIntermixed = false;
        if (configElements != null) {
            for (ConfigElement configElement : configElements) {
                if (configElement.field.equals(configText.field)) {
                    isIntermixed = true;
                }
            }
        }

        classConverter.setTextMapper(new TextMapper(targetField, targetType, valueConverter,
                collectionConverter, isIntermixed, configText.format));

//            System.out.println(currentClass.getSimpleName() + "." + targetField.getName() + " value "
//                    + " converter:" + valueConverter.getClass().getSimpleName());
    }

    private Field getFieldFromName(String fieldName, Class targetClass) {

        // traverse the class hierarchy from given class up to Object.class
        Class cl = targetClass;
        do {
            for (Field field : cl.getDeclaredFields()) {

                // search for given field name
                if (field.getName().equals(fieldName)) {
                    return field;
                }
            }
            cl = cl.getSuperclass();
        } while (cl != Object.class);

        // no field with given name was found
        throw new XmapprConfigurationException("Error: Could not find field '" + fieldName + "'" +
                " in class " + targetClass.getName());
    }

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
