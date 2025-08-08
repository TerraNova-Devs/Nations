package de.terranova.nations.regions.modules;

import de.terranova.nations.regions.base.Region;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface HasChildren {
  Map<String, List<Region>> getChildrenMap();

  default List<Region> getChildrenByType(String type) {
    return getChildrenMap().getOrDefault(type, List.of());
  }

  default void addChild(Region region) {
    getChildrenMap().computeIfAbsent(region.getType(), k -> new ArrayList<>()).add(region);
  }

  default boolean hasChildrenOfType(String type) {
    return !getChildrenByType(type).isEmpty();
  }
}
