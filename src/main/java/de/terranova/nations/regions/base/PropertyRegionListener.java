package de.terranova.nations.regions.base;

public interface PropertyRegionListener extends RegionListener {
    /**
     * Called when a property is added to a region.
     *
     * @param property The added property region.
     */
    void onPropertyAdded(Region property);

    /**
     * Called when a property is removed from a region.
     *
     * @param property The removed property region.
     */
    void onPropertyRemoved(Region property);
}
