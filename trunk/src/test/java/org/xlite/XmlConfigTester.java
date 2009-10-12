package org.xlite;

import org.xlite.converters.EmptyStringConverter;

import java.io.StringReader;
import java.io.StringWriter;

public class XmlConfigTester {

    public static StringReader reader(Class rootClass) {

        Xlite xlite = new Xlite(rootClass);
        xlite.addConverter(new EmptyStringConverter());
        String classConf = xlite.getXmlConfiguration(rootClass);

        return new StringReader(classConf);
    }
}
