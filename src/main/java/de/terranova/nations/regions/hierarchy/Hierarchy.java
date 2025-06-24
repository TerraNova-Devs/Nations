package de.terranova.nations.regions.hierarchy;

import de.terranova.nations.regions.base.Region;

import java.util.*;

public class Hierarchy {

    private Region parent;
    private Map<String, Map<UUID, Region>> children;
    private Region region;

    Hierarchy(Region region) {
        this.region = region;
    }

    public Region getParent() {
        return parent;
    }

    public void setParent(Region parent) {
        this.parent = parent;
    }

    public void addChild(Region child) {
        String typeKey = child.getType();
        UUID id = child.getId();

        children
                .computeIfAbsent(typeKey, k -> new LinkedHashMap<>())
                .put(id, child);

        if(child instanceof HasHierarchy hierarchy) {
            hierarchy.getHierarchy().setParent(region);
        }
    }

    public void addChildren(Collection<Region> regions) {
        for (Region region : regions) {
            addChild(region);
        }
    }

    public void removeChild(Region child) {
        String typeKey = child.getType();
        Map<UUID, Region> typed = children.get(typeKey);

        if (typed != null) {
            typed.remove(child.getId());
            if (typed.isEmpty()) {
                children.remove(typeKey);
            }
        }

        if(child instanceof HasHierarchy hierarchy) {
            hierarchy.getHierarchy().setParent(null);
        }
    }

    public void clearChildren() {
        for (Map<UUID, Region> typeMap : children.values()) {
            for (Region child : typeMap.values()) {
                if(child instanceof HasHierarchy hierarchy) {
                    hierarchy.getHierarchy().setParent(null);
                }
            }
        }
        children.clear();
    }

    public Map<String, Map<UUID, Region>> getChildren() {
        return Collections.unmodifiableMap(children);
    }

    public Collection<Region> getChildrenOfType(String type) {
        return children.getOrDefault(type, Collections.emptyMap()).values();
    }

    public Region getChild(String type, UUID id) {
        return children.getOrDefault(type, Collections.emptyMap()).get(id);
    }

    public boolean hasChild(UUID id) {
        return children.values().stream().anyMatch(map -> map.containsKey(id));
    }

    public int getTotalChildCount() {
        return children.values().stream().mapToInt(Map::size).sum();
    }
}
