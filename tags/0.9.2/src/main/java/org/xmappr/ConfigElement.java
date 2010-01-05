package org.xmappr;

import org.xmappr.converters.ClassNameConverter;
import org.xmappr.converters.Converter;

import java.util.List;

public class ConfigElement {

    @Attribute
    public String name;

    @Attribute
    public String field;

    @Attribute
    public String defaultvalue;

    @Attribute(converter = ClassNameConverter.class, defaultValue = "java.lang.Object")
    public Class targetType;

    @Attribute
    public String format;

    @Attribute(converter = ClassNameConverter.class, defaultValue = "org.xmappr.converters.ElementConverter")
    public Class<? extends Converter> converter;

    @Element
    public List<ConfigNamespace> namespace;

    @Element
    public ConfigText text;

    @Element
    public List<ConfigAttribute> attribute;

    @Element
    public List<ConfigElement> element;

    public ConfigElement() {
    }

    public ConfigElement(String name, String field, String defaultValue,
                         Class targetType, String format, Class<? extends Converter> converter) {
        this.converter = converter;
        this.defaultvalue = defaultValue;
        this.field = field;
        this.format = format;
        this.name = name;
        this.targetType = targetType;
    }

//    public String toString(String space) {
//        StringBuilder out = new StringBuilder();
//        out.append(space).append("element: ").append("name=").append(name).append("\n");
//        out.append(space).append("  ").append("field=").append(field).append("\n");
//        out.append(space).append("  ").append("defaultValue=").append(defaultvalue).append("\n");
//        out.append(space).append("  ").append("format=").append(format).append("\n");
//
//        if (targetType != null)
//            out.append(space).append("  ").append("targetType=").append(targetType.getName()).append("\n");
//
//        if (converter != null)
//            out.append(space).append("  ").append("converter=").append(converter.getName()).append("\n");
//
//        if (text != null)
//            out.append(text.toString(space + "  "));
//
//        if (namespace != null)
//            for (ConfigNamespace configNamespace : namespace) {
//                out.append(configNamespace.toString(space + "  ")).append("\n");
//            }
//
//        if (attribute != null)
//            for (ConfigAttribute configAttribute : attribute) {
//                out.append(configAttribute.toString(space + "  ")).append("\n");
//            }
//
//        if (element != null)
//            for (ConfigElement configElement : element) {
//                out.append(configElement.toString(space + "  ")).append("\n");
//            }
//        return out.toString();
//    }
}
