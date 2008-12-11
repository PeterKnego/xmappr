package org.xlite.docx;

import org.xlite.XMLelement;

import java.util.List;

/**
 * User: peter
 * Date: Dec 27, 2007
 * Time: 3:07:49 PM
 */
public class Paragraph {

    @XMLelement(value = "w:r", itemType = Run.class)
    public List<Run> runs;

}
