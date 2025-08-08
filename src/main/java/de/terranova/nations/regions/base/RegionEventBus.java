package de.terranova.nations.regions.base;

import java.util.ArrayList;
import java.util.List;

public class RegionEventBus {
  private final List<RegionListener> listeners = new ArrayList<>();

  public void subscribe(RegionListener listener) {
    listeners.add(listener);
  }

  // Notify all listeners of a rename
  public void publishRename(String newCityName) {
    for (RegionListener listener : listeners) {
      listener.onRegionRenamed(newCityName);
    }
  }

  // Notify all listeners of a removal
  public void publishRemoval() {
    for (RegionListener listener : listeners) {
      listener.onRegionRemoved();
    }
  }
}
