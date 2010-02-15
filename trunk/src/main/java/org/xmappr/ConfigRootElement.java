package org.xmappr;

import org.xmappr.annotation.Attribute;
import org.xmappr.annotation.Element;
import org.xmappr.annotation.RootElement;
import org.xmappr.converters.ClassNameConverter;
import org.xmappr.converters.Converter;

import java.util.List;


@RootElement("root-element")
public class ConfigRootElement {

    public boolean fromAnnotation;
    
    @Attribute
    public String name;

    @Attribute(name = "class", converter = ClassNameConverter.class)
    public Class classType;

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

    public ConfigRootElement() {
    }

    @Override
    public String toString() {
        String space = "  ";
        StringBuilder out = new StringBuilder();
        out.append("<root-element>  name=").append(name).append("\n");

        if (classType != null)
            out.append(space).append("classType=").append(classType.getName()).append("\n");

        if (converter != null)
            out.append(space).append("converter=").append(converter.getName()).append("\n");

        if (text != null)
            out.append(space).append(text.toString(space + "  "));

        if (namespace != null)
            for (ConfigNamespace configNamespace : namespace) {
                out.append(configNamespace.toString(space)).append("\n");
            }

        if (attribute != null)
            for (ConfigAttribute configAttribute : attribute) {
                out.append(configAttribute.toString(space)).append("\n");
            }

        if (element != null)
            for (ConfigElement configElement : element) {
                out.append(configElement.toString(space)).append("\n");
            }
        out.append("/>");

        return out.toString();
    }
}


