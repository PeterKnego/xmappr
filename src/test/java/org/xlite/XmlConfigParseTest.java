package org.xlite;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.StringReader;
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
            "<rootelement class='org.xlite.XmlConfigParseTest$Top'>" +
            "<attribute name='attr1' field='a1'/>" +
            "<attribute name='attr2' field='a2'/>" +
            "<element name='one' field='one' itemType = 'Integer'/>" +
            "<element name='sub' field='subelement'>" +
            "  <attribute name='tt' field='ttAttrib'/>" +
            "  <element name='two' field='two'/>" +
            "</element>" +
            "</rootelement>";

    @Test
    public void basicTest() {

        StringReader reader = new StringReader(xml);
        StringReader confReader = new StringReader(xmlConfig);
        ConfigRootElement xmlConf = ConfigurationProcessor.processConfiguration(confReader);
        ConfigRootElement classConf = ConfigurationProcessor.processConfiguration(Top.class);

        System.out.println("");
        Assert.assertTrue(xmlConf.equals(classConf));
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
