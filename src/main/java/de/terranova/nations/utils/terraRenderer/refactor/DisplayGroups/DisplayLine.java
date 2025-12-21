package de.terranova.nations.utils.terraRenderer.refactor.DisplayGroups;

import de.terranova.nations.utils.terraRenderer.refactor.Anchor.Anchor3D;
import de.terranova.nations.utils.terraRenderer.refactor.Anchor.LineAnchor;
import de.terranova.nations.utils.terraRenderer.refactor.BlockDisplayNode;
import de.terranova.nations.utils.terraRenderer.refactor.DisplayGroup;
import de.terranova.nations.utils.terraRenderer.refactor.DisplayMath;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Collection;

public class DisplayLine extends DisplayGroup {

    private Location start;
    private Location end;
    private float thickness;
    private final Material material;
    private final int glow;
    private final boolean glowing;

    public DisplayLine(Location start, Location end,
                       float thickness,
                       Material material,
                       boolean glowing,
                       int glow) {
        this.start = start == null ? null : start.clone();
        this.end = end == null ? null : end.clone();
        this.thickness = thickness;
        this.material = material;
        this.glow = glow;
        this.glowing = glowing;
        buildNode();
    }

    private void buildNode() {
        if (start == null || end == null) return;
        if (!start.getWorld().equals(end.getWorld())) return;

        Vector3f dir = new Vector3f(
                (float) (end.getX() - start.getX()),
                (float) (end.getY() - start.getY()),
                (float) (end.getZ() - start.getZ())
        );
        float length = dir.length();
        if (length == 0) return;

        dir.normalize();

        Location mid = start.clone().add(end).multiply(0.5);

        Vector3f scale = new Vector3f(thickness, thickness, length);

        Quaternionf q = new Quaternionf().rotationTo(new Vector3f(0, 0, 1), dir);
        Vector3f rotationEuler = DisplayMath.quaternionToEulerYXZDeg(q);

        BlockDisplayNode node = new BlockDisplayNode()
                .location(mid)
                .scale(scale)
                .rotationEulerDeg(rotationEuler)
                .material(material);
        if(glowing) node.glow(glow);
        add(node);
    }

    public DisplayLine thickness(float thickness) {
        this.thickness = thickness;
        nodes.clear();
        buildNode();
        return this;
    }

    public DisplayLine endpoints(Location start, Location end) {
        this.start = start == null ? null : start.clone();
        this.end = end == null ? null : end.clone();
        nodes.clear();
        buildNode();
        return this;
    }

    public void spawn(Collection<Player> players, LineAnchor anchor, Location anchorLocation) {
        if (anchor != null && anchorLocation != null && !nodes.isEmpty()) {
            // map LineAnchor to Anchor3D along the line's main axis
            // for now, just use CENTER and move the endpoints based on t if you want.
            // Simplified: we only use group anchor for center here.
            if (anchor == LineAnchor.CENTER) {
                this.anchor(anchorLocation, Anchor3D.CENTER);
            }
        }
        super.spawn(players);
    }
}
