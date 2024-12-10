package de.terranova.nations.regions.base;

public interface RegionTypeListener {
    default void onRegionTypeRenamed(String newRegionName){

    };
    default void onRegionTypeRemoved() {

    };
}
