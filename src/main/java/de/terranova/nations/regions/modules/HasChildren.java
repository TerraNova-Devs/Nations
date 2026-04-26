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
    getChildrenMap()
            .computeIfAbsent(region.getType(), k -> new ArrayList<>())
            .add(region);
  }

  default void removeChild(Region region) {
    List<Region> children = getChildrenMap().get(region.getType());
    if (children == null) return;

    children.removeIf(child -> child.getId().equals(region.getId()));

    if (children.isEmpty()) {
      getChildrenMap().remove(region.getType());
    }
  }

  default boolean hasChildrenOfType(String type) {
    return !getChildrenByType(type).isEmpty();
  }

  default boolean hasChildren() {
    return getChildrenMap().values().stream()
            .anyMatch(children -> children != null && !children.isEmpty());
  }
}