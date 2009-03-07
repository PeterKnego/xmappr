package org.xlite;

import org.xlite.converters.RootMapper;

/**
 * Created by IntelliJ IDEA.
 * User: peter
 * Date: Feb 23, 2009
 * Time: 1:49:36 PM
 * To change this template use File | Settings | File Templates.
 */

public interface Configuration {
    RootMapper getRootElementMapper();

    public void initialize();

    boolean isPrettyPrint();

    void setPrettyPrint(boolean prettyPrint);

    boolean isStoringUnknownElements();

    void setStoringUnknownElements(boolean storing);

    void setCacheSize(int sizeBytes);

    void addNamespace(String namespace);

}
