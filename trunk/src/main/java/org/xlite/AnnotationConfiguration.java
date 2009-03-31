package org.xlite;

import org.xlite.converters.*;

import javax.xml.namespace.QName;
import javax.xml.XMLConstants;
import java.util.List;
import java.util.ArrayList;

public class AnnotationConfiguration implements Configuration {

    private RootMapper rootElementMapper;

    private MappingContext mappingContext;

    private List<ElementConverter> elementConverters;

    private List<ValueConverter> valueConverters;

    private boolean initialized = false;

    private Class rootClass;

    private String rootElementName;

    private String rootElementNS = XMLConstants.NULL_NS_URI;

    private boolean isPrettyPrint = true;

    public AnnotationConfiguration(Class rootClass, String nodeName) {
        this(rootClass, nodeName, null);
    }

    public AnnotationConfiguration(Class rootClass, String nodeName, String namespaceURI) {
        setupValueConverters();
        setupElementConverters();
        this.rootClass = rootClass;
        this.rootElementName = nodeName;
        this.rootElementNS = namespaceURI;
        this.mappingContext = new MappingContext(elementConverters, valueConverters);
    }

    public RootMapper getRootElementMapper() {
        return rootElementMapper;
    }

    public boolean isPrettyPrint() {
        return isPrettyPrint;
    }

    public void setPrettyPrint(boolean prettyPrint) {
        this.isPrettyPrint = prettyPrint;
    }

    public void initialize() {

        // one-time initialization
        if (!initialized) {

            // split xml node name into prefix and local part
            int index = rootElementName.indexOf(':');
            String rootElementLocalpart;
            String rootElementPrefix;
            if (index > 0) {  // with prefix ("prefix:localpart")
                rootElementPrefix = rootElementName.substring(0, index);
                rootElementLocalpart = rootElementName.substring(index + 1, rootElementName.length());

            } else if (index == 0) { // empty prefix (no prefix defined - e.g ":nodeName")
                rootElementPrefix = XMLConstants.DEFAULT_NS_PREFIX;
                rootElementLocalpart = rootElementName.substring(1, rootElementName.length());

            } else { // no prefix given
                rootElementPrefix = XMLConstants.DEFAULT_NS_PREFIX;
                rootElementLocalpart = rootElementName;
            }

            // namespace  of root element is not defined
            if (rootElementNS == null) {
                rootElementNS = mappingContext.getPredefinedNamespaces().getNamespaceURI(rootElementPrefix);
            }
            this.rootElementMapper = new RootMapper(new QName(rootElementNS, rootElementLocalpart, rootElementPrefix), rootClass, mappingContext);
            initialized = true;
        }
    }

    private void setupElementConverters() {
        elementConverters = new ArrayList<ElementConverter>();
        elementConverters.add(new CollectionConverter());

        // wraps every ValueConverter so that it can be used as a ElementConverter
        for (ValueConverter valueConverter : valueConverters) {
            elementConverters.add(new ValueConverterWrapper(valueConverter));
        }
    }

    private void setupValueConverters() {
        valueConverters = new ArrayList<ValueConverter>();

        valueConverters.add(new StringConverter());
        valueConverters.add(new IntConverter());
        valueConverters.add(new DoubleConverter());
        valueConverters.add(new FloatConverter());
        valueConverters.add(new LongConverter());
        valueConverters.add(new ShortConverter());
        valueConverters.add(new BooleanConverter());
        valueConverters.add(new ByteConverter());
        valueConverters.add(new CharConverter());

    }

    public void addNamespace(String namespace) {
        mappingContext.addNamespace(namespace);
    }

}