package org.xlite;

import org.xlite.converters.EmptyStringConverter;

import java.io.StringReader;

public class XmlConfigTester {

    public static StringReader reader(Class rootClass) {
        return reader(rootClass, null);

    }

    public static StringReader reader(Class rootClass, String[] namespaces) {

        Xlite xlite = new Xlite(rootClass);

        if(namespaces!=null){
            for (String namespace : namespaces) {
               xlite.addNamespace(namespace);
            }
        }
        
        xlite.addConverter(new EmptyStringConverter());
        String classConf = xlite.getXmlConfiguration(rootClass);

        return new StringReader(classConf);
    }

}
