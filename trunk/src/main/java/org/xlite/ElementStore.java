package org.xlite;

/**
 * @author peter
 */
public class ElementStore {
    private ObjectStore store;

    public ElementStore(int size, int increment) {
        this.store = new ObjectStore(size,increment);
    }

    public ObjectStore getStore() {
        return store;
    }

}
