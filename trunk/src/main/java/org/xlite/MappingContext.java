package org.xlite;

import org.xlite.converters.ElementConverter;
import org.xlite.converters.ValueConverter;

import javax.xml.namespace.QName;
import java.util.List;

//todo write javadoc - IMPORTANT, as this is one of the core classes

/**
 * @author peter
 */
public class MappingContext {

    public List<ElementConverter> elementConverters;
    private List<ValueConverter> valueConverters;
    private AnnotationProcessor annotationProcessor;
    private NsContext predefinedNamespaces = new NsContext();

    public MappingContext(List<ElementConverter> elementConverters, List<ValueConverter> valueConverters) {
        this.elementConverters = elementConverters;
        this.valueConverters = valueConverters;
        annotationProcessor = new AnnotationProcessor(this);
    }

    public NsContext getPredefinedNamespaces() {
        return predefinedNamespaces;
    }

    public void addNamespace(String namespace){
        predefinedNamespaces.addNamespace(namespace);
    }

    /**
     * Delegates processing of the next XML element in the reader to the appropriate converter. It finds the right
     * ElementConverter for the given Class and uses it to process current XML element.<br><br>
     * It's mainly used by custom converters to forward processing of a subelement to appropriate converter.
     * @param targetType
     * @param reader
     * @return
     */
    public Object processNextElement(Class targetType, XMLSimpleReader reader) {
        // find the converter for given Class
        ElementConverter converter = lookupElementConverter(targetType);
        return converter.fromElement(reader, this, "");
    }

    public ValueConverter lookupValueConverter(Class type) {
        for (ValueConverter valueConverter : valueConverters) {
            if (valueConverter.canConvert(type)) {
                return valueConverter;
            }
        }
        return null;
    }

    /**
     * Finds the appropriate ElementConverter for the given Class among the registered ElementConverters. If none
     *  is found, an instance of AnnotatedClassConverter is returned.
     * @param type
     * @return
     */
    public ElementConverter lookupElementConverter(Class type) {
        for (ElementConverter elementConverter : elementConverters) {
            if (elementConverter.canConvert(type)) {
                return elementConverter;
            }
        }
        return annotationProcessor.processClass(type);
    }
}
