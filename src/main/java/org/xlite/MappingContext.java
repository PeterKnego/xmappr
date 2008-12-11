package org.xlite;

import org.xlite.converters.ElementConverter;
import org.xlite.converters.ValueConverter;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * @author peter
 */
public class MappingContext {

    public List<ElementConverter> elementConverters;
    private List<ValueConverter> valueConverters;
    private AnnotationProcessor annotationProcessor;
    private NsContext predefinedNamespaces = new NsContext();

    private SubTreeStore elementStore;

    public MappingContext(List<ElementConverter> elementConverters, List<ValueConverter> valueConverters, Class rootClass) {
        this.elementConverters = elementConverters;
        this.valueConverters = valueConverters;
        annotationProcessor = new AnnotationProcessor(this);
    }

    public SubTreeStore getElementStore() {
        return elementStore;
    }

    public void setElementStore(SubTreeStore elementStore) {
        this.elementStore = elementStore;
    }

    public NsContext getPredefinedNamespaces() {
        return predefinedNamespaces;
    }

    public void addNamespace(String namespace){
        predefinedNamespaces.addNamespace(namespace);
    }

    public Object processNextElement(Class targetType, XMLSimpleReader reader) {
        // find the converter for given Class
        ElementConverter converter = lookupElementConverter(targetType);
        return converter.fromElement(reader, this);
    }


    public void processNextObject(Object object, QName elementName, XMLSimpleWriter writer) {
        // find the converter for given Object
        ElementConverter converter = lookupElementConverter(object.getClass());
        converter.toElement(object, elementName, writer, this);
    }

    public ValueConverter lookupValueConverter(Class type) {
        for (ValueConverter valueConverter : valueConverters) {
            if (valueConverter.canConvert(type)) {
                return valueConverter;
            }
        }
        return null;
    }

    public ElementConverter lookupElementConverter(Class type) {
        for (ElementConverter elementConverter : elementConverters) {
            if (elementConverter.canConvert(type)) {
                return elementConverter;
            }
        }
        return annotationProcessor.processClass(type);
    }
}
