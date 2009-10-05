package org.xlite.converters;

import org.xlite.XliteConfigurationException;

public class ClassNameConverter extends ValueConverter {

//    private static Map<String, Class> classNames;
//
//    static{
//        classNames = new HashMap<String, Class>();
//        classNames.put("int", int.class);
//        classNames.put("Integer", Integer.class);
//        classNames.put("byte", byte.class);
//        classNames.put("Byte", Byte.class);
//        classNames.put("char", char.class);
//        classNames.put("Character", Character.class);
//        classNames.put("boolean", boolean.class);
//        classNames.put("Boolean", Boolean.class);
//        classNames.put("long", long.class);
//        classNames.put("Long", Long.class);
//        classNames.put("", .class);
//
//    }

    @Override
    public Object fromValue(String className, String format, Class targetType, Object targetObject) {

        // given class name could be an abbreviation, so we construct various possible variants
        String[] tryNames = new String[2];

        // full class name with package was given
        tryNames[0] = className;

        // Java primitive types - e.g. Integer becomes java.util.Integer
        tryNames[1] = "java.lang." + className;

        // now try to get Class from various class names
        Class clazz = null;
        for (String name : tryNames) {
            try {
                clazz = Class.forName(name);
                break;
            } catch (ClassNotFoundException e) {
                // this is normal, class was not found - will try next class name
            }
        }

        // could not find any Class by it's name or variants
        if (clazz == null) {
            throw new XliteConfigurationException("Error: Class named '" + className + "' was not found! " +
                    "Please check your XML mapping configuration.");
        }

        return clazz;
    }

    /**
     * Returns object's class name.
     *
     * @param object
     * @param format If format equals to "nopackage", only short name of the class is returned
     * @return
     */
    @Override
    public String toValue(Object object, String format) {
        String className = ((Class)object).getName();
        if ("nopackage".equals(format)) {
            return className.substring(className.lastIndexOf(".") + 1, className.length());
        } else {
            return className;
        }
    }

    public boolean canConvert(Class type) {
        return Class.class.equals(type.getClass());
    }

}
