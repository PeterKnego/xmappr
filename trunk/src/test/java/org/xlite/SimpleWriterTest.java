package org.xlite;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.xlite.Xlite;
import org.xlite.XMLattribute;
import org.xlite.XMLnamespaces;
import org.xlite.XMLelement;

/**
 * @author peter
 */
public class SimpleWriterTest {

    private static String xml1 = "<a xmlns=\"ns1\" xmlns:sec=\"ns2\">\n" +
            "<sec:b>\n" +
            "<c>\n" +
            "<d attr=\"DDD\" />\n" +
            "</c>\n" +
            "</sec:b>\n" +
            "</a>";

    @org.testng.annotations.Test
    public void testEquality() throws XMLStreamException, IOException, SAXException {
        StringReader reader = new StringReader(xml1);

        Configuration conf = new AnnotationConfiguration(A.class, "a","ns1");
        conf.addNamespace("ns1");

        Xlite xlite = new Xlite(conf);
        A a = (A) xlite.fromXML(reader);
//        System.out.println(xml1);

        // writing back to XML
        StringWriter sw = new StringWriter();
        xlite.toXML(a, sw);
//        System.out.println(sw);
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(xml1, sw.toString());
    }

    public static class A {
        @XMLnamespaces("s=ns2")
        @XMLelement("s:b")
        public B b;
    }

    //    @XMLnamespaces("ns2")
    public static class B {
        @XMLelement
        public C c;

        @XMLelement
        public C c2;
    }

    public static class C {
        @XMLelement
        public D d;
    }

    public static class D {
        @XMLattribute
        public String attr;
    }

}
