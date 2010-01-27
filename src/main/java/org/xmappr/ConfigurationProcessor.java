package org.xmappr;

import org.xmappr.converters.EmptyStringConverter;
import org.xmappr.converters.ValueConverter;

import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigurationProcessor {

    private static Xmappr xmlConfigurationParser;

    public static ConfigRootElement parseXmlConfiguration(Reader xmlReader, MappingContext mappingContext) {
        try {
            if (xmlConfigurationParser == null) {
                xmlConfigurationParser = new Xmappr(ConfigRootElement.class);
                xmlConfigurationParser.addConverter(new EmptyStringConverter());
            }
            ConfigRootElement configRootElement = (ConfigRootElement) xmlConfigurationParser.fromXML(xmlReader);
            saveConfigRootElement(configRootElement, mappingContext);
            return configRootElement;
        } catch (XmapprException xe) {
            throw new XmapprConfigurationException("Error: XML configuration could not be processed: " + xe);
        }
    }

    public static ConfigElement processClassTree(Class elementClass, MappingContext mappingContext) {
        Map<Class, ConfigElement> classCache = new HashMap<Class, ConfigElement>();

        // ConfigElement at the top of the hierarchy of ConfigElements
        ConfigElement topConfigElement = new ConfigElement();
        topConfigElement.targetType = elementClass;
        topConfigElement.fromAnnotation = true;
        processNextClass(elementClass, topConfigElement, mappingContext, classCache);

        // process elements
        saveConfigElement(elementClass, topConfigElement, mappingContext);

        return topConfigElement;
    }

    private static void processNextClass(Class elementClass, ConfigElement configElement,
                                         MappingContext mappingContext, Map<Class, ConfigElement> classCache) {

        classCache.put(elementClass, configElement);

        configElement.attribute = readAttributeAnnotations(elementClass);
        configElement.text = readTextAnnotations(elementClass);

        configElement.element = readElementAnnotations(elementClass, mappingContext, classCache);

        configElement.namespace = readNamespaceAnnotations((Namespaces) elementClass.getAnnotation(Namespaces.class));

    }

    public static void saveConfigRootElement(ConfigRootElement configRootElement, MappingContext mappingContext) {

        // Save processed configuration
        mappingContext.addConfigElement(configRootElement.classType, configRootElement);

        if (configRootElement.element != null) {
            for (ConfigElement nextElement : configRootElement.element) {
                ConfigurationProcessor.saveConfigElement(configRootElement.classType, nextElement, mappingContext);
            }
        }
    }

    public static void saveConfigElement(Class elementClass, ConfigElement configElement, MappingContext mappingContext) {


        // next class - derive it from targetType or field type
        Class nextClass = (configElement.targetType == null || configElement.targetType.equals(Object.class))
                ? configElement.baseType : configElement.targetType;

        validateConfigElement(configElement, nextClass);

        if (!mappingContext.configElementExists(nextClass)) {

            // save processed configuration
            mappingContext.addConfigElement(nextClass, configElement);

            if (configElement.element != null) {
                for (ConfigElement nextElement : configElement.element) {
                    // process further
                    saveConfigElement(nextClass, nextElement, mappingContext);
                }
            }
        }
    }

    public static ConfigRootElement readRootElementAnnotations(Class<? extends RootElement> rootClass, MappingContext mappingContext) {

        ConfigRootElement rootConfElement = new ConfigRootElement();

        RootElement rootAnnotation = rootClass.getAnnotation(RootElement.class);

        String rootName = rootAnnotation.value().length() != 0 ? rootAnnotation.value()
                : (rootAnnotation.name().length() != 0 ?
                rootAnnotation.name() : rootClass.getSimpleName().toLowerCase());

        rootConfElement.namespace = readNamespaceAnnotations((Namespaces) rootClass.getAnnotation(Namespaces.class));
        rootConfElement.name = rootName;
        rootConfElement.classType = rootClass;
        rootConfElement.converter = rootAnnotation.converter();
        rootConfElement.attribute = readAttributeAnnotations(rootClass);
        rootConfElement.text = readTextAnnotations(rootClass);

        Map<Class, ConfigElement> classCache = new HashMap<Class, ConfigElement>();
        rootConfElement.element = readElementAnnotations(rootClass, mappingContext, classCache);

        // passes the element configuration
        saveConfigRootElement(rootConfElement, mappingContext);

        return rootConfElement;
    }

    private static void validateConfigElement(ConfigElement configElement, Class elementClass) {

        // no need to validate if element was derived from Annotations
        if (configElement.fromAnnotation) {
            return;
        }
//        System.out.println("______________________________");
//        System.out.println(configElement.toString(" "));

        if (configElement.attribute != null)
            for (ConfigAttribute configAttribute : configElement.attribute) {
                validateConfigAttribute(configAttribute, elementClass);
            }

        if (configElement.text != null) {
            validateConfigText(configElement.text, elementClass);
        }
    }

    private static void validateConfigText(ConfigText text, Class elementClass) {

        // target is not defined
        if (text.field == null && text.getter == null && text.setter == null) {
            throw new XmapprConfigurationException("Error: Unknown mapping target: neither 'field' nor " +
                    "'getter' and/or 'setter' attributes are defined. Mapping must define ONE target: either 'field' or " +
                    "'getter' and/or 'setter' attributes." +
                    "\nOffending mapping:\n\n" + text);
        }
        if (text.field != null && (text.getter != null || text.setter != null)) {
            throw new XmapprConfigurationException("Error: Ambiguous mapping target: both 'field' and one of " +
                    "'getter' or 'setter' attributes defined. Mapping must define only ONE target: either 'field' or " +
                    " 'getter' and/or 'setter' attributes." +
                    "\nOffending mapping:\n\n" + text);
        }

        // field defined
        if (text.field != null) {
            text.targetField = findField(elementClass, text.field);
            text.baseType = text.targetField.getType();
        } else {
            // when using getter or setter, a targetType must be defined
            if (text.targetType == null || Object.class.equals(text.targetType)) {
                throw new XmapprConfigurationException("Error: No target type defined. When using getter or setter" +
                        " method, a 'targetType' attribute must be defined" +
                        "\nOffending mapping:\n\n" + text);
            }
            text.baseType = text.targetType;

            // getter name defined
            if (text.getterMethod == null && text.getter != null) {
                try {
                    // find a getter method
                    Method getterMethod = elementClass.getMethod(text.getter, Void.TYPE);
                    if (!getterMethod.getReturnType().equals(text.targetType))
                        throw new NoSuchMethodException();
                    text.getterMethod = getterMethod;
                } catch (NoSuchMethodException e) {
                    throw new XmapprConfigurationException("Error: Getter method not found in class " +
                            elementClass.getName() +
                            " No method matches " + text.getter +
                            "() with return type " + text.targetType.getName() + "." +
                            "\nOffending mapping:\n\n" + text);
                }
            } else if (text.setterMethod == null && text.setter != null) {
                try {
                    // find a setter method
                    text.setterMethod = elementClass.getMethod(text.setter, text.targetType);
                } catch (NoSuchMethodException e) {
                    throw new XmapprConfigurationException("Error: Setter method not found in class " +
                            elementClass.getName() +
                            " No method matches: " + text.setter +
                            "(" + text.targetType.getName() + ") with return type void." +
                            "\nOffending mapping:\n\n" + text);
                }
            }
        }

    }

    private static void validateConfigAttribute(ConfigAttribute attribute, Class elementClass) {

        // target is not defined - try to find a field from attribute name
        if (attribute.field == null && attribute.getter == null && attribute.setter == null) {
            if (findField(elementClass, attribute.name) != null) {
                attribute.field = attribute.name;
            }
        }

        if (attribute.field != null && (attribute.getter != null || attribute.setter != null)) {
            throw new XmapprConfigurationException("Error: Ambiguous mapping target: both 'field' and one of " +
                    "'getter' or 'setter' attributes defined. Mapping must define only ONE target: either 'field' or " +
                    "one of 'getter' or 'setter' attributes." +
                    "\nOffending mapping:\n\n" + attribute);
        }

        // field defined
        if (attribute.field != null) {
            attribute.targetField = findField(elementClass, attribute.field);
            attribute.baseType = attribute.targetField.getType();
        } else {

            // when using getter or setter, a targetType must be defined
            if (attribute.targetType == null || Object.class.equals(attribute.targetType)) {
                throw new XmapprConfigurationException("Error: No target type defined. When using getter or setter" +
                        " method, a 'targetType' attribute must be defined" +
                        "\nOffending mapping:\n\n" + attribute);
            }
            attribute.baseType = attribute.targetType;

            // getter name defined
            if (attribute.getterMethod == null && attribute.getter != null) {
                try {
                    // find a getter method
                    Method getterMethod = elementClass.getMethod(attribute.getter, Void.TYPE);
                    if (!getterMethod.getReturnType().equals(attribute.targetType))
                        throw new NoSuchMethodException();
                    attribute.getterMethod = getterMethod;
                } catch (NoSuchMethodException e) {
                    throw new XmapprConfigurationException("Error: Getter method not found in class " +
                            elementClass.getName() +
                            " No method matches " + attribute.getter +
                            "() with return type " + attribute.targetType.getName() + "." +
                            "\nOffending mapping:\n\n" + attribute);
                }
            } else if (attribute.setterMethod == null && attribute.setter != null) {
                try {
                    // find a setter method
                    attribute.setterMethod = elementClass.getMethod(attribute.setter, attribute.targetType);
                } catch (NoSuchMethodException e) {
                    throw new XmapprConfigurationException("Error: Setter method not found in class " +
                            elementClass.getName() +
                            " No method matches: " + attribute.setter +
                            "(" + attribute.targetType.getName() + ") with return type void." +
                            "\nOffending mapping:\n\n" + attribute);
                }
            }
        }

    }


    private static List<ConfigElement> readElementAnnotations(Class elementClass, MappingContext mappingContext,
                                                              Map<Class, ConfigElement> classCache) {

        // Getters & setters come in pairs which produce one ConfigAttribute.
        // Cache ConfigAttributes until all methods are processed.
        Map<String, ConfigElement> confElementCache = null;

        for (Field field : elementClass.getFields()) {

            Element[] annotations = getElementAnnotations(field);

            // are there any @Attribute annotations on this field
            for (Element annotation : annotations) {

                // when @Attribute is defined on field, the base type is the type of field
                Class baseType = field.getType();

                String elementName = annotation.value().length() != 0 ? annotation.value()
                        : (annotation.name().length() != 0 ? annotation.name() : field.getName());

                ConfigElement element = new ConfigElement(
                        true,
                        elementName,
                        baseType,
                        field,
                        field.getName(),
                        null,
                        null,
                        null,
                        null,
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

                if (confElementCache == null) {
                    confElementCache = new HashMap<String, ConfigElement>();
                }
                confElementCache.put(elementName, element);
            }
        }

        // scan class methods for @Element annotations
        for (Method method : elementClass.getMethods()) {

            // process all @Element annotations of given method
            for (Element annotation : getElementAnnotations(method)) {

                String elementName = null;
                Class baseType, targetType = annotation.targetType();
                Method getter = null, setter = null;

                // Check if this is a getter or setter
                if (hasGetterFormat(method, annotation.targetType())) {

                    // define the getter
                    getter = method;

                    // Try getting XML element name from @Element annotation.
                    // If not defined, use name derived from setter method name
                    elementName = annotation.value().length() != 0 ? annotation.value()
                            : (annotation.name().length() != 0 ? annotation.name()
                            : getGetterName(method));

                    // base type is defined by getter's return type
                    baseType = method.getReturnType();

                } else if (hasSetterFormat(method, annotation.targetType())) {

                    // define the setter
                    setter = method;

                    // Try getting XML element name from @Element annotation.
                    // If not defined, use name derived from getter method name
                    elementName = annotation.value().length() != 0 ? annotation.value()
                            : (annotation.name().length() != 0 ? annotation.name()
                            : getSetterName(method));

                    // base type is defined by setter's argument type
                    baseType = method.getParameterTypes()[0];

                } else {
                    throw new XmapprConfigurationException("Error: @Element annotation on " +
                            "method " + method.getName() + " in class " + elementClass.getName()
                            + " does not seem to be an appropriate field accessor method. "
                            + "Getter method must return " + annotation.targetType() + " and take no arguments."
                            + "Setter method must return void and take one argument of type " + annotation.targetType());
                }

                // XML element name could not be derived from method name, nor from @Element annotation
                if (elementName == null) {
                    throw new XmapprConfigurationException("Error: Could not find XML element name to map to. " +
                            "Either @Element annotation must have a 'name' attribute defined or accessor method " +
                            "must follow name convention for getters/setters (i.e. it must be of getXY or setXY form).");
                }

                if (annotation.targetType() == null || annotation.targetType().equals(Object.class)) {
                    targetType = baseType;
                }

                if (confElementCache == null) {
                    confElementCache = new HashMap<String, ConfigElement>();
                }

                ConfigElement element = confElementCache.get(elementName);
                if (element != null) {
                    if (element.field != null) {
                        // todo ERROR - XML element defined via both field and method
                    }
                    element.baseType = baseType;
                    element.getterMethod = getter == null ? element.getterMethod : getter;
                    element.setterMethod = setter == null ? element.setterMethod : setter;
                    element.defaultvalue = annotation.defaultValue();
                    element.targetType = targetType;
                    element.format = annotation.format();
                    element.converter = annotation.converter();
                } else {
                    element = new ConfigElement(
                            true,
                            elementName,
                            baseType,
                            null,
                            null,
                            getter,
                            (getter == null) ? null : getter.getName(),
                            setter,
                            (setter == null) ? null : setter.getName(),
                            annotation.defaultValue(),
                            annotation.targetType(),
                            annotation.format(),
                            annotation.converter()
                    );
                    confElementCache.put(elementName, element);
                }
            }
        }

        if (confElementCache != null) {
            return new ArrayList<ConfigElement>(confElementCache.values());
        } else {
            return null;
        }
    }

    private static List<ConfigAttribute> readAttributeAnnotations(Class elementClass) {

        // Getters & setters come in pairs which produce one ConfigAttribute.
        // Cache ConfigAttributes until all methods are processed.
        Map<String, ConfigAttribute> confAttributeCache = null;

        // process all @Attribute annotations of given field
        for (Field field : elementClass.getFields()) {

            // scan class methods for @Attribute annotations
            for (Attribute annotation : getAttributeAnnotations(field)) {

                // when @Attribute is defined on field, the base type is the type of field
                Class baseType = field.getType();

                // Try getting XML element name from @Attribute annotation.
                // If not defined, use the name of the field
                String attributeName = annotation.value().length() != 0 ? annotation.value()
                        : (annotation.name().length() != 0 ? annotation.name() : field.getName());

//                // if targetType annotation is not defined, use type of the field
//                Class targetType = (annotation.targetType().equals(Object.class))
//                        ? field.getType() : annotation.targetType();

                ConfigAttribute attribute = new ConfigAttribute(
                        attributeName,
                        baseType,
                        field,
                        field.getName(),
                        null,
                        null,
                        null,
                        null,
                        annotation.defaultValue(),
                        annotation.targetType(),
                        annotation.format(),
                        annotation.converter()
                );

                if (confAttributeCache == null) {
                    confAttributeCache = new HashMap<String, ConfigAttribute>();
                }

                // error - duplicate attribute names
                if (confAttributeCache.containsKey(attributeName)) {
                    throw new XmapprConfigurationException("Error: @Attribute annotation on "
                            + "field " + field.getName() + " in class " + elementClass.getName()
                            + " contains a duplicate XML attribute name: " + attributeName
                            + " @Attribute annotations must map to unique XML attributes.");
                }

                confAttributeCache.put(attributeName, attribute);
            }
        }

        // scan class methods for @Attribute annotations
        for (Method method : elementClass.getMethods()) {

            // process all @Attribute annotations of given method
            for (Attribute annotation : getAttributeAnnotations(method)) {

                String attributeName;
                Class baseType, targetType = annotation.targetType();
                Method getter = null, setter = null;

                // Check if this is a getter or setter
                if (hasGetterFormat(method, annotation.targetType())) {

                    // define the getter
                    getter = method;

                    // Try getting XML element name from @Attribute annotation.
                    // If not defined, use name derived from setter method name
                    attributeName = annotation.value().length() != 0 ? annotation.value()
                            : (annotation.name().length() != 0 ? annotation.name()
                            : getGetterName(method));

                    // base type is defined by getter's return type
                    baseType = method.getReturnType();

                } else if (hasSetterFormat(method, annotation.targetType())) {

                    // define the setter
                    setter = method;

                    // Try getting XML element name from @Attribute annotation.
                    // If not defined, use name derived from getter method name
                    attributeName = annotation.value().length() != 0 ? annotation.value()
                            : (annotation.name().length() != 0 ? annotation.name()
                            : getSetterName(method));

                    // base type is defined by setter's argument type
                    baseType = method.getParameterTypes()[0];

                } else {
                    throw new XmapprConfigurationException("Error: @Attribute annotation on " +
                            "method " + method.getName() + " in class " + elementClass.getName()
                            + " does not seem to be an appropriate field accessor method. "
                            + "Getter method must return " + annotation.targetType() + " and take no arguments."
                            + "Setter method must return void and take one argument of type " + annotation.targetType());
                }

                if (annotation.targetType() == null || annotation.targetType().equals(Object.class)) {
                    targetType = baseType;
                }

                // XML element name could not be derived from method name, nor from @Attribute annotation
                if (attributeName == null) {
                    throw new XmapprConfigurationException("Error: Could not find XML attribute name to map to. " +
                            "Either @Attribute annotation must have a 'name' attribute defined or accessor method " +
                            "must follow name convention for getters/setters (i.e. it must be of getXY or setXY form).");
                }

                if (confAttributeCache == null) {
                    confAttributeCache = new HashMap<String, ConfigAttribute>();
                }

                ConfigAttribute attribute = confAttributeCache.get(attributeName);
                if (attribute != null) {
                    if (attribute.field != null) {
                        // todo XML attribute defined via both - ERROR
                    }
                    attribute.baseType = baseType;
                    attribute.getterMethod = getter == null ? attribute.getterMethod : getter;
                    attribute.setterMethod = setter == null ? attribute.setterMethod : setter;
                    attribute.getter = getter == null ? attribute.getter : getter.getName();
                    attribute.setter = setter == null ? attribute.setter : setter.getName();
                    attribute.defaultvalue = annotation.defaultValue();
                    attribute.targetType = targetType;
                    attribute.format = annotation.format();
                    attribute.converter = annotation.converter();
                } else {
                    attribute = new ConfigAttribute(
                            attributeName,
                            baseType,
                            null,
                            null,
                            getter,
                            (getter == null) ? null : getter.getName(),
                            setter,
                            (setter == null) ? null : setter.getName(),
                            annotation.defaultValue(),
                            targetType,
                            annotation.format(),
                            annotation.converter()
                    );
                    confAttributeCache.put(attributeName, attribute);
                }
            }
        }

        if (confAttributeCache != null) {
            return new ArrayList<ConfigAttribute>(confAttributeCache.values());
        } else {
            return null;
        }
    }

    private static boolean hasGetterFormat(Method method, Class targetType) {
        if (targetType == null || targetType.equals(Object.class)) {
            return method.getParameterTypes().length == 0;
        }
        return targetType.isAssignableFrom(method.getReturnType())
                && method.getParameterTypes().length == 0;
    }

    private static boolean hasSetterFormat(Method method, Class targetType) {
        if (targetType == null || targetType.equals(Object.class)) {
            return method.getReturnType().equals(Void.TYPE)
                    && method.getParameterTypes().length == 1;
        }
        return method.getReturnType().equals(Void.TYPE)
                && method.getParameterTypes().length == 1
                && method.getParameterTypes()[0].isAssignableFrom(targetType);
    }

    private static ConfigText readTextAnnotations(Class elementClass) {

        int found = 0;
        Text fieldAnnotation = null;
        Field targetField = null;
        
        for (Field field : elementClass.getFields()) {
            if (field.getAnnotation(Text.class) != null) {
                found++;
                fieldAnnotation = field.getAnnotation(Text.class);
                targetField = field;
            }
        }
        if (found > 1) {
            throw new XmapprConfigurationException("Error: Multiple @Text annotations in class "
                    + elementClass.getName() + ". Max one @Text fieldAnnotation can be present in a class.");
        }

        ConfigText configText = null;
        if (found == 1) {

            // if targetType annotation is not defined, use type of the field
            Class targetType = (fieldAnnotation.targetType().equals(Object.class))
                    ? targetField.getType() : fieldAnnotation.targetType();

            configText = new ConfigText(
                    targetField.getType(),
                    targetField,
                    targetField.getName(),
                    null,
                    null,
                    null,
                    null,
                    targetType,
                    fieldAnnotation.format(),
                    fieldAnnotation.converter());
        }

        // continue with searching @Text annotations on methods
        for (Method method : elementClass.getMethods()) {

            Text annotation = method.getAnnotation(Text.class);
            Class baseType;
            Method getter = null, setter = null;

            if (annotation != null) {

//                // check for targetType
//                if (fieldAnnotation.targetType().getClass().equals(Object.class)) {
//
//                }

                // Check if this is a getter or setter
                if (hasGetterFormat(method, annotation.targetType())) {

                    // define the getter
                    getter = method;

                    // base type is defined by getter's return type
                    baseType = getter.getReturnType();

                } else if (hasSetterFormat(method, annotation.targetType())) {

                    // define the setter
                    setter = method;

                    // base type is defined by setter's argument type
                    baseType = setter.getParameterTypes()[0];

                } else {
                    throw new XmapprConfigurationException("Error: @Text annotation on " +
                            "method " + method.getName() + " in class " + elementClass.getName()
                            + " does not seem to be an appropriate field accessor method. "
                            + "Getter method must return " + annotation.targetType() + " and take no arguments."
                            + "Setter method must return void and take one argument of type " + annotation.targetType());
                }

                if (configText == null) {
                    configText = new ConfigText(
                            baseType,
                            null,
                            null,
                            getter,
                            (getter == null) ? null : getter.getName(),
                            setter,
                            (setter == null) ? null : setter.getName(),
                            annotation.targetType(),
                            annotation.format(),
                            annotation.converter());
                } else {
                    // double mapping - @Text defined on both field and method
                    if (configText.targetField != null) {
                        throw new XmapprConfigurationException("Error: Duplicate use of @Text annotation. "
                                + "Method " + method.getName() + " and field " + configText.targetField.getName()
                                + " in class " + elementClass.getName() + " both contain @Text annotation. "
                                + "@Text can only be used on one field or on a pair of accessor methods (getter/setter).");

                    } else if (configText.setterMethod != null) {
                        // setter can not be defined twice
                        if (setter != null) {
                            throw new XmapprConfigurationException("Error: Duplicate use of @Text annotation."
                                    + "Methods " + setter.getName() + " and " + configText.setterMethod.getName()
                                    + " in class " + elementClass.getName() + " both appear to be setters and both "
                                    + "contain @Text annotation. @Text can only be used on one getter and/or one setter.");
                        }
                        // getter and setter do not have compatible types
                        if (!getter.getReturnType().equals(configText.setterMethod.getParameterTypes()[0])) {
                            throw new XmapprConfigurationException("Error: Incompatible getter and setter types."
                                    + "@Text annotations in class " + elementClass.getName()
                                    + " define getter method " + getter.getName()
                                    + " and setter method " + configText.setterMethod.getName()
                                    + ". Return type of getter and first argument of setter must be of the same type.");
                        }
                        configText.getterMethod = getter;
                        configText.getter = getter.getName();
                        if (annotation.format().length() != 0)
                            configText.format = annotation.format();
                        if (!annotation.converter().equals(ValueConverter.class))
                            configText.converter = annotation.converter();
                        if (!annotation.targetType().equals(Object.class))
                            configText.targetType = annotation.targetType();

                    } else if (configText.getterMethod != null) {
                        // getter can not be defined twice
                        if (getter != null) {
                            throw new XmapprConfigurationException("Error: Duplicate use of @Text annotation."
                                    + "Methods " + getter.getName() + " and " + configText.getterMethod.getName()
                                    + " in class " + elementClass.getName() + " both appear to be getters and both "
                                    + "contain @Text annotation. @Text can only be used on one getter and/or one setter.");
                        }
                        // getter and setter do not have compatible types
                        if (!configText.getterMethod.getReturnType().equals(setter.getParameterTypes()[0])) {
                            throw new XmapprConfigurationException("Error: Incompatible getter and setter types."
                                    + "@Text annotations in class " + elementClass.getName()
                                    + " define getter method " + configText.getterMethod.getName()
                                    + " and setter method " + setter.getName()
                                    + ". Return type of getter and first argument of setter must be of the same type.");

                        }
                        configText.setterMethod = setter;
                        configText.setter = setter.getName();
                        if (annotation.format().length() != 0)
                            configText.format = annotation.format();
                        if (!annotation.converter().equals(ValueConverter.class))
                            configText.converter = annotation.converter();
                        if (!annotation.targetType().equals(Object.class))
                            configText.targetType = annotation.targetType();
                    }
                }

            }
        }

        return configText;
    }

    private static Element[] getElementAnnotations(Field field) {
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
        return annotations;
    }

    private static Element[] getElementAnnotations(Method method) {
        // collect all @Attribute annotations in a single array for easier processing
        Element[] annotations = new Element[0];
        Elements multiAnno = method.getAnnotation(Elements.class);
        if (multiAnno != null && multiAnno.value().length != 0) {
            annotations = multiAnno.value();
        }
        Element singleAnno = method.getAnnotation(Element.class);
        if (singleAnno != null) {
            Element[] copy = new Element[annotations.length + 1];
            System.arraycopy(annotations, 0, copy, 0, annotations.length);
            annotations = copy;
            annotations[annotations.length - 1] = singleAnno;
        }
        return annotations;
    }

    private static Attribute[] getAttributeAnnotations(Method method) {
        // collect all @Attribute annotations in a single array for easier processing
        Attribute[] annotations = new Attribute[0];
        Attributes multiAnno = method.getAnnotation(Attributes.class);
        if (multiAnno != null && multiAnno.value().length != 0) {
            annotations = multiAnno.value();
        }
        Attribute singleAnno = method.getAnnotation(Attribute.class);
        if (singleAnno != null) {
            Attribute[] copy = new Attribute[annotations.length + 1];
            System.arraycopy(annotations, 0, copy, 0, annotations.length);
            annotations = copy;
            annotations[annotations.length - 1] = singleAnno;
        }
        return annotations;
    }

    private static Attribute[] getAttributeAnnotations(Field field) {
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
        return annotations;
    }

    /**
     * Checks that given method has a setter form: starts with 'get' and then uppercase char.
     *
     * @param method
     * @return Name of the field the getter method is associated to.
     *         Null if the getter method does not follow naming conventions.
     */
    public static String getSetterName(Method method) {

        String methodName = method.getName();
        // starts with 'set' then an uppercase char
        if (methodName.startsWith("set") && Character.isUpperCase(methodName.charAt(3))) {
            // loose the 'set' and lowercase next char
            return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
        }
        return null;
    }


    /**
     * Checks that given method has a getter form: starts with 'get' and then uppercase char.
     *
     * @param method
     * @return Name of the field the getter method is associated to.
     *         Null if the getter method does not follow naming conventions.
     */
    public static String getGetterName(Method method) {

        String methodName = method.getName();
        // starts with 'get' then an uppercase char
        if (methodName.startsWith("get") && Character.isUpperCase(methodName.charAt(3))) {
            // loose the 'get' and lowercase next char
            return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
        }
        return null;
    }

    private static Field findField(Class clazz, String fieldName) {

        // only process real classes
        if (clazz.isPrimitive() || fieldName == null) {
            return null;
        }

        try {
            return clazz.getField(fieldName);
        } catch (NoSuchFieldException e) {
            // no field with given name was found
            return null;
        }
    }

    private static List<ConfigNamespace> readNamespaceAnnotations(Namespaces nsAnnotation) {
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
