package de.terranova.nations.utils.terraRenderer.refactor;

import de.terranova.nations.utils.terraRenderer.refactor.Anchor.Anchor3D;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DisplayGroup {

    protected final List<BlockDisplayNode> nodes = new ArrayList<>();

    protected Location anchorLocation;   // world-space anchor
    protected Anchor3D anchor3D;         // which point on the primary node

    public DisplayGroup add(BlockDisplayNode node) {
        if (node != null) {
            nodes.add(node);
        }
        return this;
    }

    public DisplayGroup anchor(Location worldLocation, Anchor3D anchor3D) {
        this.anchorLocation = worldLocation == null ? null : worldLocation.clone();
        this.anchor3D = anchor3D;
        return this;
    }

    public DisplayGroup scale(float factor) {
        for (BlockDisplayNode node : nodes) {
            Vector3f old = node.getScale();
            node.scale(new Vector3f(old).mul(factor));
        }
        return this;
    }

    public void spawn(Collection<Player> players) {
        applyAnchorIfNeeded();
        for (BlockDisplayNode node : nodes) {
            node.spawn(players);
        }
    }

    public void update(Collection<Player> players) {
        applyAnchorIfNeeded();
        for (BlockDisplayNode node : nodes) {
            node.update(players);
        }
    }

    public void despawn(Collection<Player> players) {
        for (BlockDisplayNode node : nodes) {
            node.despawn(players);
        }
    }

    public int getPrimaryEntityId() {
        if (nodes.isEmpty()) return -1;
        return nodes.get(0).getDisplayEntityId();
    }

    protected void applyAnchorIfNeeded() {
        if (anchorLocation == null || anchor3D == null || nodes.isEmpty()) return;

        BlockDisplayNode primary = nodes.get(0);
        Location loc = primary.getLocation();
        if (loc == null) return;

        Vector3f size = primary.getScale();
        Vector3f localOffset = anchor3D.offsetForSize(size);

        Quaternionf rot = DisplayMath.eulerToQuaternion(primary.getRotationEulerDeg());
        Vector3f worldOffset = new Vector3f(localOffset).rotate(rot);

        Location currentAnchorPos = loc.clone().add(
                new Vector(worldOffset.x, worldOffset.y, worldOffset.z)
        );

        Vector delta = anchorLocation.toVector().subtract(currentAnchorPos.toVector());

        for (BlockDisplayNode node : nodes) {
            Location nLoc = node.getLocation();
            if (nLoc == null) continue;
            node.location(nLoc.add(delta));
        }
    }
}