package org.xmappr;

import org.xmappr.converters.*;

import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class ConfigurationProcessor {

    private static Xmappr xmlConfigurationParser;

    public static ConfigRootElement parseXmlConfiguration(Reader xmlReader, MappingContext mappingContext) {
        try {
            if (xmlConfigurationParser == null) {
                xmlConfigurationParser = new Xmappr(ConfigRootElement.class);
                xmlConfigurationParser.addConverter(new EmptyStringConverter());
            }
            ConfigRootElement configRootElement = (ConfigRootElement) xmlConfigurationParser.fromXML(xmlReader);
            XmlConfigurationValidator.validateConfigRootElement(configRootElement);
            return configRootElement;
        } catch (XmapprException xe) {
            throw new XmapprConfigurationException("Error: XML configuration could not be processed: " + xe);
        }
    }

    public static ConfigElement processClassTree(Class elementClass, MappingContext mappingContext) {
        Map<String, ConfigElement> classCache = new HashMap<String, ConfigElement>();

        // ConfigElement at the top of the hierarchy of ConfigElements
        ConfigElement topConfigElement = new ConfigElement();
        topConfigElement.targetType = elementClass;
        topConfigElement.fromAnnotation = true;
        processNextClass(elementClass, topConfigElement, mappingContext, classCache);

        // process elements
        XmlConfigurationValidator.validateConfigElement(topConfigElement, elementClass);

        return topConfigElement;
    }

    private static void processNextClass(Class elementClass, ConfigElement configElement,
                                         MappingContext mappingContext, Map<String, ConfigElement> classCache) {

//        classCache.put(elementClass, configElement);

        configElement.attribute = readAttributeAnnotations(elementClass);
        configElement.text = readTextAnnotations(elementClass);

        configElement.element = readElementAnnotations(elementClass, mappingContext, classCache);

//        final Namespaces nsAnno = (Namespaces) elementClass.getAnnotation(Namespaces.class);
//        configElement.namespace = readNamespaceAnnotations(nsAnno);

    }

//    public static void saveConfigRootElement(ConfigRootElement configRootElement, MappingContext mappingContext) {
//
//        XmlConfigurationValidator.validateConfigRootElement(configRootElement);
//
//        // Save processed configuration
//        mappingContext.addConfigRootElement(configRootElement.classType, configRootElement);
//
//        if (configRootElement.element != null) {
//            for (ConfigElement nextElement : configRootElement.element) {
//                saveConfigElement(configRootElement.classType, nextElement, mappingContext);
//            }
//        }
//    }
//
//    public static void saveConfigElement(Class parentClass, ConfigElement configElement, MappingContext mappingContext) {
//
//        Class nextClass;
//
//        if (configElement.fromAnnotation) {
//            nextClass = (configElement.targetType != null) && !configElement.targetType.equals(Object.class)
//                    ? configElement.targetType : configElement.accessorType;
//        } else {
//            nextClass = XmlConfigurationValidator.validateConfigElement(configElement, parentClass);
//        }
//
//        if (!mappingContext.configElementExists(nextClass)) {
//
//            // save processed configuration
//            mappingContext.addConfigElement(nextClass, configElement);
//
//            if (configElement.element != null) {
//                for (ConfigElement nextElement : configElement.element) {
//                    // process further
//                    saveConfigElement(nextClass, nextElement, mappingContext);
//                }
//            }
//        }
//    }


    public static ConfigRootElement readRootElementAnnotations(Class<? extends RootElement> rootClass, MappingContext mappingContext) {

        ConfigRootElement rootConfElement = new ConfigRootElement();

        RootElement rootAnnotation = rootClass.getAnnotation(RootElement.class);

        String rootName = rootAnnotation.value().length() != 0 ? rootAnnotation.value()
                : (rootAnnotation.name().length() != 0 ?
                rootAnnotation.name() : rootClass.getSimpleName().toLowerCase());

        rootConfElement.namespace = readNamespaceAnnotations((Namespaces) rootClass.getAnnotation(Namespaces.class));
        rootConfElement.fromAnnotation = true;
        rootConfElement.name = rootName;
        rootConfElement.classType = rootClass;
        rootConfElement.converter = rootAnnotation.converter();
        rootConfElement.attribute = readAttributeAnnotations(rootClass);
        rootConfElement.text = readTextAnnotations(rootClass);

        Map<String, ConfigElement> classCache = new HashMap<String, ConfigElement>();
        rootConfElement.element = readElementAnnotations(rootClass, mappingContext, classCache);

        // passes the element configuration
        XmlConfigurationValidator.validateConfigRootElement(rootConfElement);

        return rootConfElement;
    }


    private static List<ConfigElement> readElementAnnotations(Class elementClass, MappingContext mappingContext,
                                                              Map<String, ConfigElement> classCache) {

        // Getters & setters come in pairs which produce one ConfigAttribute.
        // Cache ConfigAttributes until all methods are processed.
        Map<String, ConfigElement> confElementCache = null;

        for (Field field : elementClass.getFields()) {

            // aggregate all @Element annotation on this field into an array
            Element[] annotations = getElementAnnotations(field);

            List<ConfigNamespace> nsList = readNamespaceAnnotations((Namespaces) field.getAnnotation(Namespaces.class));

            // are there any @Element annotations on this field
            for (Element annotation : annotations) {

                // Rule 1.: accessorType is type of field
                Class accessorType = field.getType();

                // XML element name is derived from annotation or from field name
                String elementName = annotation.value().length() != 0 ? annotation.value()
                        : (annotation.name().length() != 0 ? annotation.name() : field.getName());

                // accessorType is a Collection
                boolean isCollection = Collection.class.isAssignableFrom(field.getType());

                // Rule 2.: converterType equals accessorType, unless accessorType is a Collection,
                // then converterType equals Collection's parametrized type.
                Class converterType = isCollection ?  // accessor is a Collection?
                        getParameterizedCollectionType(field.getGenericType()) :  // get it's parametrized type
                        accessorType;

                // look if custom converter defined
                Class<? extends Converter> customConverter = annotation.converter().equals(ElementConverter.class)
                        ? null : annotation.converter();

                // Rule 3.: if name is a wildcard mapping ("*") then converter is DomElementConverter, unless
                // custom converter is already defined by user
                if (elementName.equals("*") && customConverter == null) {
                    customConverter = DomElementConverter.class;
                }

                // retrieve targetType
                Class targetType = annotation.targetType().equals(Object.class) ? null : annotation.targetType();

                // Rule 4.: if custom converter is not defined then converterType is obligatory
                if (customConverter == null && converterType == null && targetType == null) {
                    throw new XmapprConfigurationException("Error: Converter type could not be inferred. " +
                            "@Element annotation is used on " +
                            "field " + field.getName() + " (class " + elementClass.getName() +
                            ") is of  " + field.getGenericType() + " type. " +
                            "@Element annotation defined on fields of java.util.Collection type must have " +
                            "either 'targetType' attribute defined or Collection type must be a " +
                            "parametrized generic type (e.g. List<String>).");
                }

                ConfigElement element = new ConfigElement(
                        true,
                        elementName,
                        accessorType,
                        isCollection,
                        converterType,
                        field,
                        field.getName(),
                        null,
                        null,
                        null,
                        null,
                        "".equals(annotation.defaultValue()) ? null : annotation.defaultValue(),
                        targetType,
                        annotation.format(),
                        customConverter,
                        nsList
                );

                // initialize cache if needed
                if (confElementCache == null) {
                    confElementCache = new HashMap<String, ConfigElement>();
                }

                // error - duplicate element names
                if (confElementCache.containsKey(elementName)) {
                    throw new XmapprConfigurationException("Error: @Element annotation on "
                            + "field " + field.getName() + " in class " + elementClass.getName()
                            + " contains a duplicate XML element name: " + elementName
                            + " @Element annotations must map to unique XML elements.");
                }

                confElementCache.put(elementName, element);
            }
        }

        // scan class methods for @Element annotations
        for (Method method : elementClass.getMethods()) {

            List<ConfigNamespace> nsList = readNamespaceAnnotations((Namespaces) method.getAnnotation(Namespaces.class));

            // process all @Element annotations of given method
            for (Element annotation : getElementAnnotations(method)) {

                String elementName = null;
                Class accessorType, converterType;
                Method getterMethod = null, setterMethod = null;

                // retrieve targetType
                Class targetType = annotation.targetType().equals(Object.class) ? null : annotation.targetType();

                // accessorType is a Collection
                boolean isCollection;

                Class getterType = hasGetterFormat(method);
                Class setterType = hasSetterFormat(method);

                // Check if this is a getter or setterMethod
                if (getterType != null) {

                    // define the getter
                    getterMethod = method;

                    // Try getting XML element name from @Element annotation.
                    // If not defined, use name derived from setterMethod method name
                    elementName = annotation.value().length() != 0 ? annotation.value()
                            : (annotation.name().length() != 0 ? annotation.name()
                            : getGetterName(method));

                    // Rule 1.: base type is defined by getter's return type
                    accessorType = getterType;

                    // is getter type a Collection?
                    isCollection = Collection.class.isAssignableFrom(getterType);

                    // Rule 2.: converterType equals accessorType, unless accessorType is a Collection,
                    // then converterType equals Collection's parametrized type.
                    converterType = isCollection ?  // accessor is a Collection?
                            getParameterizedCollectionType(getterMethod.getGenericReturnType()) :  // get it's parametrized type
                            accessorType;

                } else if (setterType != null) {

                    // define the setterMethod
                    setterMethod = method;

                    // Try getting XML element name from @Element annotation.
                    // If not defined, use name derived from getter method name
                    elementName = annotation.value().length() != 0 ? annotation.value()
                            : (annotation.name().length() != 0 ? annotation.name()
                            : getSetterName(method));

                    // Rule 1.: base type is defined by setterMethod's argument type
                    accessorType = setterType;

                    // is setterMethod type a Collection?
                    isCollection = Collection.class.isAssignableFrom(setterType);

                    // Rule 2.: converterType equals accessorType, unless accessorType is a Collection,
                    // then converterType equals Collection's parametrized type.
                    converterType = isCollection ?  // accessor is a Collection?
                            getParameterizedCollectionType(setterMethod.getGenericParameterTypes()[0]) :  // get it's parametrized type
                            accessorType;

                } else {
                    throw new XmapprConfigurationException("Error: @Element annotation on " +
                            "method " + method.getName() + " in class " + elementClass.getName()
                            + " does not seem to be an appropriate field accessor method. "
                            + "Getter method must not return void and take no arguments."
                            + "Setter method must return void and take one argument.");
                }

                // XML element name could not be derived from method name, nor from @Element annotation
                if (elementName == null) {
                    throw new XmapprConfigurationException("Error: Could not find XML element name to map to. " +
                            "Either @Element annotation must have a 'name' attribute defined or accessor method " +
                            "must follow name convention for getters/setters (i.e. it must be of getXY or setXY form).");
                }

                // look if custom converter defined
                Class<? extends Converter> customConverter = annotation.converter().equals(ElementConverter.class)
                        ? null : annotation.converter();

                // Rule 3.: if name is a wildcard mapping ("*") then converter is DomElementConverter, unless
                // custom converter is already defined by user
                if (elementName.equals("*") && customConverter == null) {
                    customConverter = DomElementConverter.class;
                }

                if (confElementCache == null) {
                    confElementCache = new HashMap<String, ConfigElement>();
                }

                ConfigElement element = confElementCache.get(elementName);
                if (element != null) {
                    if (element.field != null) {
                        // todo ERROR - XML element defined via both field and method
                    }
                    element.accessorType = accessorType;
                    element.getterMethod = getterMethod == null ? element.getterMethod : getterMethod;
                    if (getterMethod != null) {
                        element.getter = getterMethod.getName();
                        element.getterMethod = getterMethod;
                    }
                    if (setterMethod != null) {
                        element.setter = setterMethod.getName();
                        element.setterMethod = setterMethod;
                    }
                    element.defaultvalue = "".equals(annotation.defaultValue()) ? null : annotation.defaultValue();
                    element.targetType = targetType;
                    element.format = annotation.format();
                    element.converter = customConverter;
                    if (element.namespace != null && element.namespace.equals(nsList)) {
                        throw new XmapprConfigurationException("Error: Ambiguous namespace mapping. " +
                                "Getter and setter both define @Namespaces with different values. Accessor methods map" +
                                "to the same XML element so the namespaces (if defined on both methods) must be the same.");
                    }
                    element.namespace = nsList;

                } else {
                    element = new ConfigElement(
                            true,
                            elementName,
                            accessorType,
                            isCollection,
                            converterType,
                            null,
                            null,
                            getterMethod,
                            (getterMethod == null) ? null : getterMethod.getName(),
                            setterMethod,
                            (setterMethod == null) ? null : setterMethod.getName(),
                            "".equals(annotation.defaultValue()) ? null : annotation.defaultValue(),
                            targetType,
                            annotation.format(),
                            customConverter,
                            nsList
                    );
                    confElementCache.put(elementName, element);
                }
            }
        }


        if (confElementCache != null) {

            ArrayList<ConfigElement> elements = new ArrayList<ConfigElement>(confElementCache.values());

            boolean isAlreadyWildcardMapping = false;

            // scan all elements to see if further processing is needed
            for (int i = 0; i < elements.size(); i++) {
                ConfigElement element = elements.get(i);

                if (element.name.equals("*")) {
                    // double use of wildcard mapping @Element("*") within a single class
                    if (isAlreadyWildcardMapping) {
                        throw new XmapprConfigurationException("Error: Incorrect use of Element(\"*\")" +
                                " in class " + elementClass.getName() +
                                "Wildcard name mapping @Element(\"*\") can be used only one time within a class");
                    }
                    isAlreadyWildcardMapping = true;
                }

                // if custom converter is not defined then we need to look for more configuration
                if (element.converter == null) {

                    // Which class to process next?
                    // If targetType is not defined then use converterType.
                    Class nextClass = (element.targetType != null) ? element.targetType : element.converterType;

                    // unique element name for storing into element cache
                    String uniqueElementName = elementClass.getName() + "$" + element.name;

                    // Process next class if it's converter is not yet defined
                    if (!mappingContext.isElementConverterDefined(nextClass)) {

                        // Was this class already processed?
                        // This eliminates loops.
                        if (classCache.containsKey(uniqueElementName)) {

                            // replace with an existing
                            elements.set(i, classCache.get(uniqueElementName));
                        } else {

                            // recursive call to process next class
                            classCache.put(uniqueElementName, element);
                            processNextClass(nextClass, element, mappingContext, classCache);
                        }
                    }
                }
            }
            return elements;
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

//                // when @Attribute is defined on field, the base type is the type of field
//                Class accessorType = field.getType();
//
//
////                // if targetType annotation is not defined, use type of the field
////                Class targetType = (annotation.targetType().equals(Object.class))
////                        ? field.getType() : annotation.targetType();

                // Try getting XML attribute name from @Attribute annotation.
                // If not defined, use the name of the field
                String attributeName = annotation.value().length() != 0 ? annotation.value()
                        : (annotation.name().length() != 0 ? annotation.name() : field.getName());

                // Rule 1.: accessorType is type of field
                Class accessorType = field.getType();

                // XML element name is derived from annotation or from field name
                String elementName = annotation.value().length() != 0 ? annotation.value()
                        : (annotation.name().length() != 0 ? annotation.name() : field.getName());

                // accessorType is a Collection
                boolean isMap = isMapType(field.getType());

                // Rule 2.: converterType equals accessorType, unless accessorType is a Collection,
                // then converterType equals Collection's parametrized type.
                Class converterType = isMap ?  // accessor is a Collection?
                        getParameterizedMapType(field.getGenericType()) :  // get it's parametrized type
                        accessorType;

                // look if custom converter defined
                Class<? extends ValueConverter> customConverter = annotation.converter().equals(ValueConverter.class)
                        ? null : annotation.converter();

                // Rule 3.: if name is a wildcard mapping ("*") then converter is DomElementConverter, unless
                // custom converter is already defined by user
                if (elementName.equals("*") && customConverter == null) {
                    customConverter = StringConverter.class;
                }

                // retrieve targetType
                Class targetType = annotation.targetType().equals(Object.class) ? null : annotation.targetType();

                // Rule 6.: Attributes mapped to Maps fall back to String. Given:
                // 1. it's a Map and
                // 2. custom converter is not defined and
                // 3. converterType could not be inferred and
                // 4. targetType is not defined
                // Then targetType is implicitly set to String
                if (isMap && customConverter == null && converterType == null && targetType == null) {
                    targetType = String.class;
                }

                ConfigAttribute attribute = new ConfigAttribute(
                        attributeName,
                        accessorType,
                        isMap,
                        field,
                        field.getName(),
                        null,
                        null,
                        null,
                        null,
                        "".equals(annotation.defaultValue()) ? null : annotation.defaultValue(),
                        targetType,
                        annotation.format(),
                        customConverter
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

//                String attributeName;
//                Class accessorType, targetType = annotation.targetType();
//                Method getter = null, setter = null;

                String attributeName;
                Class accessorType, converterType;
                Method getterMethod = null, setterMethod = null;

                // retrieve targetType
                Class targetType = annotation.targetType().equals(Object.class) ? null : annotation.targetType();

                // accessorType is a Collection
                boolean isMap;

                Class getterType = hasGetterFormat(method);
                Class setterType = hasSetterFormat(method);

                // Check if this is a getter or setter
                if (getterType != null) {

                    // define the getter
                    getterMethod = method;

                    // Try getting XML element name from @Attribute annotation.
                    // If not defined, use name derived from setter method name
                    attributeName = annotation.value().length() != 0 ? annotation.value()
                            : (annotation.name().length() != 0 ? annotation.name()
                            : getGetterName(method));

                    // base type is defined by getter's return type
                    accessorType = getterType;

                    // is getter type a Collection?
                    isMap = isMapType(getterType);

                    // Rule 2.: converterType equals accessorType, unless accessorType is a Collection,
                    // then converterType equals Collection's parametrized type.
                    converterType = isMap ?  // accessor is a Collection?
                            getParameterizedMapType(getterMethod.getGenericReturnType()) :  // get it's parametrized type
                            accessorType;

                } else if (setterType != null) {

                    // define the setter
                    setterMethod = method;

                    // Try getting XML element name from @Attribute annotation.
                    // If not defined, use name derived from getter method name
                    attributeName = annotation.value().length() != 0 ? annotation.value()
                            : (annotation.name().length() != 0 ? annotation.name()
                            : getSetterName(method));

                    // base type is defined by setter's argument type
                    accessorType = setterType;

                    // is setterMethod type a Collection?
                    isMap = Collection.class.isAssignableFrom(setterType);

                    // Rule 2.: converterType equals accessorType, unless accessorType is a Collection,
                    // then converterType equals Collection's parametrized type.
                    converterType = isMap ?  // accessor is a Collection?
                            getParameterizedMapType(setterMethod.getGenericParameterTypes()[0]) :  // get it's parametrized type
                            accessorType;

                } else {
                    throw new XmapprConfigurationException("Error: @Attribute annotation on " +
                            "method " + method.getName() + " in class " + elementClass.getName()
                            + " does not seem to be an appropriate field accessor method. "
                            + "Getter method must not return void and take no arguments."
                            + "Setter method must return void and take one argument.");
                }

                if (annotation.targetType() == null || annotation.targetType().equals(Object.class)) {
                    targetType = accessorType;
                }

                // XML element name could not be derived from method name, nor from @Attribute annotation
                if (attributeName == null) {
                    throw new XmapprConfigurationException("Error: Could not find XML attribute name to map to. " +
                            "Either @Attribute annotation must have a 'name' attribute defined or accessor method " +
                            "must follow name convention for getters/setters (i.e. it must be of getXY or setXY form).");
                }

                // look if custom converter defined
                Class<? extends ValueConverter> customConverter = annotation.converter().equals(ValueConverter.class)
                        ? null : annotation.converter();

                // Rule 3.: if name is a wildcard mapping ("*") then converter is DomElementConverter, unless
                // custom converter is already defined by user
                if (attributeName.equals("*") && customConverter == null) {
                    customConverter = StringConverter.class;
                }

                if (confAttributeCache == null) {
                    confAttributeCache = new HashMap<String, ConfigAttribute>();
                }

                ConfigAttribute attribute = confAttributeCache.get(attributeName);
                if (attribute != null) {
                    if (attribute.field != null) {
                        // todo XML attribute defined via both - ERROR
                    }
                    attribute.accessorType = accessorType;
                    if (getterMethod != null) {
                        attribute.getter = getterMethod.getName();
                        attribute.getterMethod = getterMethod;
                    }
                    if (setterMethod != null) {
                        attribute.setter = setterMethod.getName();
                        attribute.setterMethod = setterMethod;
                    }
                    attribute.defaultvalue = annotation.defaultValue();
                    attribute.targetType = targetType;
                    attribute.format = annotation.format();
                    attribute.converter = annotation.converter();
                } else {
                    attribute = new ConfigAttribute(
                            attributeName,
                            accessorType,
                            isMap,
                            null,
                            null,
                            getterMethod,
                            (getterMethod == null) ? null : getterMethod.getName(),
                            setterMethod,
                            (setterMethod == null) ? null : setterMethod.getName(),
                            "".equals(annotation.defaultValue()) ? null : annotation.defaultValue(),
                            targetType,
                            annotation.format(),
                            customConverter
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

    public static Class hasGetterFormat(Method method) {
        if (method != null && method.getParameterTypes().length == 0) {
            return method.getReturnType();
        }
        return null;

    }

    public static Class hasSetterFormat(Method method) {
        if (method != null && equalTypes(method.getReturnType(), (Void.TYPE)) && method.getParameterTypes().length == 1) {
            return method.getParameterTypes()[0];
        }
        return null;
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
                    + elementClass.getName() + ". Max one @Tex annotation can be present in a class.");
        }

        ConfigText configText = null;
        if (found == 1) {

            // Rule 1.: accessorType is type of field
            Class accessorType = targetField.getType();

            // accessorType is a Collection
            boolean isCollection = Collection.class.isAssignableFrom(targetField.getType());

            // Rule 2.: converterType equals accessorType, unless accessorType is a Collection,
            // then converterType equals Collection's parametrized type.
            Class converterType = isCollection ?  // accessor is a Collection?
                    getParameterizedCollectionType(targetField.getGenericType()) :  // get it's parametrized type
                    accessorType;

            // look if custom converter defined
            Class<? extends ValueConverter> customConverter = fieldAnnotation.converter().equals(ValueConverter.class)
                    ? null : fieldAnnotation.converter();

            // retrieve targetType
            Class targetType = fieldAnnotation.targetType().equals(Object.class) ? null : fieldAnnotation.targetType();

            // Rule 7.: if custom converter is not defined then converterType is obligatory
            if (customConverter == null && converterType == null && targetType == null) {
                targetType = String.class;
            }

            configText = new ConfigText(
                    targetField.getType(),
                    targetField,
                    targetField.getName(),
                    isCollection,
                    converterType,
                    null,
                    null,
                    null,
                    null,
                    targetType,
                    fieldAnnotation.format(),
                    customConverter);
        }

        // continue with searching @Text annotations on methods
        for (Method method : elementClass.getMethods()) {

            Text annotation = method.getAnnotation(Text.class);
//            Class accessorType;
//            Method getter = null, setter = null;

            if (annotation != null) {

                Class accessorType, converterType;
                Method getterMethod = null, setterMethod = null;

                // retrieve targetType
                Class targetType = annotation.targetType().equals(Object.class) ? null : annotation.targetType();

                // accessorType is a Collection
                boolean isCollection;

                Class getterType = hasGetterFormat(method);
                Class setterType = hasSetterFormat(method);

                // Check if this is a getter or setter
                if (getterType != null) {

//                    // define the getter
//                    getter = method;
//
//                    // base type is defined by getter's return type
//                    accessorType = getterType;

                    // define the getter
                    getterMethod = method;

                    // Rule 1.: base type is defined by getter's return type
                    accessorType = getterType;

                    // is getter type a Collection?
                    isCollection = Collection.class.isAssignableFrom(getterType);

                    // Rule 2.: converterType equals accessorType, unless accessorType is a Collection,
                    // then converterType equals Collection's parametrized type.
                    converterType = isCollection ?  // accessor is a Collection?
                            getParameterizedCollectionType(getterMethod.getGenericReturnType()) :  // get it's parametrized type
                            accessorType;

                } else if (setterType != null) {

//                    // define the setter
//                    setter = method;
//
//                    // base type is defined by setter's argument type
//                    accessorType = setterType;

                    // define the setterMethod
                    setterMethod = method;

                    // Rule 1.: base type is defined by setterMethod's argument type
                    accessorType = setterType;

                    // is setterMethod type a Collection?
                    isCollection = Collection.class.isAssignableFrom(setterType);

                    // Rule 2.: converterType equals accessorType, unless accessorType is a Collection,
                    // then converterType equals Collection's parametrized type.
                    converterType = isCollection ?  // accessor is a Collection?
                            getParameterizedCollectionType(setterMethod.getGenericParameterTypes()[0]) :  // get it's parametrized type
                            accessorType;

                } else {
                    throw new XmapprConfigurationException("Error: @Text annotation is used on " +
                            "method " + method.getName() + " in class " + elementClass.getName()
                            + " , which does not seem to be an appropriate field accessor method. "
                            + "Getter method must not return void and take no arguments."
                            + "Setter method must return void and take one argument.");
                }

                // look if custom converter defined
                Class<? extends ValueConverter> customConverter = annotation.converter().equals(ValueConverter.class)
                        ? null : annotation.converter();

                if (configText == null) {
                    configText = new ConfigText(
                            accessorType,
                            null,
                            null,
                            isCollection,
                            converterType,
                            getterMethod,
                            (getterMethod == null) ? null : getterMethod.getName(),
                            setterMethod,
                            (setterMethod == null) ? null : setterMethod.getName(),
                            targetType,
                            annotation.format(),
                            customConverter);
                } else {
                    // double mapping - @Text defined on both field and method
                    if (configText.targetField != null) {
                        throw new XmapprConfigurationException("Error: Duplicate use of @Text annotation. "
                                + "Method " + method.getName() + " and field " + configText.targetField.getName()
                                + " in class " + elementClass.getName() + " both contain @Text annotation. "
                                + "@Text can only be used on one field or on a pair of accessor methods (getter/setter).");

                    } else if (configText.setterMethod != null) {
                        // setter can not be defined twice
                        if (setterMethod != null) {
                            throw new XmapprConfigurationException("Error: Duplicate use of @Text annotation."
                                    + "Methods " + setterMethod.getName() + " and " + configText.setterMethod.getName()
                                    + " in class " + elementClass.getName() + " both appear to be setters and both "
                                    + "contain @Text annotation. @Text can only be used on one getter and/or one setter.");
                        }
                        // getter and setter do not have compatible types
                        if (!equalTypes(getterMethod.getReturnType(), (configText.setterMethod.getParameterTypes()[0]))) {
                            throw new XmapprConfigurationException("Error: Incompatible getter and setter types."
                                    + "@Text annotations in class " + elementClass.getName()
                                    + " define getter method " + getterMethod.getName()
                                    + " and setter method " + configText.setterMethod.getName()
                                    + ". Return type of getter and first argument of setter must be of the same type.");
                        }
                        configText.getterMethod = getterMethod;
                        configText.getter = getterMethod.getName();
                        if (annotation.format().length() != 0)
                            configText.format = annotation.format();
                        if (!annotation.converter().equals(ValueConverter.class))
                            configText.converter = annotation.converter();
                        if (!annotation.targetType().equals(Object.class))
                            configText.targetType = annotation.targetType();

                    } else if (configText.getterMethod != null) {
                        // getter can not be defined twice
                        if (getterMethod != null) {
                            throw new XmapprConfigurationException("Error: Duplicate use of @Text annotation."
                                    + "Methods " + getterMethod.getName() + " and " + configText.getterMethod.getName()
                                    + " in class " + elementClass.getName() + " both appear to be getters and both "
                                    + "contain @Text annotation. @Text can only be used on one getter and/or one setter.");
                        }
                        // getter and setter do not have compatible types
                        if (!equalTypes(configText.getterMethod.getReturnType(), (setterMethod.getParameterTypes()[0]))) {
                            throw new XmapprConfigurationException("Error: Incompatible getter and setter types."
                                    + "@Text annotations in class " + elementClass.getName()
                                    + " define getter method " + configText.getterMethod.getName()
                                    + " and setter method " + setterMethod.getName()
                                    + ". Return type of getter and first argument of setter must be of the same type.");
                        }
                        configText.setterMethod = setterMethod;
                        configText.setter = setterMethod.getName();
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

    public static Class getParameterizedCollectionType(Type genericType) {
        if (genericType instanceof ParameterizedType) {
            Type[] typeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
            if (typeArguments.length == 1) {
                Type paraType = typeArguments[0];
                if (paraType instanceof Class)
                    return (Class) paraType;
            }
        }
        return null;
    }

    public static Class getParameterizedMapType(Type genericType) {
        if (genericType instanceof ParameterizedType) {
            Type[] typeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
            if (typeArguments.length == 2) {
                Type paraType = typeArguments[1];
                if (paraType instanceof Class)
                    return (Class) paraType;
            }
        }
        return null;
    }

    public static boolean isCollectionType(Class type) {
        return Collection.class.isAssignableFrom(type);
    }

    public static boolean isMapType(Class type) {
        return Map.class.isAssignableFrom(type);
    }


//    public static Class getCollectionParameterizedType(Field field) {
//        Type genericType = field.getGenericType();
//        if (Collection.class.isAssignableFrom(field.getType())) {
//            return getParameterizedCollectionType(genericType);
//        }
//        return null;
//    }
//
//    public static Class getCollectionParameterizedType(Method getter, Method setter) {
//        Type genericType;
//        if (getter != null) {
//            genericType = getter.getGenericReturnType();
//            if (Collection.class.isAssignableFrom(getter.getReturnType())) {
//                return getParameterizedCollectionType(genericType);
//            }
//        } else if (setter != null) {
//            genericType = setter.getGenericParameterTypes()[0];
//            if (Collection.class.isAssignableFrom(setter.getParameterTypes()[0])) {
//                return getParameterizedCollectionType(genericType);
//            }
//        }
//        return null;
//    }

    private static Map<Class<?>, Class<?>> primToWrap = new HashMap<Class<?>, Class<?>>(16);

    static {
        primToWrap.put(boolean.class, Boolean.class);
        primToWrap.put(byte.class, Byte.class);
        primToWrap.put(char.class, Character.class);
        primToWrap.put(double.class, Double.class);
        primToWrap.put(float.class, Float.class);
        primToWrap.put(int.class, Integer.class);
        primToWrap.put(long.class, Long.class);
        primToWrap.put(short.class, Short.class);
        primToWrap.put(void.class, Void.class);
    }

    /**
     * checks if types are equal, takin primitive types into account
     *
     * @param type1
     * @param type2
     * @return
     */
    public static boolean equalTypes(Class type1, Class type2) {
        if (type1.isPrimitive()) type1 = primToWrap.get(type1);
        if (type2.isPrimitive()) type2 = primToWrap.get(type2);

        return type1.equals(type2);
    }
}
