package org.xmappr;

import org.xmappr.annotation.Attribute;
import org.xmappr.converters.ClassNameConverter;
import org.xmappr.converters.ValueConverter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ConfigAttribute {

    // This fields are not configurable via mapping configuration.
    // They are derived during the validation phase.
    public Class accessorType;
    public Method getterMethod;
    public Method setterMethod;
    private boolean isMap;
    public Field targetField;

    @Attribute
    public String name;

    @Attribute
    public String field;

    @Attribute
    public String getter;

    @Attribute
    public String setter;

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

    public ConfigAttribute(String attributeName, Class accessorType, boolean isMap, Field targetField, String field,
                           Method getterMethod, String getterName, Method setterMethod, String setterName,
                           String defaultValue, Class targetType,
                           String format, Class<? extends ValueConverter> converter) {
        this.accessorType = accessorType;
        this.isMap = isMap;
        this.targetField = targetField;
        this.field = field;
        this.getterMethod = getterMethod;
        this.getter = getterName;
        this.setterMethod = setterMethod;
        this.setter = setterName;

        this.converter = converter;
        this.defaultvalue = defaultValue;
        this.format = format;
        this.name = attributeName;
        this.targetType = targetType;
    }

    public String toString(String space) {
        StringBuilder out = new StringBuilder();
        out.append(space).append("<attribute ").append("name=").append(name).append("\r\n");
        out.append(space).append("  ").append("field=").append(field).append("\n");
        out.append(space).append("  ").append("getter=").append(getter).append("\n");
        out.append(space).append("  ").append("setter=").append(setter).append("\n");
        out.append(space).append("  ").append("defaultValue=").append(defaultvalue).append("\n");
        out.append(space).append("  ").append("format=").append(format).append("\n");
        if (targetType != null)
            out.append(space).append("  ").append("targetType=").append(targetType.getName()).append("\n");
        if (converter != null)
            out.append(space).append("  ").append("converter=").append(converter.getName()).append(" />\n");

        return out.toString();
    }

    @Override
    public String toString() {
        return this.toString("");
    }
}
