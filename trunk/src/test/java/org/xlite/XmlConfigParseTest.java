package org.xlite;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.testng.annotations.Test;
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
            "<root-element name='top' class='org.xlite.XmlConfigParseTest$Top'>" +
            "<attribute name='attr1' field='a1'/>" +
            "<attribute name='attr2' field='a2'/>" +
            "<element name='one' field='one' itemType = 'Integer'/>" +
            "<element name='sub' field='subelement'>" +
            "  <element name='two' field='two'>" +
            "  <attribute name='tt' field='ttAttrib'/>" +
            "</element>" +
            "</element>" +
            "</root-element>";

    @Test
    public void basicTest() throws IOException, SAXException {

        StringReader inputData = new StringReader(xml);
        StringReader configData = new StringReader(xmlConfig);

        Xlite xmlXlite = new Xlite(configData);
        Xlite classXlite = new Xlite(Top.class);

        StringWriter swClass = new StringWriter();
        StringWriter swXml = new StringWriter();

        Top cx = (Top) classXlite.fromXML(inputData);
        inputData.reset();
        Top xx = (Top) xmlXlite.fromXML(inputData);

        classXlite.toXML(cx, swClass);
        xmlXlite.toXML(xx, swXml);

        System.out.println(swClass.toString());
        System.out.println(swXml.toString());

        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(swClass.toString(), swXml.toString());

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

        @Text
        public float two;

    }
}
