package org.xmappr;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.testng.annotations.Test;
import org.xmappr.converters.ElementConverter;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

public class CustomConverterOnCollectionTest {

    public static String xml = "<users>\n" +
            "    <user>\n" +
            "        <id>1</id>\n" +
            "        <main>test</main>\n" +
            "        <meta type=\"String\" key=\"wtf\">just for fun</meta>\n" +
            "        <meta type=\"Integer\" key=\"age\">99</meta>\n" +
            "    </user>\n" +
            "    <user>\n" +
            "        <id>2</id>\n" +
            "        <main>test</main>\n" +
            "        <meta type=\"String\" key=\"wtf\">fooooo</meta>\n" +
            "        <meta type=\"Integer\" key=\"age\">101</meta>\n" +
            "    </user>\n" +
            "</users>";

    @Test
    public void test() throws IOException, SAXException {
        Xmappr xmappr = new Xmappr(Users.class);
        xmappr.setPrettyPrint(true);
        Users root = (Users) xmappr.fromXML(new StringReader(xml));

        StringWriter sw = new StringWriter();
        xmappr.toXML(root, sw);

        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(xml, sw.toString());
    }

    @RootElement("users")
    public static class Users {
        @Element(name = "user", converter = UserCompoundConverter.class)
        public List<UserCompound> users;
    }

    public static class UserCompound {
        public User user;

        public Map<String, UserMeta> meta;
    }

    public static class User {
        public Integer id;
        public String mainName;
    }

    public static class UserMeta extends Meta {
        //nothing here
    }

    public static class Meta {
        public String key;
        public Object value;
        public String type; //String, Integer, Boolean, Date
    }

    public static class UserCompoundConverter implements ElementConverter {

        public boolean canConvert(Class type) {
            return UserCompound.class.isAssignableFrom(type);
        }

        public Object fromElement(XMLSimpleReader reader, MappingContext mappingContext, String defaultValue,
                                  String format, Class targetType, Object targetObject) {

            UserCompound userCompound = new UserCompound();
            userCompound.user = new User();

            QName qname;
            while (reader.moveDown()) {

                qname = reader.getName();
                if (qname.getLocalPart().equals("id")) {
                    userCompound.user.id = Integer.valueOf(reader.getText());
                } else if (qname.getLocalPart().equals("main")) {
                    userCompound.user.mainName = reader.getText();
                } else if (qname.getLocalPart().equals("meta")) {
                    UserMeta userMeta = new UserMeta();
                    userMeta.type = reader.getAttribute(new QName("type"));
                    userMeta.key = reader.getAttribute(new QName("key"));
                    userMeta.value = reader.getText();
                    if (userCompound.meta == null) {
                        userCompound.meta = new LinkedHashMap<String, UserMeta>();
                    }
                    userCompound.meta.put(userMeta.key, userMeta);
                }

                reader.moveUp();
            }
            return userCompound;
        }

        public void toElement(Object object, QName nodeName, XMLSimpleWriter writer, MappingContext mappingContext,
                              String defaultValue, String format) {
            UserCompound user = (UserCompound) object;

            writer.startElement(new QName("user"));

            writer.startElement(new QName("id"));
            writer.addText(user.user.id.toString());
            writer.endElement();

            writer.startElement(new QName("main"));
            writer.addText(user.user.mainName);
            writer.endElement();

            for (UserMeta userMeta : user.meta.values()) {
                writer.startElement(new QName("meta"));
                writer.addAttribute(new QName("type"), userMeta.type);
                writer.addAttribute(new QName("key"), userMeta.key);
                writer.addText(userMeta.value.toString());
                writer.endElement();
            }

            // end element "user"
            writer.endElement();

        }
    }
}
