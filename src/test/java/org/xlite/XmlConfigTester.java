package org.xlite;

import org.xlite.converters.EmptyStringConverter;

import java.io.StringReader;
import java.io.StringWriter;

public class XmlConfigTester {

    public static StringReader reader(Class rootClass) {

        ConfigRootElement xmlConf = ConfigurationProcessor.processConfiguration(rootClass);

        StringWriter swClass = new StringWriter();
        Xlite xlite = new Xlite(ConfigRootElement.class);
        xlite.addConverter(new EmptyStringConverter());
        xlite.toXML(xmlConf, swClass);

        return new StringReader(swClass.toString());
    }
}
