package org.xlite;

import org.xlite.converters.ClassNameConverter;
import org.xlite.converters.Converter;

import java.util.List;


@RootElement("root-element")
public class ConfigRootElement {

    @Attribute
    public String name;

    @Attribute(name = "class", converter = ClassNameConverter.class)
    public Class classType;

    @Attribute(converter = ClassNameConverter.class, defaultValue = "org.xlite.converters.ElementConverter")
    public Class<? extends Converter> converter;

    @Element(targetType = ConfigNamespace.class)
    public List<ConfigNamespace> namespace;

    @Element
    public ConfigText text;

    @Element(targetType = ConfigAttribute.class)
    public List<ConfigAttribute> attribute;

    @Element(targetType = ConfigElement.class)
    public List<ConfigElement> element;

    public ConfigRootElement() {
    }

//    @Override
//    public String toString() {
//        String space = "  ";
//        StringBuilder out = new StringBuilder();
//        out.append("ConfigRootElement:  name=").append(name).append("\n");
//
//        if (classType != null)
//            out.append("  classType=").append(classType.getName()).append("\n");
//
//        if (converter != null)
//            out.append("  converter=").append(converter.getName()).append("\n");
//
//        if (text != null)
//            out.append(text.toString(space + "  "));
//
//        if (namespace != null)
//            for (ConfigNamespace configNamespace : namespace) {
//                out.append(configNamespace.toString(space)).append("\n");
//            }
//
//        if (attribute != null)
//            for (ConfigAttribute configAttribute : attribute) {
//                out.append(configAttribute.toString(space)).append("\n");
//            }
//
//        if (element != null)
//            for (ConfigElement configElement : element) {
//                out.append(configElement.toString(space)).append("\n");
//            }
//
//        return out.toString();
//    }
}


