package org.xmappr;

import org.xmappr.converters.EmptyStringConverter;

import java.io.Reader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigurationProcessor {

    private static Xmappr xmlConfigurationParser;

    public static ConfigRootElement processConfiguration(Reader xmlReader, MappingContext mappingContext) {
        try {
            if (xmlConfigurationParser == null) {
                xmlConfigurationParser = new Xmappr(ConfigRootElement.class);
                xmlConfigurationParser.addConverter(new EmptyStringConverter());
            }
            ConfigRootElement configRootElement = (ConfigRootElement) xmlConfigurationParser.fromXML(xmlReader);
            processRootElement(configRootElement, mappingContext);
            return configRootElement;
        } catch (XmapprException xe) {
            throw new XmapprConfigurationException("Error: XML configuration could not be processed: " + xe);
        }
    }

    public static ConfigRootElement processRootClassAnnotations(Class rootClass, MappingContext mappingContext) {

        ConfigRootElement rootConfElement = new ConfigRootElement();

        RootElement rootAnnotation = (RootElement) rootClass.getAnnotation(RootElement.class);

        String rootName = rootAnnotation.value().length() != 0 ? rootAnnotation.value()
                : (rootAnnotation.name().length() != 0 ?
                rootAnnotation.name() : rootClass.getSimpleName().toLowerCase());

        rootConfElement.namespace = processNamespaceAnnotations((Namespaces) rootClass.getAnnotation(Namespaces.class));
        rootConfElement.name = rootName;
        rootConfElement.classType = rootClass;
        rootConfElement.converter = rootAnnotation.converter();
        rootConfElement.attribute = processAttributeAnnotations(rootClass);
        rootConfElement.text = processTextAnnotations(rootClass);

        Map<Class, ConfigElement> classCache = new HashMap<Class, ConfigElement>();
//        classCache.put(rootClass, );
        rootConfElement.element = processElementAnnotations(rootClass, mappingContext, classCache);

        // passes the element configuration
        processRootElement(rootConfElement, mappingContext);

        return rootConfElement;
//        ConfigMappings cm = new ConfigMappings();
//        cm.mappings.add(rootConfElement);
//        return cm;
    }

    public static ConfigElement processClassAnnotations(Class elementClass, MappingContext mappingContext) {
        Map<Class, ConfigElement> classCache = new HashMap<Class, ConfigElement>();

        // ConfigElement at the top of the hierarchy of ConfigElements
        ConfigElement topConfigElement = new ConfigElement();
        processNextClass(elementClass, topConfigElement, mappingContext, classCache);

        // process elements
        processConfigElement(elementClass, topConfigElement, mappingContext);

        return topConfigElement;
    }

    private static void processNextClass(Class elementClass, ConfigElement configElement,
                                         MappingContext mappingContext, Map<Class, ConfigElement> classCache) {

        classCache.put(elementClass, configElement);

        configElement.targetType = elementClass;
        configElement.attribute = processAttributeAnnotations(elementClass);
        configElement.text = processTextAnnotations(elementClass);

        configElement.element = processElementAnnotations(elementClass, mappingContext, classCache);

        configElement.namespace = processNamespaceAnnotations((Namespaces) elementClass.getAnnotation(Namespaces.class));

    }

    private static void processRootElement(ConfigRootElement configRootElement, MappingContext mappingContext) {

        // Save processed configuration
        mappingContext.addConfigElement(configRootElement.classType, configRootElement);

        if (configRootElement.element != null) {
            for (ConfigElement nextElement : configRootElement.element) {
                processConfigElement(configRootElement.classType, nextElement, mappingContext);
            }
        }
    }

    private static void processConfigElement(Class elementClass, ConfigElement configElement, MappingContext mappingContext) {

        // find the field
        Field field = findField(elementClass, configElement.field);

        // next class - derive it from targetType or field type
        Class nextClass = (configElement.targetType == null || configElement.targetType.equals(Object.class))
                ? field.getType() : configElement.targetType;

        if (!mappingContext.configElementExists(nextClass)) {

            // save processed configuration
            mappingContext.addConfigElement(nextClass, configElement);

            if (configElement.element != null) {
                for (ConfigElement nextElement : configElement.element) {
                    // process further
                    processConfigElement(nextClass, nextElement, mappingContext);
                }
            }
        }
    }

    private static List<ConfigElement> processElementAnnotations(Class elementClass, MappingContext mappingContext,
                                                                 Map<Class, ConfigElement> classCache) {
        List<ConfigElement> elements = null;

        List<Field> allfields = getAllFields(elementClass);
        for (Field field : allfields) {

            // collect all @Attribute annotations in a single array for easier processing
            Element[] annotations = new Element[0];
            Elements multiAnno = field.getAnnotation(Elements.class);
            if (multiAnno != null && multiAnno.value().length != 0) {
                annotations = multiAnno.value();
            }
            Element singleAnno = field.getAnnotation(Element.class);
            if (singleAnno != null) {
                Element[] copy = new Element[annotations.length + 1];
                System.arraycopy(annotations, 0, copy, 0, annotations.length);
                annotations = copy;
                annotations[annotations.length - 1] = singleAnno;
            }
            // are there any @Attribute annotations on this field
            for (Element annotation : annotations) {

                String elementName = annotation.value().length() != 0 ? annotation.value()
                        : (annotation.name().length() != 0 ? annotation.name() : field.getName());

                ConfigElement element = new ConfigElement(
                        elementName,
                        field.getName(),
                        annotation.defaultValue(),
                        annotation.targetType(),
                        annotation.format(),
                        annotation.converter()
                );

                // Which class to process next? Defined by targetType or type of field?
                // If targetType is not defined (==Object.class) then derive type from field.
                Class nextClass = annotation.targetType().equals(Object.class) ? field.getType() : annotation.targetType();

                // Process next class if it's converter is not yet defined 
                if (!mappingContext.isElementConverterDefined(nextClass)) {

                    // Was this class already processed?
                    // This eliminates loops.
                    if (classCache.containsKey(nextClass)) {

                        // use an existing
                        element = classCache.get(nextClass);
                    } else {

                        // recursive call to process next class
                        processNextClass(nextClass, element, mappingContext, classCache);
                    }
                }

                if (elements == null) {
                    elements = new ArrayList<ConfigElement>();
                }
                elements.add(element);
            }
        }
        return elements;
    }

    private static List<ConfigAttribute> processAttributeAnnotations(Class elementClass) {

        List<ConfigAttribute> attributes = null;

        for (Field field : getAllFields(elementClass)) {

            // collect all @Attribute annotations in a single array for easier processing
            Attribute[] annotations = new Attribute[0];
            Attributes multiAnno = field.getAnnotation(Attributes.class);
            if (multiAnno != null && multiAnno.value().length != 0) {
                annotations = multiAnno.value();
            }
            Attribute singleAnno = field.getAnnotation(Attribute.class);
            if (singleAnno != null) {
                Attribute[] copy = new Attribute[annotations.length + 1];
                System.arraycopy(annotations, 0, copy, 0, annotations.length);
                annotations = copy;
                annotations[annotations.length - 1] = singleAnno;
            }
            // are there any @Attribute annotations on this field
            if (annotations.length != 0) {
                for (Attribute annotation : annotations) {

                    String elementName = annotation.value().length() != 0 ? annotation.value()
                            : (annotation.name().length() != 0 ? annotation.name() : field.getName());

                    ConfigAttribute attribute = new ConfigAttribute(
                            elementName,
                            field.getName(),
                            annotation.defaultValue(),
                            annotation.targetType(),
                            annotation.format(),
                            annotation.converter()
                    );

                    if (attributes == null) {
                        attributes = new ArrayList<ConfigAttribute>();
                    }
                    attributes.add(attribute);
                }
            }
        }

        return attributes;
    }

    private static ConfigText processTextAnnotations(Class elementClass) {

        int found = 0;
        Text annotation = null;
        Field targetField = null;
        for (Field field : getAllFields(elementClass)) {
            if (field.getAnnotation(Text.class) != null) {
                found++;
                annotation = field.getAnnotation(Text.class);
                targetField = field;
            }
        }
        if (found > 1) {
            throw new XmapprConfigurationException("Error: Multiple @Text annotations in class "
                    + elementClass.getName() + ". Max one @Text annotation can be present in a class.");
        }

        if (found == 1) {
            assert annotation != null;
            return new ConfigText(targetField.getName(), annotation.format(), annotation.converter());
        } else {
            return null;
        }
    }

    private static List<Field> getAllFields(Class clazz) {
        List<Field> fields = new ArrayList<Field>();

        // only process real classes
        if (clazz.isPrimitive()) {
            return fields;
        }

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

    private static Field findField(Class clazz, String fieldName) {

        // only process real classes
        if (clazz.isPrimitive()) {
            return null;
        }

        Class cl = clazz;
        do {
            for (Field field : cl.getDeclaredFields()) {
                if (!field.isSynthetic() && field.getName().equals(fieldName)) {
                    return field;
                }
            }
            cl = cl.getSuperclass();
        } while (cl != Object.class);
        return null;
    }

    private static List<ConfigNamespace> processNamespaceAnnotations(Namespaces nsAnnotation) {
        List<ConfigNamespace> namespaces = null;
        if (nsAnnotation != null && nsAnnotation.value().length != 0) {
            String[] valueArray = nsAnnotation.value();
            for (String value : valueArray) {
                if (namespaces == null) {
                    namespaces = new ArrayList<ConfigNamespace>();
                }
                namespaces.add(new ConfigNamespace(value));
            }
        }
        return namespaces;
    }

}
