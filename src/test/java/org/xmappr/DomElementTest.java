package org.xmappr;

import org.testng.Assert;
import org.testng.annotations.Test;

import javax.xml.namespace.QName;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

public class DomElementTest {

    private static String xml = "" +
            "<customer>\n" +
            "  <contact>\n" +
            "    <firstname>Joe</firstname>\n" +
            "    <lastname>Somebody</lastname>\n" +
            "  </contact>\n" +
            "  <address>\n" +
            "    <street>Streetname</street>just some text inside<city>Big City</city>\n" +
            "  </address>\n" +
            "<one>1</one>" +
            "<two>2</two>" +
            "<three>3</three>" +
            "</customer>";

    @Test
    public void test() {
        Reader reader = new StringReader(xml);
        Xmappr xmappr = new Xmappr(Customer.class);

        Customer customer = (Customer) xmappr.fromXML(reader);
        asserts(customer);
    }

    @Test
    public void testViaXML() {
        Reader reader = new StringReader(xml);

        // Double step to make Xmappr work harder (not necessary normally - do not copy)
        // Reads Class configuration, produces XML configuration from it and then feeds it to Xmappr
        StringReader configuration = XmlConfigTester.reader(Customer.class);
        Xmappr xmappr = new Xmappr(configuration);

        Customer customer = (Customer) xmappr.fromXML(reader);
        asserts(customer);
    }

    private void asserts(Customer customer) {
        // <address> has two subelements
        Assert.assertEquals(customer.address.getElements().size(), 3);
        // first node is <street>
        Assert.assertEquals(((DomElement) customer.address.getElements().get(0)).getName(), new QName("street"));
        // second node is XML text
        Assert.assertEquals(customer.address.getElements().get(1), "just some text inside");
        // third node is <city>
        Assert.assertEquals(((DomElement) customer.address.getElements().get(2)).getName(), new QName("city"));
    }


    @RootElement
    public static class Customer {
        @Element
        public Contact contact;

        @Element
        public DomElement address;

        @Element("*")
        public List otherElements;
    }

    public static class Contact {
        @Element
        public String firstName;
        @Element
        public String lastName;
    }

}
