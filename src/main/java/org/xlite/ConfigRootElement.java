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

    @Attribute(converter = ClassNameConverter.class)
    public Class<? extends Converter> converter;

    @Element(itemType = ConfigNamespace.class)
    public List<ConfigNamespace> namespace;

    @Element
    public ConfigText text;

    @Element(itemType = ConfigAttribute.class)
    public List<ConfigAttribute> attribute;

    @Element(itemType = ConfigElement.class)
    public List<ConfigElement> element;

    public ConfigRootElement() {
    }
}


