/*
 * This software is released under the BSD license. Full license available at http://www.xlite.org/license/
 *
 * Copyright (c) 2008, 2009, Peter Knego & Xlite contributors
 * All rights reserved.
 */
package org.xlite;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xlite.converters.ValueConverter;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class XMLtextTest {

    static final String xml1 = "<one>1,2,3</one>";
    static final String xml2 = "<one>0,1,2,3</one>";


    @Test
    public void basicTest() {

        StringReader reader = new StringReader(xml1);
        Configuration conf = new AnnotationConfiguration(XMLTextTest.class, "one");
        conf.setPrettyPrint(false);
        Xlite xlite = new Xlite(conf);

        XMLTextTest one = (XMLTextTest) xlite.fromXML(reader);
        Assert.assertEquals((int) one.getValues().size(), 4);
        Assert.assertEquals((int) one.getValues().get(0), 0);
        Assert.assertEquals((int) one.getValues().get(1), 1);
        Assert.assertEquals((int) one.getValues().get(2), 2);
        Assert.assertEquals((int) one.getValues().get(3), 3);

        Writer w = new StringWriter();
        xlite.toXML(one, w);
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertEquals(xml2, w.toString());
    }

    public static class XMLTextTest {

        @Text(converter = IntegerCollectionConverter.class)
        private List<Integer> values = new ArrayList<Integer>();


        public XMLTextTest() {
            values.add(0);
        }

        public List<Integer> getValues() {
            return values;
        }

        public void setValues(List<Integer> values) {
            this.values = values;
        }
    }

    public static class IntegerCollectionConverter extends ValueConverter {
        public boolean canConvert(Class type) {
            return Collection.class.isAssignableFrom(type);
        }

        public Object fromValue(String value, String format, Class targetType) {
            Collection<Integer> integers = new ArrayList<Integer>();
            if (value != null) {
                String[] values = value.split(",");
                for (String v : values) {
                    integers.add(Integer.valueOf(v));
                }
            }
            return integers;
        }

        public String toValue(Object object, String format) {
            StringBuilder builder = new StringBuilder();
            Collection<Integer> collection = (Collection<Integer>) object;
            for (Integer integer : collection) {
                builder.append(",").append(integer);
            }
            return collection.isEmpty() ? null : builder.substring(1);
        }
    }

}