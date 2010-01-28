package org.xmappr.converters;

import org.xmappr.XmapprConfigurationException;

import java.util.HashMap;
import java.util.Map;

public class ClassNameConverter extends ValueConverter {

    private static Map<String, Class> classNames;

    static{
        classNames = new HashMap<String, Class>();
        classNames.put("int", Integer.class);
        classNames.put("byte", Byte.class);
        classNames.put("char", Character.class);
        classNames.put("boolean", Boolean.class);
        classNames.put("long", Long.class);
        classNames.put("float", Float.class);
        classNames.put("double", Double.class);
        classNames.put("short", Short.class);
    }

    @Override
    public Object fromValue(String className, String format, Class targetType, Object targetObject) {

        // given class name could be an abbreviation, so we construct various possible variants
        String[] tryNames = new String[3];

        // full class name with package was given
        tryNames[0] = className;

        // Java primitive types - e.g. Integer becomes java.util.Integer
        tryNames[1] = "java.lang." + className;

        // Java primitive types - e.g. Integer becomes java.util.Integer
        tryNames[2] = classNames.containsKey(className) ? classNames.get(className).getName() : "";

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
            throw new XmapprConfigurationException("Error: Class named '" + className + "' was not found! " +
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


    public static void main(String[] args) {

        Class inte = int.class;

        try {
            Class clazz = Class.forName("java.lang.int");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
