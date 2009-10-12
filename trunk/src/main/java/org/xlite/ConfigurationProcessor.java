package org.xlite;

import org.xlite.converters.ElementConverter;
import org.xlite.converters.EmptyStringConverter;

import java.io.Reader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationProcessor {

    private static Xlite xmlConfigurationParser;

    public static ConfigRootElement processConfiguration(Reader xmlReader, MappingContext mappingContext) {
        try {
            if (xmlConfigurationParser == null) {
                xmlConfigurationParser = new Xlite(ConfigRootElement.class);
                xmlConfigurationParser.addConverter(new EmptyStringConverter());
            }
            ConfigRootElement configRootElement = (ConfigRootElement) xmlConfigurationParser.fromXML(xmlReader);
            processRootElement(configRootElement, mappingContext);
            return configRootElement;
        } catch (XliteException xe) {
            throw new XliteConfigurationException("Error: XML configuration could not be processed: " + xe);
        }
    }

    private static void processRootElement(ConfigRootElement configRootElement, MappingContext mappingContext) {

        // Save processed configuration
        mappingContext.addConfigElement(configRootElement.classType, configRootElement);

        if (configRootElement.element != null) {
            for (ConfigElement nextElement : configRootElement.element) {
                processElement(configRootElement.classType, nextElement, mappingContext);
            }
        }
    }

    private static void processElement(Class elementClass, ConfigElement configElement, MappingContext mappingContext) {

        if (configElement.element != null) {
            // find the field
            Field field = findField(elementClass, configElement.field);

            // next class - derive it from targetType or field type
            Class nextClass = (configElement.targetType == null || configElement.targetType.equals(Object.class))
                    ? field.getType() : configElement.targetType;

            // save processed configuration
            mappingContext.addConfigElement(nextClass, configElement);

            for (ConfigElement nextElement : configElement.element) {
                // process further
                processElement(nextClass, nextElement, mappingContext);
            }
        }
    }


    public static ConfigRootElement processConfiguration(Class rootClass, MappingContext mappingContext) {

        ConfigRootElement rootConfElement = new ConfigRootElement();

        RootElement rootAnnotation = (RootElement) rootClass.getAnnotation(RootElement.class);

        String rootName = rootAnnotation.value().length() != 0 ? rootAnnotation.value()
                : (rootAnnotation.name().length() != 0 ?
                rootAnnotation.name() : rootClass.getSimpleName().toLowerCase());

        rootConfElement.namespace = processNamespaces((Namespaces) rootClass.getAnnotation(Namespaces.class));
        rootConfElement.name = rootName;
        rootConfElement.classType = rootClass;
        rootConfElement.converter = rootAnnotation.converter();
        rootConfElement.attribute = processAttributes(rootClass);
        rootConfElement.text = processText(rootClass);
        rootConfElement.element = processElements(rootClass, mappingContext);

        // passes the element configuration
        processRootElement(rootConfElement, mappingContext);

        return rootConfElement;
//        ConfigMappings cm = new ConfigMappings();
//        cm.mappings.add(rootConfElement);
//        return cm;
    }

    public static ConfigElement processClass(Class elementClass, MappingContext mappingContext) {
        ConfigElement configElement = new ConfigElement();

        configElement.targetType = elementClass;
        configElement.attribute = processAttributes(elementClass);
        configElement.text = processText(elementClass);
        configElement.element = processElements(elementClass, mappingContext);
        configElement.namespace = processNamespaces((Namespaces) elementClass.getAnnotation(Namespaces.class));

        // process elements
        processElement(elementClass, configElement, mappingContext);

        return configElement;
    }

    private static List<ConfigElement> processElements(Class elementClass, MappingContext mappingContext) {
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
                        annotation.itemType(),
                        annotation.format(),
                        annotation.converter()
                );

                // Which class to process next? Defined by itemType or type of field?
                // If itemType is not defined (==Object.class) then derive type from field.
                Class nextClass = annotation.itemType().equals(Object.class) ? field.getType() : annotation.itemType();

                // Process next class if custom converter is not defined 
                if (annotation.converter() != null && !annotation.converter().equals(ElementConverter.class)) {

                    //recursive call to process next class
                    processClass(nextClass, mappingContext);

//                    element.attribute = processAttributes(nextClass);
//                    element.text = processText(nextClass);
//                    element.namespace = processNamespaces((Namespaces) nextClass.getAnnotation(Namespaces.class));
//
//                    //recursive call to process next class
//                    element.element = processElement(nextClass, mappingContext);
                }

                if (elements == null) {
                    elements = new ArrayList<ConfigElement>();
                }
                elements.add(element);
            }
        }
        return elements;
    }

    private static List<ConfigAttribute> processAttributes(Class elementClass) {

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
                            annotation.itemType(),
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

    private static ConfigText processText(Class elementClass) {

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
            throw new XliteConfigurationException("Error: Multiple @Text annotations in class "
                    + elementClass.getName() + ". Max one @Text annotation can be present in a class.");
        }

        if (found == 1) {
            assert annotation != null;
            return new ConfigText(targetField.getName(), annotation.format(), annotation.converter());
        } else {
            return null;
        }
    }

    public static List<Field> getAllFields(Class clazz) {
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

    public static Field findField(Class clazz, String fieldName) {

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

    private static List<ConfigNamespace> processNamespaces(Namespaces nsAnnotation) {
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
