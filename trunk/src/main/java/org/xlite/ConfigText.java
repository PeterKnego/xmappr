package org.xlite;

import org.xlite.converters.ClassNameConverter;
import org.xlite.converters.Converter;
import org.xlite.converters.ValueConverter;

public class ConfigText {

    @Attribute
    public String field;

    @Attribute
    public String format;

    @Attribute(converter = ClassNameConverter.class)
    public Class<? extends ValueConverter> converter;

    public ConfigText() {
    }

    public ConfigText(String fieldName, String format, Class<? extends ValueConverter> converter) {
        this.field = fieldName;
        this.format = format;
        this.converter = converter;
    }
}
