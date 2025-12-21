package de.terranova.nations.utils.terraRenderer.refactor.DisplayGroups;

import de.terranova.nations.utils.terraRenderer.refactor.Anchor.Anchor3D;
import de.terranova.nations.utils.terraRenderer.refactor.BlockDisplayNode;
import de.terranova.nations.utils.terraRenderer.refactor.DisplayGroup;
import de.terranova.nations.utils.terraRenderer.refactor.DisplayMath;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Collection;

/**
 * A DisplayGroup representing a cuboid as 12 line segments (wireframe),
 * not a filled block.
 */
public class DisplayCube extends DisplayGroup {

    private Location from;
    private Location to;
    private Material material;
    private boolean glowing;
    private int glowColor;
    private float thickness;

    public DisplayCube(Location from,
                       Location to,
                       Material material,
                       boolean glowing,
                       int glowColor) {
        this(from, to, 0.10f, material, glowing, glowColor); // default thickness
    }

    public DisplayCube(Location from,
                       Location to,
                       float thickness,
                       Material material,
                       boolean glowing,
                       int glowColor) {
        this.from = from == null ? null : from.clone();
        this.to = to == null ? null : to.clone();
        this.material = material;
        this.glowing = glowing;
        this.glowColor = glowColor;
        this.thickness = thickness;

        buildNodes();
    }

    private void buildNodes() {
        nodes.clear();
        if (from == null || to == null) return;
        if (!from.getWorld().equals(to.getWorld())) return;

        double minX = Math.min(from.getX(), to.getX());
        double minY = Math.min(from.getY(), to.getY());
        double minZ = Math.min(from.getZ(), to.getZ());
        double maxX = Math.max(from.getX(), to.getX());
        double maxY = Math.max(from.getY(), to.getY());
        double maxZ = Math.max(from.getZ(), to.getZ());

        var world = from.getWorld();

        Location c000 = new Location(world, minX, minY, minZ);
        Location c100 = new Location(world, maxX, minY, minZ);
        Location c010 = new Location(world, minX, maxY, minZ);
        Location c110 = new Location(world, maxX, maxY, minZ);

        Location c001 = new Location(world, minX, minY, maxZ);
        Location c101 = new Location(world, maxX, minY, maxZ);
        Location c011 = new Location(world, minX, maxY, maxZ);
        Location c111 = new Location(world, maxX, maxY, maxZ);

        // Bottom rectangle
        addEdge(c000, c100); // 0
        addEdge(c100, c110); // 1
        addEdge(c110, c010); // 2
        addEdge(c010, c000); // 3

        // Top rectangle
        addEdge(c001, c101); // 4
        addEdge(c101, c111); // 5
        addEdge(c111, c011); // 6
        addEdge(c011, c001); // 7

        // Vertical edges
        addEdge(c000, c001); // 8
        addEdge(c100, c101); // 9
        addEdge(c110, c111); // 10
        addEdge(c010, c011); // 11
    }

    private void addEdge(Location a, Location b) {
        BlockDisplayNode edge = createEdgeNode(a, b);
        if (edge != null) {
            add(edge);
        }
    }

    private BlockDisplayNode createEdgeNode(Location start, Location end) {
        if (start == null || end == null) return null;
        if (!start.getWorld().equals(end.getWorld())) return null;

        Vector3f dir = new Vector3f(
                (float) (end.getX() - start.getX()),
                (float) (end.getY() - start.getY()),
                (float) (end.getZ() - start.getZ())
        );
        float length = dir.length();
        if (length == 0) return null;

        dir.normalize();

        // Midpoint of the edge
        Location mid = start.clone().add(end).multiply(0.5);

        Vector3f scale = new Vector3f(thickness, thickness, length);

        // Rotate local +Z to dir
        Quaternionf q = new Quaternionf().rotationTo(new Vector3f(0, 0, 1), dir);
        Vector3f rotationEuler = DisplayMath.quaternionToEulerYXZDeg(q);

        BlockDisplayNode node = new BlockDisplayNode()
                .location(mid)
                .scale(scale)
                .rotationEulerDeg(rotationEuler)
                .material(material);

        if (glowing) {
            node.glow(glowColor);
        } else {
            node.glow(); // ensure glow is off
        }

        return node;
    }

    public DisplayCube anchor(Anchor3D anchor, Location worldLocation) {
        super.anchor(worldLocation, anchor);
        return this;
    }

    /**
     * Update all 12 edge nodes to match the new from/to locations.
     * Reuses existing BlockDisplayNodes and uses interpolationTicks for smooth movement.
     */
    public void update(Location from, Location to, Collection<Player> viewers, int interpolationTicks) {
        this.from = from == null ? null : from.clone();
        this.to = to == null ? null : to.clone();

        if (this.from == null || this.to == null) {
            return;
        }
        if (!this.from.getWorld().equals(this.to.getWorld())) {
            return;
        }

        // If something went wrong and we don't have 12 edges, rebuild & respawn once.
        if (nodes.size() != 12) {
            nodes.clear();
            buildNodes();
            spawn(viewers); // no interpolation on rebuild
            return;
        }

        double minX = Math.min(this.from.getX(), this.to.getX());
        double minY = Math.min(this.from.getY(), this.to.getY());
        double minZ = Math.min(this.from.getZ(), this.to.getZ());
        double maxX = Math.max(this.from.getX(), this.to.getX());
        double maxY = Math.max(this.from.getY(), this.to.getY());
        double maxZ = Math.max(this.from.getZ(), this.to.getZ());

        var world = this.from.getWorld();

        Location c000 = new Location(world, minX, minY, minZ);
        Location c100 = new Location(world, maxX, minY, minZ);
        Location c010 = new Location(world, minX, maxY, minZ);
        Location c110 = new Location(world, maxX, maxY, minZ);

        Location c001 = new Location(world, minX, minY, maxZ);
        Location c101 = new Location(world, maxX, minY, maxZ);
        Location c011 = new Location(world, minX, maxY, maxZ);
        Location c111 = new Location(world, maxX, maxY, maxZ);

        // Same edge order as in buildNodes()

        // Bottom rectangle
        updateEdge((BlockDisplayNode) nodes.get(0), c000, c100, viewers, interpolationTicks);
        updateEdge((BlockDisplayNode) nodes.get(1), c100, c110, viewers, interpolationTicks);
        updateEdge((BlockDisplayNode) nodes.get(2), c110, c010, viewers, interpolationTicks);
        updateEdge((BlockDisplayNode) nodes.get(3), c010, c000, viewers, interpolationTicks);

        // Top rectangle
        updateEdge((BlockDisplayNode) nodes.get(4), c001, c101, viewers, interpolationTicks);
        updateEdge((BlockDisplayNode) nodes.get(5), c101, c111, viewers, interpolationTicks);
        updateEdge((BlockDisplayNode) nodes.get(6), c111, c011, viewers, interpolationTicks);
        updateEdge((BlockDisplayNode) nodes.get(7), c011, c001, viewers, interpolationTicks);

        // Vertical edges
        updateEdge((BlockDisplayNode) nodes.get(8),  c000, c001, viewers, interpolationTicks);
        updateEdge((BlockDisplayNode) nodes.get(9),  c100, c101, viewers, interpolationTicks);
        updateEdge((BlockDisplayNode) nodes.get(10), c110, c111, viewers, interpolationTicks);
        updateEdge((BlockDisplayNode) nodes.get(11), c010, c011, viewers, interpolationTicks);
    }

    private void updateEdge(BlockDisplayNode node,
                            Location start,
                            Location end,
                            Collection<Player> viewers,
                            int interpolationTicks) {
        if (node == null || start == null || end == null) return;
        if (!start.getWorld().equals(end.getWorld())) return;

        Vector3f dir = new Vector3f(
                (float) (end.getX() - start.getX()),
                (float) (end.getY() - start.getY()),
                (float) (end.getZ() - start.getZ())
        );
        float length = dir.length();
        if (length == 0) return;

        dir.normalize();

        // Midpoint of the edge
        Location mid = start.clone().add(end).multiply(0.5);

        Vector3f scale = new Vector3f(thickness, thickness, length);

        // Rotate local +Z to dir
        Quaternionf q = new Quaternionf().rotationTo(new Vector3f(0, 0, 1), dir);
        Vector3f rotationEuler = DisplayMath.quaternionToEulerYXZDeg(q);

        node.location(mid)
                .scale(scale)
                .rotationEulerDeg(rotationEuler);

        if (glowing) {
            node.glow(glowColor);
        } else {
            node.glow();
        }

        node.update(viewers, interpolationTicks);
    }
}


