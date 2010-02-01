package org.xmappr;

import org.xmappr.converters.DomElementConverter;
import org.xmappr.converters.ElementConverter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

public class XmlConfigurationValidator {

    public static void outputXmlChecker(ConfigRootElement rootElement) {

        if (rootElement.element != null) {
            for (ConfigElement element : rootElement.element) {
                xmlElementChecker(element);
            }
        }

        if (rootElement.attribute != null) {
            for (ConfigAttribute attribute : rootElement.attribute) {
                xmlAttributeChecker(attribute);
            }
        }

        if (rootElement.text != null) xmlTextChecker(rootElement.text);
    }

    public static void xmlElementChecker(ConfigElement element) {

        Class parametrizedType;

        // fill in targetType parameter - it's obligatory for XML based config
        if (element.targetType == null || Object.class.equals(element.targetType)) {
            if (element.targetField != null) {
                element.targetType = element.targetField.getType();
                if (ConfigurationProcessor.isCollectionType(element.targetField.getType())
                        && element.converter == null) {
                    parametrizedType = ConfigurationProcessor.getParameterizedCollectionType(element.targetField.getGenericType());
                    if (parametrizedType == null) {
                        throw new XmapprConfigurationException("\nError: @Element annotation is used on " +
                                "field " + element.targetField.getName() + " (class " +
                                element.targetField.getDeclaringClass().getName() +
                                ") is of  " + element.targetField.getType() + " type.\n" +
                                "@Element annotation defined on fields of java.util.Collection type must have " +
                                "either 'targetType' attribute defined or Collection type must be a " +
                                "parametrized generic type (e.g. List<String>).");
                    } else {
                        element.targetType = parametrizedType;
                    }
                }
            } else if (element.getterMethod != null) {
                element.targetType = element.getterMethod.getReturnType();
                if (ConfigurationProcessor.isCollectionType(element.getterMethod.getReturnType())
                        && element.converter == null) {
                    parametrizedType = ConfigurationProcessor.getParameterizedCollectionType(element.getterMethod.getGenericReturnType());
                    if (parametrizedType == null) {
                        throw new XmapprConfigurationException("\nError: @Element annotation is used on " +
                                "getter method " + element.getterMethod.getName() + " (class " +
                                element.getterMethod.getDeclaringClass().getName() +
                                ") which has " + element.getterMethod.getReturnType() + " return type.\n" +
                                "@Element annotation defined on types of java.util.Collection type must have " +
                                "either 'targetType' attribute defined or Collection type must be a " +
                                "parametrized generic type (e.g. List<String>).");
                    } else {
                        element.targetType = parametrizedType;
                    }
                }
            } else if (element.setterMethod != null) {
                element.targetType = element.setterMethod.getParameterTypes()[0];
                if (ConfigurationProcessor.isCollectionType(element.setterMethod.getParameterTypes()[0])) {
                    parametrizedType = ConfigurationProcessor.getParameterizedCollectionType(element.setterMethod.getGenericParameterTypes()[0]);
                    if (parametrizedType == null) {
                        throw new XmapprConfigurationException("\nError: @Element annotation is used on " +
                                "setter method " + element.setterMethod.getName() + " (class " +
                                element.setterMethod.getDeclaringClass().getName() +
                                ") which has " + element.getterMethod.getParameterTypes()[0] + " parameter type.\n" +
                                "@Element annotation defined on types of java.util.Collection type must have " +
                                "either 'targetType' attribute defined or Collection type must be a " +
                                "parametrized generic type (e.g. List<String>).");
                    } else {
                        element.targetType = parametrizedType;
                    }
                }
            }
        }

        if (element.element != null) {
            for (ConfigElement subelement : element.element) {
                xmlElementChecker(subelement);
            }
        }

        if (element.attribute != null) {
            for (ConfigAttribute attribute : element.attribute) {
                xmlAttributeChecker(attribute);
            }
        }

        if (element.text != null) xmlTextChecker(element.text);

    }


    public static void xmlAttributeChecker(ConfigAttribute attribute) {

        Class parametrizedType;

        // fill in targetType parameter - it's obligatory for XML based config
        if (attribute.targetType == null || Object.class.equals(attribute.targetType)) {
            if (attribute.targetField != null) {
                attribute.targetType = attribute.targetField.getType();
                if (ConfigurationProcessor.isMapType(attribute.targetField.getType())
                        && attribute.converter == null) {
                    parametrizedType = ConfigurationProcessor.getParameterizedMapType(attribute.targetField.getGenericType());
                    if (parametrizedType == null) {
                        throw new XmapprConfigurationException("\nError: @Atribute annotation is used on " +
                                "field " + attribute.targetField.getName() + " (class " +
                                attribute.targetField.getDeclaringClass().getName() +
                                ") is of  " + attribute.targetField.getType() + " type.\n" +
                                "@Atribute annotation defined on fields of java.util.Collection type must have " +
                                "either 'targetType' attribute defined or Collection type must be a " +
                                "parametrized generic type (e.g. List<String>).");
                    } else {
                        attribute.targetType = parametrizedType;
                    }
                }
            } else if (attribute.getterMethod != null) {
                attribute.targetType = attribute.getterMethod.getReturnType();
                if (ConfigurationProcessor.isMapType(attribute.getterMethod.getReturnType())
                        && attribute.converter == null) {
                    parametrizedType = ConfigurationProcessor.getParameterizedMapType(attribute.getterMethod.getGenericReturnType());
                    if (parametrizedType == null) {
                        throw new XmapprConfigurationException("\nError: @Atribute annotation is used on " +
                                "getter method " + attribute.getterMethod.getName() + " (class " +
                                attribute.getterMethod.getDeclaringClass().getName() +
                                ") which has " + attribute.getterMethod.getReturnType() + " return type.\n" +
                                "@Atribute annotation defined on types of java.util.Collection type must have " +
                                "either 'targetType' attribute defined or Collection type must be a " +
                                "parametrized generic type (e.g. List<String>).");
                    } else {
                        attribute.targetType = parametrizedType;
                    }
                }
            } else if (attribute.setterMethod != null) {
                attribute.targetType = attribute.setterMethod.getParameterTypes()[0];
                if (ConfigurationProcessor.isMapType(attribute.setterMethod.getParameterTypes()[0])) {
                    parametrizedType = ConfigurationProcessor.getParameterizedMapType(attribute.setterMethod.getGenericParameterTypes()[0]);
                    if (parametrizedType == null) {
                        throw new XmapprConfigurationException("\nError: @Atribute annotation is used on " +
                                "setter method " + attribute.setterMethod.getName() + " (class " +
                                attribute.setterMethod.getDeclaringClass().getName() +
                                ") which has " + attribute.getterMethod.getParameterTypes()[0] + " parameter type.\n" +
                                "@Atribute annotation defined on types of java.util.Collection type must have " +
                                "either 'targetType' attribute defined or Collection type must be a " +
                                "parametrized generic type (e.g. List<String>).");
                    } else {
                        attribute.targetType = parametrizedType;
                    }
                }
            }
        }
    }

    public static void xmlTextChecker(ConfigText text) {
//        if (text.targetType == null || ConfigurationProcessor.equalTypes(text.targetType, (Object.class))) {
//            if (text.targetField != null) {
//                text.targetType = text.targetField.getType();
//            } else if (text.getterMethod != null) {
//                text.targetType = text.getterMethod.getReturnType();
//            } else if (text.setterMethod != null) {
//                text.targetType = text.setterMethod.getParameterTypes()[0];
//            } else {
//                throw new XmapprConfigurationException("Xmappr system error: Please report this on mailing list.");
//            }
//        }

           Class parametrizedType;

        // fill in targetType parameter - it's obligatory for XML based config
        if (text.targetType == null || Object.class.equals(text.targetType)) {
            if (text.targetField != null) {
                text.targetType = text.targetField.getType();
                if (ConfigurationProcessor.isMapType(text.targetField.getType())
                        && text.converter == null) {
                    parametrizedType = ConfigurationProcessor.getParameterizedMapType(text.targetField.getGenericType());
                    if (parametrizedType == null) {
                        throw new XmapprConfigurationException("\nError: @Text annotation is used on " +
                                "field " + text.targetField.getName() + " (class " +
                                text.targetField.getDeclaringClass().getName() +
                                ") is of  " + text.targetField.getType() + " type.\n" +
                                "@Text annotation defined on fields of java.util.Collection type must have " +
                                "either 'targetType' attribute defined or Collection type must be a " +
                                "parametrized generic type (e.g. List<String>).");
                    } else {
                        text.targetType = parametrizedType;
                    }
                }
            } else if (text.getterMethod != null) {
                text.targetType = text.getterMethod.getReturnType();
                if (ConfigurationProcessor.isMapType(text.getterMethod.getReturnType())
                        && text.converter == null) {
                    parametrizedType = ConfigurationProcessor.getParameterizedMapType(text.getterMethod.getGenericReturnType());
                    if (parametrizedType == null) {
                        throw new XmapprConfigurationException("\nError: @Text annotation is used on " +
                                "getter method " + text.getterMethod.getName() + " (class " +
                                text.getterMethod.getDeclaringClass().getName() +
                                ") which has " + text.getterMethod.getReturnType() + " return type.\n" +
                                "@Text annotation defined on types of java.util.Collection type must have " +
                                "either 'targetType' attribute defined or Collection type must be a " +
                                "parametrized generic type (e.g. List<String>).");
                    } else {
                        text.targetType = parametrizedType;
                    }
                }
            } else if (text.setterMethod != null) {
                text.targetType = text.setterMethod.getParameterTypes()[0];
                if (ConfigurationProcessor.isMapType(text.setterMethod.getParameterTypes()[0])) {
                    parametrizedType = ConfigurationProcessor.getParameterizedMapType(text.setterMethod.getGenericParameterTypes()[0]);
                    if (parametrizedType == null) {
                        throw new XmapprConfigurationException("\nError: @Text annotation is used on " +
                                "setter method " + text.setterMethod.getName() + " (class " +
                                text.setterMethod.getDeclaringClass().getName() +
                                ") which has " + text.getterMethod.getParameterTypes()[0] + " parameter type.\n" +
                                "@Text annotation defined on types of java.util.Collection type must have " +
                                "either 'targetType' attribute defined or Collection type must be a " +
                                "parametrized generic type (e.g. List<String>).");
                    } else {
                        text.targetType = parametrizedType;
                    }
                }
            }
        }

    }


    public static void validateConfigRootElement(ConfigRootElement configRootElement) {

        // no need to validate if element was derived from Annotations
        if (configRootElement.fromAnnotation) {
            return;
        }

        if (configRootElement.name == null) {
            throw new XmapprConfigurationException("Error: XML node name not defined. Mapping must define "
                    + "a name of the target XML element. " +
                    "\nOffending mapping:\n\n" + configRootElement);
        }

        if (configRootElement.classType == null) {
            throw new XmapprConfigurationException("Error: Root class not defined. Mapping must define "
                    + "a 'classType' attribute. " +
                    "\nOffending mapping:\n\n" + configRootElement);
        }
        Class targetClass = configRootElement.classType;

        if (configRootElement.attribute != null)
            for (ConfigAttribute configAttribute : configRootElement.attribute) {
                validateConfigAttribute(configAttribute, targetClass);
            }

        if (configRootElement.text != null) {
            validateConfigText(configRootElement.text, targetClass);
        }

        if (configRootElement.element != null) {
            for (ConfigElement element : configRootElement.element) {
                validateConfigElement(element, targetClass);
            }
        }
    }


    public static void validateConfigElement(ConfigElement configElement, Class containingClass) {

        Class nextClass = validateConfigElementInternals(configElement, containingClass);

        if (nextClass == null) return;

        if (configElement.attribute != null)
            for (ConfigAttribute configAttribute : configElement.attribute) {
                validateConfigAttribute(configAttribute, nextClass);
            }

        if (configElement.text != null) {
            validateConfigText(configElement.text, nextClass);
        }

        if (configElement.element != null)
            for (ConfigElement element : configElement.element) {
                validateConfigElement(element, nextClass);
            }
    }

    public static Class validateConfigElementInternals(ConfigElement element, Class containingClass) {

        if (element.name == null) {
            throw new XmapprConfigurationException("Error: XML node name not defined. Mapping must define "
                    + "a name of the target XML element. " +
                    "\nOffending mapping:\n\n" + element);
        }

        // target is not defined
        if (element.field == null && element.getter == null && element.setter == null) {
            throw new XmapprConfigurationException("\nError: Unknown mapping target: neither 'field' nor " +
                    "'getter' and/or 'setter' attributes are defined.\n" +
                    "Mapping must define ONE target: either 'field' or " +
                    "'getter' and/or 'setter' attributes." +
                    "\nOffending mapping:\n\n" + element);
        }

        // both 'field' and 'getter' or 'setter' are defined
        if (element.field != null && (element.getter != null || element.setter != null)) {
            throw new XmapprConfigurationException("\nError: Ambiguous mapping target: both 'field' and one of " +
                    "'getter' or 'setter' attributes defined.\n" +
                    "Mapping must define only ONE target: either 'field' or " +
                    " 'getter' and/or 'setter' attributes." +
                    "\nOffending mapping:\n\n" + element);
        }

        // field defined
        if (element.field != null) {
            element.targetField = findField(containingClass, element.field);

            // Rule 1.: accessorType is type of field
            element.accessorType = element.targetField.getType();

            // accessorType is a Collection
            element.isCollection = Collection.class.isAssignableFrom(element.targetField.getType());

            // Rule 2.: converterType equals accessorType, unless accessorType is a Collection,
            // then converterType equals Collection's parametrized type.
            element.converterType = element.isCollection ?  // accessor is a Collection?
                    ConfigurationProcessor.getParameterizedCollectionType(element.targetField.getGenericType()) :  // get it's parametrized type
                    element.accessorType;

            // look if custom converter defined
            element.converter = ElementConverter.class.equals(element.converter) ? null : element.converter;

        } else {
            // when using getter or setter, a targetType must be defined
            element.targetType = Object.class.equals(element.targetType) ? null : element.targetType;
            if (element.targetType == null) {
                throw new XmapprConfigurationException("Error: No target type defined. When using getter or setter" +
                        " method, a 'targetType' attribute must be defined" +
                        "\nOffending mapping:\n\n" + element);
            }

            // look if custom converter defined
            element.converter = ElementConverter.class.equals(element.converter) ? null : element.converter;


            Class getterType = null, setterType = null;

            // getter name defined
            if (element.getter != null) {
                try {
                    // find a getter method and it's accessor type
                    Method getterMethod = containingClass.getMethod(element.getter);
                    if (getterMethod == null) throw new NoSuchMethodException();

                    getterType = ConfigurationProcessor.hasGetterFormat(getterMethod);

                    element.getterMethod = getterMethod;

                    // Rule 1.: base type is defined by getter's return type
                    element.accessorType = getterType;

                    // is getter type a Collection?
                    element.isCollection = Collection.class.isAssignableFrom(getterType);

                    // Rule 2.: converterType equals accessorType, unless accessorType is a Collection,
                    // then converterType equals Collection's parametrized type.
                    element.converterType = element.isCollection ?  // accessor is a Collection?
                            ConfigurationProcessor.getParameterizedCollectionType(element.getterMethod.getGenericReturnType()) :  // get it's parametrized type
                            element.accessorType;

                } catch (NoSuchMethodException e) {
                    throw new XmapprConfigurationException("\nError: Getter method not found in class " +
                            containingClass.getName() +
                            " No method matches " + element.getter +
                            "() with return type " + element.targetType.getName() + "." +
                            "\nOffending mapping:\n\n" + element);
                }
            }
            if (element.setter != null) {
                try {
                    // find a setter method and it's accessor type
                    int methodsFound = 0;
                    Class argType = null;
                    for (Method method : containingClass.getMethods()) {
                        argType = ConfigurationProcessor.hasSetterFormat(method);
                        if (argType != null && method.getName().equals(element.setter)) {
                            methodsFound++;
                            element.setterMethod = method;
                            setterType = argType;
                        }
                    }

                    if (methodsFound == 0) {
                        throw new NoSuchMethodException();
                    } else if (methodsFound > 1) {
                        throw new XmapprConfigurationException("\nError: Multiple setter methods found in class " +
                                containingClass.getName() +
                                "\nMultiple methods match: " + element.setter +
                                " with return type void and one parameter." +
                                "\nOffending mapping:\n\n" + element);
                    }

                    // Rule 1.: base type is defined by setter's argument type
                    element.accessorType = setterType;

                    // is getter type a Collection?
                    element.isCollection = ConfigurationProcessor.isCollectionType(setterType);

                    // Rule 2.: converterType equals accessorType, unless accessorType is a Collection,
                    // then converterType equals Collection's parametrized type.
                    element.converterType = element.isCollection ?  // accessor is a Collection?
                            ConfigurationProcessor.getParameterizedCollectionType(element.setterMethod.getGenericParameterTypes()[0]) :  // get it's parametrized type
                            element.accessorType;

                } catch (NoSuchMethodException e) {
                    throw new XmapprConfigurationException("\nError: Setter method not found in class " +
                            containingClass.getName() +
                            " No method matches: " + element.setter +
                            "(" + element.targetType.getName() + ") with return type void." +
                            "\nOffending mapping:\n\n" + element);
                }
            }

            // getter and setter types must be the same
            if (getterType != null && setterType != null
                    && !ConfigurationProcessor.equalTypes(getterType, (setterType))) {
                throw new XmapprConfigurationException("\nError: Mismatched types of accessor methods." +
                        "Return type of getter and first argument of setter must be of the same type.\n" +
                        "Getter " + element.getter + " has return type " + getterType + ".\n" +
                        "Setter " + element.setter + " has argument type " + setterType + ".\n" +
                        "\nOffending mapping:\n" + element);
            }
        }

        // Rule 3.: if name is a wildcard mapping ("*") then converter is DomElementConverter, unless
        // custom converter is already defined by user
        if (element.name.equals("*") && element.converter == null) {
            element.converter = DomElementConverter.class;
        }

        // set targetType
        element.targetType = Object.class.equals(element.targetType) ? null : element.targetType;

        // Rule 4.: if custom converter is not defined then converterType is obligatory
        if (element.converter == null && element.converterType == null && element.targetType == null) {
            throw new XmapprConfigurationException("Error: Converter type could not be inferred. " +
                    "<element> is mapped to " +
                    "field " + element.field + " (class " + containingClass.getName() +
                    ") of " + element.targetField.getGenericType() + " type. " +
                    "<element> mapped to fields of java.util.Collection type must have " +
                    "either 'targetType' attribute defined or Collection type must be a " +
                    "parametrized generic type (e.g. List<String>).");
        }

        // Which class to process next?
        if (element.converter != null) {
            return null;
        } else {
            return (element.targetType != null) ? element.targetType : element.converterType;
        }
    }

    public static void validateConfigText(ConfigText text, Class containingClass) {

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
            text.targetField = findField(containingClass, text.field);
            text.accessorType = text.targetField.getType();
        } else {
            // when using getter or setter, a targetType must be defined
            if (text.targetType == null || Object.class.equals(text.targetType)) {
                throw new XmapprConfigurationException("Error: No target type defined. When using getter or setter" +
                        " method, a 'targetType' attribute must be defined" +
                        "\nOffending mapping:\n\n" + text);
            }
            text.accessorType = text.targetType;

            // getter name defined
            if (text.getterMethod == null && text.getter != null) {
                try {
                    // find a getter method
                    Method getterMethod = containingClass.getMethod(text.getter);
                    if (!ConfigurationProcessor.equalTypes(getterMethod.getReturnType(), (text.targetType)))
                        throw new NoSuchMethodException();
                    text.getterMethod = getterMethod;
                } catch (NoSuchMethodException e) {
                    throw new XmapprConfigurationException("Error: Getter method not found in class " +
                            containingClass.getName() +
                            " No method matches " + text.getter +
                            "() with return type " + text.targetType.getName() + "." +
                            "\nOffending mapping:\n\n" + text);
                }
            }
            if (text.setterMethod == null && text.setter != null) {
                try {
                    // find a setter method
                    text.setterMethod = containingClass.getMethod(text.setter, text.targetType);
                } catch (NoSuchMethodException e) {
                    throw new XmapprConfigurationException("Error: Setter method not found in class " +
                            containingClass.getName() +
                            " No method matches: " + text.setter +
                            "(" + text.targetType.getName() + ") with return type void." +
                            "\nOffending mapping:\n\n" + text);
                }
            }
        }

    }

    public static void validateConfigAttribute(ConfigAttribute attribute, Class elementClass) {
        if (attribute.name == null) {
            throw new XmapprConfigurationException("Error: XML node name not defined. Mapping must define "
                    + "a name of the target XML attribute. " +
                    "\nOffending mapping:\n\n" + attribute);
        }

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
            attribute.accessorType = attribute.targetField.getType();
        } else {

            // when using getter or setter, a targetType must be defined
            if (attribute.targetType == null || Object.class.equals(attribute.targetType)) {
                throw new XmapprConfigurationException("Error: No target type defined. When using getter or setter" +
                        " method, a 'targetType' attribute must be defined" +
                        "\nOffending mapping:\n\n" + attribute);
            }
            attribute.accessorType = attribute.targetType;

            // getter name defined
            if (attribute.getterMethod == null && attribute.getter != null) {
                try {
                    // find a getter method
                    Method getterMethod = elementClass.getMethod(attribute.getter);
                    if (!ConfigurationProcessor.equalTypes(getterMethod.getReturnType(), attribute.targetType))
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
}
