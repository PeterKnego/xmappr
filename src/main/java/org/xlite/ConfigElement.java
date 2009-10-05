package org.xlite;

import org.xlite.converters.ClassNameConverter;
import org.xlite.converters.Converter;

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

    @Attribute(converter = ClassNameConverter.class, defaultValue = "org.xlite.converters.ElementConverter")
    public Class<? extends Converter> converter;

    @Element(itemType = ConfigNamespace.class)
    public List<ConfigNamespace> namespace;

    @Element
    public ConfigText text;

    @Element(itemType = ConfigAttribute.class)
    public List<ConfigAttribute> attribute;

    @Element(itemType = ConfigElement.class)
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
}
