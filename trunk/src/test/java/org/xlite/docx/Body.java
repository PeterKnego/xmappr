package org.xlite.docx;


import org.xlite.XMLelement;

import java.util.List;

/**
 * User: peter
 * Date: Dec 30, 2007
 * Time: 11:08:01 PM
 */

public class Body {

    @XMLelement(value = "w:p", itemType = Paragraph.class)
    public List<Paragraph> paragraphs;


}
