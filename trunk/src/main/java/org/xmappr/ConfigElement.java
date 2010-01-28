package org.xmappr;

import org.xmappr.converters.ClassNameConverter;
import org.xmappr.converters.Converter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class ConfigElement {

    // This fields are not configurable via mapping configuration.
    // They are derived during the validation phase.
    public Class baseType;
    public boolean fromAnnotation;
    public Method getterMethod;
    public Method setterMethod;
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

    public ConfigElement(boolean fromAnnotation, String name, Class baseType, Field targetField, String field,
                         Method getterMethod, String getterName, Method setterMethod, String setterName,
                         String defaultValue, Class targetType, String format, Class<? extends Converter> converter,
                         List<ConfigNamespace> namespaces) {
        this.fromAnnotation = fromAnnotation;
        this.baseType = baseType;
        this.targetField = targetField;
        this.field = field;
        this.getterMethod = getterMethod;
        this.getter = getterName;
        this.setterMethod = setterMethod;
        this.setter = setterName;

        this.converter = converter;
        this.defaultvalue = defaultValue;
        this.format = format;
        this.name = name;
        this.targetType = targetType;
        this.namespace = namespaces;
    }

    public String toString(String space) {
        StringBuilder out = new StringBuilder();
        out.append(space).append("<element ").append("name=").append(name).append("\n");
        out.append(space).append("  ").append("fromAnnotation=").append(fromAnnotation).append("\n");
        out.append(space).append("  ").append("field=").append(field).append("\n");
        out.append(space).append("  ").append("getter=").append(getter).append("\n");
        out.append(space).append("  ").append("setter=").append(setter).append("\n");
        out.append(space).append("  ").append("defaultValue=").append(defaultvalue).append("\n");
        out.append(space).append("  ").append("format=").append(format).append("\n");

        if (targetType != null)
            out.append(space).append("  ").append("targetType=").append(targetType.getName()).append("\n");

        if (converter != null)
            out.append(space).append("  ").append("converter=").append(converter.getName()).append("\n");

        if (text != null)
            out.append(text.toString(space + "  "));

        if (namespace != null)
            for (ConfigNamespace configNamespace : namespace) {
                out.append(configNamespace.toString(space + "  ")).append("\n");
            }

        if (attribute != null)
            for (ConfigAttribute configAttribute : attribute) {
                out.append(configAttribute.toString(space + "  ")).append("\n");
            }

        if (element != null)
            for (ConfigElement configElement : element) {
                out.append(configElement.toString(space + "  ")).append("\n");
            }
        out.append(space).append("/>");
        return out.toString();
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("<element ").append("name=").append(name).append("\n");
        out.append("  ").append("field=").append(field).append("\n");
        out.append("  ").append("defaultValue=").append(defaultvalue).append("\n");
        out.append("  ").append("format=").append(format).append(" />\n");
        return out.toString();
    }
}
