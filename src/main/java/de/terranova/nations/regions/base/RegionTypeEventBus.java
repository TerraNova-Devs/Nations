package de.terranova.nations.regions.base;

import java.util.ArrayList;
import java.util.List;

public class RegionTypeEventBus {
    private List<RegionTypeListener> listeners = new ArrayList<>();

    public void subscribe(RegionTypeListener listener) {
        listeners.add(listener);
    }

    // Notify all listeners of a rename
    public void publishRename(String newCityName) {
        for (RegionTypeListener listener : listeners) {
            listener.onRegionTypeRenamed(newCityName);
        }
    }

    // Notify all listeners of a removal
    public void publishRemoval() {
        for (RegionTypeListener listener : listeners) {
            listener.onRegionTypeRemoved();
        }
    }
}
