package org.xlite;

import org.xlite.converters.ClassNameConverter;
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

    public String toString(String space) {

        StringBuilder out = new StringBuilder();
        out.append(space).append("text: ").append("field=").append(field).append("\n");
        out.append(space).append("  ").append("format=").append(format).append("\n");
        out.append(space).append("  ").append("converter=").append(converter.getName()).append("\n");

        return out.toString();
    }
}
