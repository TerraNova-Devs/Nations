package de.terranova.nations.regions.modules;

import de.terranova.nations.regions.base.Region;

public interface HasParent<T extends Region> {
    T getParent();
    void setParentRaw(T parent);

    default void setParent(T parent) {
        setParentRaw(parent);

        if (parent instanceof HasChildren hasChildren) {
            hasChildren.addChild((Region) this);
        }
    }
}
