package de.terranova.nations.regions.base;

public interface RegionListener {
  void onRegionRenamed(String newRegionName);

  void onRegionRemoved();
}
