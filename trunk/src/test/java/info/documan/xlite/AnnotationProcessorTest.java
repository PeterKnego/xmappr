package info.documan.xlite;

import org.testng.annotations.Test;

import java.io.StringReader;

import info.documan.xlite.Xlite;

/**
 * @author peter
 */
public class AnnotationProcessorTest {

    @Test
    public void printMapperTree() {
        StringReader reader = new StringReader(SampleXml.xml);
        Xlite xf = new Xlite(SampleXml.One.class, "one");
    }
   

}
