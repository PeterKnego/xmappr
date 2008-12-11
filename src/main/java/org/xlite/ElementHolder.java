package org.xlite;

/**
 * @author peter
 */
public class ElementHolder {
    private SubTreeStore store;

    public ElementHolder(int size, int increment) {
        this.store = new SubTreeStore(size,increment);
    }

    public SubTreeStore getStore() {
        return store;
    }
}
