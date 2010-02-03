package org.xmappr;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

public class XmlConfigParseTest {

    private static String xmlConfig = "" +
            "<root-element name='top' class='org.xmappr.XmlConfigParseTest$Top'>" +
            "<text field='sometext'/>" +
            "<attribute name='attr1' field='a1'/>" +
            "<attribute name='attr2' field='a2'/>" +
            "<element name='one' field='one' targetType = 'Integer'/>" +
            "<element name='sub' field='subelement'>" +
            "  <element name='two' field='two'>" +
            "    <attribute name='tt' field='ttAttrib'/>" +
            "    <text field='text'/>" +
            "  </element>" +
            "</element>" +
            "</root-element>";

    private static String xml = "" +
            "<top attr1=\"11\" attr2=\"twenty\">" +
            "some text" +
            "<one>1</one>" +
            "<one>2</one>" +
            "<sub>" +
            "<two tt='5'>1.123</two>" +
            "</sub>" +
            "</top>";

    @Test
    public void basicTest() throws IOException, SAXException {
        StringReader inputData = new StringReader(xml);
        StringReader configData = new StringReader(xmlConfig);

        Xmappr xmlXmappr = new Xmappr(configData);
        xmlXmappr.setPrettyPrint(true);
        Xmappr classXmappr = new Xmappr(Top.class);
        classXmappr.setPrettyPrint(true);

        StringWriter swClass = new StringWriter();
        StringWriter swXml = new StringWriter();

        Top cx = (Top) classXmappr.fromXML(inputData);
        inputData.reset();
        Top xx = (Top) xmlXmappr.fromXML(inputData);

        classXmappr.toXML(cx, swClass);
        xmlXmappr.toXML(xx, swXml);
    }

    @RootElement
    public static class Top {

        @Text
        public String sometext;

        @Attribute("attr1")
        public int a1;

        @Attribute("attr2")
        public String a2;

        @Element(targetType = Integer.class)
        public List one;

        @Element("sub")
        public Sub subelement;

    }

    public static class Sub {

        @Element
        public Two two;
    }

    public static class Two {

        @Attribute("tt")
        public int ttAttrib;

        @Text
        public String text;
    }

}
