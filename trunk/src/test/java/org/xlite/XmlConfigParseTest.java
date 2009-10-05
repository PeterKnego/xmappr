package org.xlite;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xlite.converters.EmptyStringConverter;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

public class XmlConfigParseTest {

    private static String xml = "" +
            "<top attr1=\"11\" attr2=\"twenty\">" +
            "some text" +
            "<one>1</one>" +
            "<one>2</one>" +
            "<sub>" +
            "<two tt='5'>1.123</two>" +
            "</sub>" +
            "</top>";

    private static String xmlConfig = "" +
            "<root-element class='org.xlite.XmlConfigParseTest$Top'>" +
            "<attribute name='attr1' field='a1'/>" +
            "<attribute name='attr2' field='a2'/>" +
            "<element name='one' field='one' itemType = 'Integer'/>" +
            "<element name='sub' field='subelement'>" +
            "  <attribute name='tt' field='ttAttrib'/>" +
            "  <element name='two' field='two'/>" +
            "</element>" +
            "</root-element>";

    @Test
    public void basicTest() throws IOException, SAXException {

        StringReader reader = new StringReader(xml);
        StringReader confReader = new StringReader(xmlConfig);
        ConfigRootElement xmlConf = ConfigurationProcessor.processConfiguration(confReader);
        ConfigRootElement classConf = ConfigurationProcessor.processConfiguration(Top.class);

        StringWriter swClass = new StringWriter();
        StringWriter swXml = new StringWriter();

        Xlite xlite = new Xlite(ConfigRootElement.class);
        xlite.addConverter(new EmptyStringConverter());
        xlite.toXML(classConf, swClass);
        xlite.toXML(classConf, swXml);

        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(swClass.toString(),swXml.toString() );

        System.out.println(swClass);
        System.out.println(swXml);

    }

    @RootElement
    public static class Top {

        @Attribute("attr1")
        public int a1;

        @Attribute("attr2")
        public String a2;

        @Element(itemType = Integer.class)
        public List one;

        @Element("sub")
        public Sub subelement;

    }

    public static class Sub {

        @Attribute("tt")
        public double ttAttrib;

        @Element
        public float two;

    }
}
