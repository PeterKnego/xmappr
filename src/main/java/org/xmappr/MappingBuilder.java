package org.xmappr;

import org.xmappr.converters.*;
import org.xmappr.mappers.AttributeMapper;
import org.xmappr.mappers.ElementMapper;
import org.xmappr.mappers.TextMapper;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            rootConverter = mappingContext.lookupElementConverter(config.classType, null, false);
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

        QName rootQName = mappingContext.getQName(config.name, null, rootNs);

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

        boolean isAlreadyWildcardMapping = false;
        Map<String, ElementMapper> fieldMapperCache = new HashMap<String, ElementMapper>();

        for (ConfigElement configElement : configElements) {

            // custom converter defined
            Class<? extends Converter> customConverter = configElement.converter;

            // If target is a collection, then a collection converter must be defined.
            // This will also pick up any custom-defined collection converters.
            CollectionConverting collectionConverter = configElement.isCollection ?
                    (CollectionConverting) mappingContext.lookupElementConverter(configElement.accessorType, null, false) :
                    null;

            // Is ElementMapper for this field already initialized?
            ElementMapper fieldMapper = initializeFieldMapper(fieldMapperCache, configElement, collectionConverter);

            // getValue QName that field maps to
            QName qname = mappingContext.getQName(configElement.name, getNamespaces(configElement), classConverter.getClassNamespaces());

            // wildcard mapping - maps any element name to given field
            if (configElement.name.equals("*")) {

                assignWildcardMapper(classConverter, configElement, collectionConverter, fieldMapper, qname);
            } else { // normal name-to-field mapping

                // target field is a collection
                if (configElement.isCollection) {

                    assignCollectionConverter(classConverter, configElement, fieldMapper, qname);

                } else { // target field is a normal field (not a collection)

                    assignFieldConverter(classConverter, configElement, fieldMapper, qname);
                }
            }
        }
    }

    private void assignFieldConverter(ClassConverter classConverter,
                                      ConfigElement configElement,
                                      ElementMapper fieldMapper,
                                      QName qname) {

        Class useType = configElement.targetType != null ? configElement.targetType : configElement.converterType;

        // was custom converter assigned via annotation?
        ElementConverter fieldConverter;
        if (configElement.converter != null) {
            fieldConverter = initializeConverter(configElement.converter);
        } else {
            fieldConverter = mappingContext.lookupElementConverter(useType, configElement, true);
        }

        fieldMapper.addMapping(qname, fieldConverter, useType);
        fieldMapper.setDefaultValue(configElement.defaultvalue);
        fieldMapper.setFormat(configElement.format);
        classConverter.addElementMapper(qname, fieldMapper);
    }

    private void assignCollectionConverter(ClassConverter classConverter, ConfigElement configElement, ElementMapper fieldMapper,
                                           QName qname) {


        // if 'targetType' is not defined we use parametrized type instead
        Class useType = configElement.targetType != null ? configElement.targetType : configElement.converterType;

        // was custom converter assigned via annotation?
        ElementConverter fieldConverter;
        if (configElement.converter != null) {
            fieldConverter = initializeConverter(configElement.converter);
        } else {
            // converter was not declared via annotation, so we just use a converter derived from field type
            fieldConverter = mappingContext.lookupElementConverter(useType, configElement, true);
        }

        fieldMapper.addMapping(qname, fieldConverter, useType);
        classConverter.addElementMapper(qname, fieldMapper);
    }

    private void assignWildcardMapper(ClassConverter classConverter,
                                      ConfigElement configElement,
                                      CollectionConverting collectionConverter,
                                      ElementMapper fieldMapper,
                                      QName qname) {

        ElementConverter fieldConverter;// was custom converter assigned via annotation?
        fieldConverter = initializeConverter(configElement.converter);

        if (collectionConverter == null) {
            fieldMapper.setDefaultValue(configElement.defaultvalue);
            fieldMapper.setFormat(configElement.format);
        }

        fieldMapper.addMapping(qname, fieldConverter, configElement.targetType);
        fieldMapper.setWildcardConverter(fieldConverter);
        classConverter.setElementCatcher(fieldMapper);
        classConverter.addElementMapper(new QName("*"), fieldMapper);
    }

    private ElementMapper initializeFieldMapper(Map<String, ElementMapper> fieldMapperCache, ConfigElement configElement,
                                                CollectionConverting collectionConverter) {

        ElementMapper fieldMapper = fieldMapperCache.get(configElement.field);
        // If not then initialize it
        if (fieldMapper == null) {
            fieldMapper = new ElementMapper(configElement.targetField, configElement.getterMethod,
                    configElement.setterMethod, collectionConverter, mappingContext);
            // then save it
            fieldMapperCache.put(configElement.field, fieldMapper);
        }
        return fieldMapper;
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
                        ", because it's of wrong type: Converters defined on @Element annotation must" +
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

            Field field = configAttribute.targetField;
            Class accessorType = configAttribute.accessorType;
            String attributeName = configAttribute.name;

            // find the converter by the field type
            ValueConverter baseConverter = mappingContext.lookupValueConverter(accessorType);

            // get ValueConverter for the class that the field references
            ValueConverter converter;

            // the type of the target object
            Class targetType = (configAttribute.targetType == null || configAttribute.targetType.equals(Object.class))
                    ? null : configAttribute.targetType;

            Class<? extends ValueConverter> annotatedConverter = configAttribute.converter;
            boolean isAttributeCatcher = false;

            // set to default values according to annotations
            if (annotatedConverter == null || annotatedConverter.equals(ValueConverter.class)) {
                annotatedConverter = null;
            }

            QName qname = mappingContext.getQName(attributeName, null, classConverter.getClassNamespaces());

            // Is target field a Map?
            if (Map.class.isAssignableFrom(accessorType)) {

                if (annotatedConverter != null) { // 'converter' defined
                    try {
                        converter = annotatedConverter.newInstance();
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
                    converter = mappingContext.lookupValueConverter(targetType);
                }

                // attribute catcher
                if (attributeName.equals("*")) {
                    isAttributeCatcher = true;
                }

            } else { // target field is a normal field (not a collection)

                // if targetType is not defined we assign it based on field type
                if (targetType == null) {
                    targetType = accessorType;
                }

                // was custom converter assigned via annotation?
                if (annotatedConverter != null) {
                    try {
                        converter = annotatedConverter.newInstance();
                    } catch (Exception e) {
                        throw new XmapprException("Could not instantiate converter " +
                                annotatedConverter.getName() + ". ", e);
                    }

                    // check that assigned converter can actually convert to the target field type
                    if (!converter.canConvert(accessorType)) {
                        throw new XmapprConfigurationException("Error: assigned converter type " +
                                "does not match field type.\n" +
                                "Converter " + converter.getClass().getName() + " can not be used " +
                                "to convert data of type " + accessorType + ".\n" +
                                "Please check XML annotations on field '" + attributeName +
                                "' in class " + field.getDeclaringClass().getName() + ".");
                    }

                } else {
                    // converter was not declared via annotation, so we just use a converter derived from field type
                    converter = baseConverter;
                }

                // attribute catcher
                if (attributeName.equals("*")) {
                    throw new XmapprConfigurationException("Error: Wrong @Attribute annotation value " +
                            "on field " + attributeName + " in class " + accessorType.getDeclaringClass().getName() +
                            ". @Attribute wildcard name \"*\" can only be used on " +
                            "field types that implement Map.");
                }
            }


            if (isAttributeCatcher) {
                // assign an attribute catcher
                classConverter.setAttributeCatcher(
                        new AttributeMapper(field, configAttribute.getterMethod, configAttribute.setterMethod,
                                accessorType, targetType, converter, null, configAttribute.format)
                );
            } else {
                // normal one-to-one attribute mapping

                // SPECIAL CASE!!!
                // XML attributes with empty prefix DO NOT belong to default namespace
                if (qname.getPrefix().equals(XMLConstants.DEFAULT_NS_PREFIX)) {
                    String localPart = qname.getLocalPart();
                    qname = new QName(localPart);
                }

                // get default value of this element
                String defaultValue = (configAttribute.defaultvalue == null || configAttribute.defaultvalue.length() == 0)
                        ? null : configAttribute.defaultvalue;

                classConverter.addAttributeMapper(
                        qname,
                        new AttributeMapper(field, configAttribute.getterMethod, configAttribute.setterMethod,
                                accessorType, targetType, converter, defaultValue, configAttribute.format)
                );
            }
        }
    }


    private NsContext getNamespaces(ConfigElement configElement) {

        NsContext fieldNS = new NsContext();
        if (configElement.namespace != null) {
            for (ConfigNamespace configNamespace : configElement.namespace) {
                fieldNS.addNamespace(configNamespace.prefix, configNamespace.uri);
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

        Class accessorType = configText.accessorType;

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
            if (Collection.class.isAssignableFrom(accessorType)) {

                ElementConverter result;
                synchronized (mappingContext) {
                    result = mappingContext.lookupElementConverter(accessorType, null, true);
                }
                collectionConverter = (CollectionConverting) result;
                targetType = String.class;
//                    throw new XmapprConfigurationException("Error: @Text annotation on a collection field of "
//                            + currentClass.getName() + ". No converter parameter provided.");
            } else {
                // choose converter according to field type
                targetType = accessorType;
            }
            valueConverter = mappingContext.lookupValueConverter(targetType);


            // check that assigned converter can actually convert to the target field type
            if (!valueConverter.canConvert(targetType)) {
                throw new XmapprConfigurationException("Error: assigned converter type does not match field type.\n" +
                        "Converter " + valueConverter.getClass().getName() + " can not be used to convert " +
                        "data of type " + accessorType + ".\n");
            }

        }

        // check if this field is mapped both to XML text and XML elements
        boolean isIntermixed = false;
        if (configElements != null) {
            for (ConfigElement configElement : configElements) {
                if (configElement.field != null && configElement.field.equals(configText.field)
                        || (configElement.getterMethod != null && configElement.getterMethod.equals(configText.getterMethod))
                        || (configElement.setterMethod != null && configElement.setterMethod.equals(configText.setterMethod))) {
                    isIntermixed = true;
                }
            }
        }

        classConverter.setTextMapper(new TextMapper(configText.targetField, configText.getterMethod, configText.setterMethod,
                targetType, valueConverter, collectionConverter, isIntermixed, configText.format));

//            System.out.println(currentClass.getSimpleName() + "." + accessorType.getName() + " value "
//                    + " converter:" + valueConverter.getClass().getSimpleName());
    }

    private static Field getFieldFromName(String fieldName, Class targetClass) {

        try {
            return targetClass.getField(fieldName);
        } catch (NoSuchFieldException e) {
            // no field with given name was found
            throw new XmapprConfigurationException("Error: Could not find field '" + fieldName + "'" +
                    " in class " + targetClass.getName());
        }
    }
}
