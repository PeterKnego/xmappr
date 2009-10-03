package org.xlite;

import org.xlite.converters.ClassNameConverter;
import org.xlite.converters.ValueConverter;

public class ConfigAttribute {

    @Attribute
    public String name;

    @Attribute
    public String field;

    @Attribute
    public String defaultvalue;

    @Attribute(converter = ClassNameConverter.class)
    public Class targetType;

    @Attribute
    public String format;

    @Attribute(converter = ClassNameConverter.class)
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
}
