package org.xmappr;

import org.xmappr.annotation.Attribute;
import org.xmappr.converters.ClassNameConverter;
import org.xmappr.converters.ValueConverter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ConfigText {

    // This fields are not configurable via mapping configuration.
    // They are derived during the validation phase.
    public Class accessorType;
    private boolean isCollection;
    private Class converterType;
    public Method getterMethod;
    public Method setterMethod;
    public Field targetField;

    @Attribute
    public String field;

    @Attribute
    public String getter;

    @Attribute
    public String setter;

    @Attribute
    public String format;

    @Attribute(converter = ClassNameConverter.class, defaultValue = "java.lang.Object")
    public Class targetType;

    @Attribute(converter = ClassNameConverter.class)
    public Class<? extends ValueConverter> converter;

    public ConfigText() {
    }

    public ConfigText(Class accessorType, Field targetField, String field,boolean isCollection, Class converterType,
                      Method getterMethod, String getterName, Method setterMethod, String setterName,
                      Class targetType, String format, Class<? extends ValueConverter> converter) {
        this.accessorType = accessorType;
        this.targetField = targetField;
        this.field = field;
        this.isCollection = isCollection;
        this.converterType = converterType;
        this.getterMethod = getterMethod;
        this.getter = getterName;
        this.setterMethod = setterMethod;
        this.setter = setterName;

        this.targetType = targetType;
        this.format = format;
        this.converter = converter;
    }

    public String toString(String space) {

        StringBuilder out = new StringBuilder();
        out.append(space).append("<text ").append("field=").append(field).append("\n");
        out.append(space).append("  ").append("getter=").append(getter).append("\n");
        out.append(space).append("  ").append("setter=").append(setter).append("\n");
        out.append(space).append("  ").append("format=").append(format).append("\n");
        out.append(space).append("  ").append("targetType=").append(targetType).append("\n");
        out.append(space).append("  ").append("converter=").append(converter).append(" />\n");

        return out.toString();
    }

    @Override
    public String toString() {
        return this.toString("");
    }
}
