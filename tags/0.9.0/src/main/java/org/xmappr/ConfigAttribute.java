package org.xmappr;

import org.xmappr.converters.ClassNameConverter;
import org.xmappr.converters.ValueConverter;

public class ConfigAttribute {

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

    @Attribute(converter = ClassNameConverter.class, defaultValue = "org.xmappr.converters.ValueConverter")
    public Class<? extends ValueConverter> converter;

    public ConfigAttribute() {
    }

    public ConfigAttribute(String name, String field, String defaultValue,
                           Class targetType, String format, Class<? extends ValueConverter> converter) {
        this.converter = converter;
        this.defaultvalue = defaultValue;
        this.field = field;
        this.format = format;
        this.name = name;
        this.targetType = targetType;
    }

//    public String toString(String space) {
//        StringBuilder out = new StringBuilder();
//        out.append(space).append("attribute: ").append("name=").append(name).append("\n");
//        out.append(space).append("  ").append("field=").append(field).append("\n");
//        out.append(space).append("  ").append("defaultValue=").append(defaultvalue).append("\n");
//        out.append(space).append("  ").append("format=").append(format).append("\n");
//        out.append(space).append("  ").append("targetType=").append(targetType.getName()).append("\n");
//        out.append(space).append("  ").append("converter=").append(converter.getName()).append("\n");
//
//        return out.toString();
//    }
}
