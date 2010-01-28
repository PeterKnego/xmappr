package org.xmappr;

import org.xmappr.converters.EmptyStringConverter;

import java.io.StringReader;

public class XmlConfigTester {

    public static StringReader reader(Class rootClass) {
        return reader(rootClass, null);
    }

    public static StringReader reader(Class rootClass, String[] namespaces) {

        Xmappr xmappr = new Xmappr(rootClass);

        if(namespaces!=null){
            for (String namespace : namespaces) {
               xmappr.addNamespace(namespace);
            }
        }
        
        xmappr.addConverter(new EmptyStringConverter());
        String classConf = xmappr.getXmlConfiguration(rootClass);

        return new StringReader(classConf);
    }

}
