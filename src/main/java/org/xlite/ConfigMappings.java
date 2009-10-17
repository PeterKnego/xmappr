package org.xlite;

import java.util.ArrayList;
import java.util.List;

@RootElement("xlite-mappings")
public class ConfigMappings {

    @Elements({
            @Element(name = "root-element", targetType = ConfigRootElement.class),
            @Element(name = "element", targetType = ConfigElement.class)
    })
    List mappings = new ArrayList();
}
