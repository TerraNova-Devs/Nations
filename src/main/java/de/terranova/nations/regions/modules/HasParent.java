package de.terranova.nations.regions.modules;

import de.terranova.nations.regions.base.Region;

public interface HasParent<T extends Region> {
    T getParent();
}
