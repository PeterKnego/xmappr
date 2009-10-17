package org.xlite;

import org.testng.annotations.Test;

import java.io.StringReader;

public class ConverterNotFoundTest {


    private static String xml = "" +
            "<top>" +
            " <sub>" +
            "  <two >some content</two>" +
            " </sub>" +
            "</top>";

    @Test(expectedExceptions = XliteConfigurationException.class)
    public void basicTest() {
        Xlite xlite = new Xlite(Top.class);
        xlite.fromXML(new StringReader(xml));
    }

    @RootElement
    public static class Top {

        @Element("sub")
        public Sub subelement;

    }

    /**
     * This class contains no XML mapping annotations, so it wont be processed.
     */
    public static class Sub {

        public String text;
    }
}
