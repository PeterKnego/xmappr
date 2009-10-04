package org.xlite;

import java.io.Reader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationProcessor {

    private static Xlite xmlConfigurationParser = new Xlite(ConfigRootElement.class);

    public static ConfigRootElement processConfiguration(Reader xmlReader) {
        try {
            return (ConfigRootElement) xmlConfigurationParser.fromXML(xmlReader);
        } catch (XliteException xe) {
            throw new XliteConfigurationException("Error: XML configuration could not be processed: " + xe);
        }
    }

    public static ConfigRootElement processConfiguration(Class rootClass) {
        ConfigRootElement rootConfElement = new ConfigRootElement();

        RootElement rootAnnotation = (RootElement) rootClass.getAnnotation(RootElement.class);

        String rootName = rootAnnotation.value().length() != 0 ? rootAnnotation.value()
                : (rootAnnotation.name().length() != 0 ?
                rootAnnotation.name() : rootClass.getSimpleName().toLowerCase());

        rootConfElement.name = rootName;
        rootConfElement.classType = rootClass;
        rootConfElement.attribute = processAttributes(rootClass);
        rootConfElement.text = processText(rootClass);
        rootConfElement.element = processElements(rootClass);
        rootConfElement.namespace = processNamespaces((Namespaces) rootClass.getAnnotation(Namespaces.class));

        return rootConfElement;
    }

    private static List<ConfigElement> processElements(Class elementClass) {
        List<ConfigElement> elements = null;

        for (Field field : getAllFields(elementClass)) {

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
            if (annotations.length != 0) {
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

                    element.attribute = processAttributes(nextClass);
                    element.text = processText(nextClass);
                    element.namespace = processNamespaces((Namespaces) nextClass.getAnnotation(Namespaces.class));
                    //recursive call to process next class
                    element.element = processElements(nextClass);

                    if (elements == null) {
                        elements = new ArrayList<ConfigElement>();
                    }
                    elements.add(element);
                }
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

    private static List<ConfigNamespace> processNamespaces(Namespaces nsAnnotation) {
        List<ConfigNamespace> namespaces = new ArrayList<ConfigNamespace>();
        if (nsAnnotation != null && nsAnnotation.value().length != 0) {
            String[] valueArray = nsAnnotation.value();
            for (String value : valueArray) {
                namespaces.add(new ConfigNamespace(value));
            }
        }
        return namespaces;
    }

}
