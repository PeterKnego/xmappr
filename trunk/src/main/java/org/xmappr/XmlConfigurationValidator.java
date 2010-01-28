package org.xmappr;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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

        if (element.targetType == null || element.targetType.equals(Object.class)) {
            if (element.targetField != null) {
                element.targetType = element.targetField.getType();
            } else if (element.getterMethod != null) {
                element.targetType = element.getterMethod.getReturnType();
            } else if (element.setterMethod != null) {
                element.targetType = element.setterMethod.getParameterTypes()[0];
            } else {
                throw new XmapprConfigurationException("Xmappr system error: Please report this on mailing list.");
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
        if (attribute.targetType == null || attribute.targetType.equals(Object.class)) {
            if (attribute.targetField != null) {
                attribute.targetType = attribute.targetField.getType();
            } else if (attribute.getterMethod != null) {
                attribute.targetType = attribute.getterMethod.getReturnType();
            } else if (attribute.setterMethod != null) {
                attribute.targetType = attribute.setterMethod.getParameterTypes()[0];
            } else {
                throw new XmapprConfigurationException("Xmappr system error: Please report this on mailing list.");
            }
        }
    }

    public static void xmlTextChecker(ConfigText text) {
        if (text.targetType == null || text.targetType.equals(Object.class)) {
            if (text.targetField != null) {
                text.targetType = text.targetField.getType();
            } else if (text.getterMethod != null) {
                text.targetType = text.getterMethod.getReturnType();
            } else if (text.setterMethod != null) {
                text.targetType = text.setterMethod.getParameterTypes()[0];
            } else {
                throw new XmapprConfigurationException("Xmappr system error: Please report this on mailing list.");
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
    }


    public static Class validateConfigElement(ConfigElement configElement, Class containingClass) {

        Class nextClass = validateConfigElementInternals(configElement, containingClass);


        if (configElement.attribute != null)
            for (ConfigAttribute configAttribute : configElement.attribute) {
                validateConfigAttribute(configAttribute, nextClass);
            }

        if (configElement.text != null) {
            validateConfigText(configElement.text, nextClass);
        }

        return nextClass;
    }

    public static Class validateConfigElementInternals(ConfigElement element, Class containingClass) {

        Class nextClass;

        if (element.name == null) {
            throw new XmapprConfigurationException("Error: XML node name not defined. Mapping must define "
                    + "a name of the target XML element. " +
                    "\nOffending mapping:\n\n" + element);
        }

        // target is not defined
        if (element.field == null && element.getter == null && element.setter == null) {
            throw new XmapprConfigurationException("Error: Unknown mapping target: neither 'field' nor " +
                    "'getter' and/or 'setter' attributes are defined. Mapping must define ONE target: either 'field' or " +
                    "'getter' and/or 'setter' attributes." +
                    "\nOffending mapping:\n\n" + element);
        }
        if (element.field != null && (element.getter != null || element.setter != null)) {
            throw new XmapprConfigurationException("Error: Ambiguous mapping target: both 'field' and one of " +
                    "'getter' or 'setter' attributes defined. Mapping must define only ONE target: either 'field' or " +
                    " 'getter' and/or 'setter' attributes." +
                    "\nOffending mapping:\n\n" + element);
        }

        // field defined
        if (element.field != null) {
            element.targetField = findField(containingClass, element.field);
            element.baseType = element.targetField.getType();
            nextClass = element.baseType;
        } else {
            // when using getter or setter, a targetType must be defined
            if (element.targetType == null || Object.class.equals(element.targetType)) {
                throw new XmapprConfigurationException("Error: No target type defined. When using getter or setter" +
                        " method, a 'targetType' attribute must be defined" +
                        "\nOffending mapping:\n\n" + element);
            }
            element.baseType = element.targetType;
            nextClass = element.baseType;

            // getter name defined
            if (element.getterMethod == null && element.getter != null) {
                try {
                    // find a getter method
                    Method getterMethod = containingClass.getMethod(element.getter);
                    if (!getterMethod.getReturnType().equals(element.targetType))
                        throw new NoSuchMethodException();
                    element.getterMethod = getterMethod;
                } catch (NoSuchMethodException e) {
                    throw new XmapprConfigurationException("Error: Getter method not found in class " +
                            containingClass.getName() +
                            " No method matches " + element.getter +
                            "() with return type " + element.targetType.getName() + "." +
                            "\nOffending mapping:\n\n" + element);
                }
            } else if (element.setterMethod == null && element.setter != null) {
                try {
                    // find a setter method
                    element.setterMethod = containingClass.getMethod(element.setter, element.targetType);
                } catch (NoSuchMethodException e) {
                    throw new XmapprConfigurationException("Error: Setter method not found in class " +
                            containingClass.getName() +
                            " No method matches: " + element.setter +
                            "(" + element.targetType.getName() + ") with return type void." +
                            "\nOffending mapping:\n\n" + element);
                }
            }
        }

        return nextClass;
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
                    Method getterMethod = containingClass.getMethod(text.getter);
                    if (!getterMethod.getReturnType().equals(text.targetType))
                        throw new NoSuchMethodException();
                    text.getterMethod = getterMethod;
                } catch (NoSuchMethodException e) {
                    throw new XmapprConfigurationException("Error: Getter method not found in class " +
                            containingClass.getName() +
                            " No method matches " + text.getter +
                            "() with return type " + text.targetType.getName() + "." +
                            "\nOffending mapping:\n\n" + text);
                }
            } else if (text.setterMethod == null && text.setter != null) {
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
                    Method getterMethod = elementClass.getMethod(attribute.getter);
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
