package org.xmappr;

import org.testng.annotations.Test;

import java.io.StringReader;

public class WrongTargetObject {

    private static String inXml = "<root>" +
            "<a>text1</a>" +
            "</root>";

    @Test
    public void testCorrect() {
        StringReader reader = new StringReader(inXml);

        // Double step to make Xmappr work harder (not necessary normally - do not copy)
        // Reads Class configuration, produces XML configuration from it and then feeds it to Xmappr
        StringReader configuration = XmlConfigTester.reader(Root.class);
        Xmappr xmappr = new Xmappr(configuration);
        xmappr.setPrettyPrint(false);

        Root root = new Root();
        xmappr.fromXML(reader, root);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testWrong() {
        StringReader reader = new StringReader(inXml);

        // Double step to make Xmappr work harder (not necessary normally - do not copy)
        // Reads Class configuration, produces XML configuration from it and then feeds it to Xmappr
        StringReader configuration = XmlConfigTester.reader(Root.class);
        Xmappr xmappr = new Xmappr(configuration);
        xmappr.setPrettyPrint(false);

        Object root = new Object();
        xmappr.fromXML(reader, root);  // object of wrong type passed
    }

    @RootElement
    public static class Root {

        @Element
        public String a;

    }

}
