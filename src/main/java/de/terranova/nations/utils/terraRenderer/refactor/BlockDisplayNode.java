package de.terranova.nations.utils.terraRenderer.refactor;

import com.mojang.math.Transformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Interaction;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.entity.Player;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * High-level wrapper around an NMS BlockDisplay entity.
 *
 * - Renders a BlockDisplay via packets only (not added to the world)
 * - Optionally spawns a packet-only Interaction entity as a click hitbox
 * - Clicks are captured via Netty (ServerboundInteractPacket) and routed to onClick(...)
 */
public class BlockDisplayNode {

    // ------------------------------------------------------------------------
    // Static registries for hitboxes
    // ------------------------------------------------------------------------

    /** Map of hitbox entity id (Interaction) -> BlockDisplayNode. */
    private static final Map<Integer, BlockDisplayNode> NODES_BY_HITBOX_ID =
            new ConcurrentHashMap<>();

    // ------------------------------------------------------------------------
    // Per-node state
    // ------------------------------------------------------------------------

    private Location location;
    private Vector3f scale = new Vector3f(1f, 1f, 1f);
    private Vector3f rotationEulerDeg = new Vector3f(0f, 0f, 0f);
    private Material material = Material.BARRIER;

    private boolean glowing = false;
    private Color glowColor = null;

    /** Packet-only BlockDisplay entity id. */
    private int displayEntityId = -1;

    /** Packet-only hitbox entity id (Interaction), or -1 if none. */
    private int hitboxEntityId = -1;

    /** Optional click handler. */
    private Consumer<ClickContext> clickHandler;

    /** Optional hover handler (not triggered yet, reserved for future use). */
    private Consumer<ClickContext> hoverHandler;

    /** The server-side BlockDisplay instance we re-use for updates (packet-only). */
    private Display.BlockDisplay displayEntity;

    /** The server-side Interaction instance we re-use for updates (packet-only). */
    private Interaction hitboxEntity;

    // ------------------------------------------------------------------------
    // Fluent configuration API
    // ------------------------------------------------------------------------

    public BlockDisplayNode location(Location location) {
        this.location = (location == null) ? null : location.clone();
        return this;
    }

    /** Convenience: uniform scale in all directions (world-space size of the rendered block). */
    public BlockDisplayNode size(float size) {
        this.scale = new Vector3f(size, size, size);
        return this;
    }

    public BlockDisplayNode scale(Vector3f scale) {
        this.scale = (scale == null) ? new Vector3f(1f, 1f, 1f) : new Vector3f(scale);
        return this;
    }

    public BlockDisplayNode rotationEulerDeg(Vector3f eulerDeg) {
        this.rotationEulerDeg = (eulerDeg == null) ? new Vector3f(0f, 0f, 0f) : new Vector3f(eulerDeg);
        return this;
    }

    public BlockDisplayNode material(Material material) {
        this.material = material;
        return this;
    }

    /** Disable glow. */
    public BlockDisplayNode glow() {
        this.glowing = false;
        this.glowColor = null;
        return this;
    }

    /** Enable glow with the given RGB color (0xRRGGBB). */
    public BlockDisplayNode glow(int rgb) {
        this.glowing = true;
        this.glowColor = Color.fromRGB(rgb & 0xFFFFFF);
        return this;
    }

    /**
     * Register a click handler. When set, a packet-only Interaction hitbox entity is spawned
     * and click packets are routed via Netty to this handler.
     */
    public BlockDisplayNode onClick(Consumer<ClickContext> handler) {
        this.clickHandler = handler;
        return this;
    }

    /**
     * Register a hover handler placeholder.
     * NOTE: not triggered yet – reserved for ray-trace/hover implementation.
     */
    @Deprecated
    public BlockDisplayNode onHover(Consumer<ClickContext> handler) {
        this.hoverHandler = handler;
        return this;
    }

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    public Location getLocation() {
        return location == null ? null : location.clone();
    }

    public Vector3f getScale() {
        return new Vector3f(scale);
    }

    public Vector3f getRotationEulerDeg() {
        return new Vector3f(rotationEulerDeg);
    }

    public int getDisplayEntityId() {
        return displayEntityId;
    }

    public int getHitboxEntityId() {
        return hitboxEntityId;
    }

    // ------------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------------

    /**
     * Spawn this BlockDisplay visually via packets and, if needed, a hitbox
     * Interaction entity for click/hover interactions.
     */
    public void spawn(Collection<Player> players) {
        Display.BlockDisplay nmsDisplay = createDisplayNmsEntity();
        if (nmsDisplay == null) return;

        this.displayEntity = nmsDisplay;
        this.displayEntityId = nmsDisplay.getId();
        DisplayPackets.spawn(nmsDisplay, players);

        // If any interaction handler is present, spawn a hitbox entity
        if (clickHandler != null || hoverHandler != null) {
            Interaction nmsHitbox = createHitboxNmsEntity();
            if (nmsHitbox != null) {
                this.hitboxEntity = nmsHitbox;
                this.hitboxEntityId = nmsHitbox.getId();
                NODES_BY_HITBOX_ID.put(this.hitboxEntityId, this);
                DisplayPackets.spawn(nmsHitbox, players);
            }
        }
    }

    /**
     * Despawn both the BlockDisplay and the hitbox entity via packets
     * and cleanup static mappings + cached instances.
     */
    public void despawn(Collection<Player> players) {
        if (displayEntityId != -1) {
            DisplayPackets.remove(displayEntityId, players);
            displayEntityId = -1;
        }

        if (hitboxEntityId != -1) {
            DisplayPackets.remove(hitboxEntityId, players);
            NODES_BY_HITBOX_ID.remove(hitboxEntityId);
            hitboxEntityId = -1;
        }

        this.displayEntity = null;
        this.hitboxEntity = null;
    }

    /**
     * Instant update without interpolation (snap to new state).
     */
    public void update(Collection<Player> players) {
        update(players, 0);
    }

    /**
     * Update the existing BlockDisplay using built-in interpolation.
     *
     * @param interpolationDurationTicks number of ticks the client should interpolate between old and new state
     */
    public void update(Collection<Player> players, int interpolationDurationTicks) {
        if (players == null || players.isEmpty()) return;

        // If somehow the entity is gone, fall back to full respawn
        if (displayEntityId == -1 || displayEntity == null) {
            despawn(players);
            spawn(players);
            return;
        }

        // Apply our high-level state to the existing NMS entity
        applySettingsToDisplay(displayEntity, interpolationDurationTicks);

        // Send metadata/transform updates to viewers
        DisplayPackets.update(displayEntity, players);
    }

    // ------------------------------------------------------------------------
    // NMS construction for BlockDisplay (visual)
    // ------------------------------------------------------------------------

    /**
     * Build the underlying NMS BlockDisplay with the current settings.
     * Does not send any packets or register the node.
     */
    public Display.BlockDisplay createDisplayNmsEntity() {
        if (location == null || location.getWorld() == null) return null;
        if (material == null || !material.isBlock()) return null;

        ServerLevel nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        Display.BlockDisplay nmsDisplay =
                new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, nmsWorld);

        // Initial configuration (no interpolation on initial spawn)
        applySettingsToDisplay(nmsDisplay, 0);
        return nmsDisplay;
    }

    /**
     * Apply current BlockDisplayNode state to an existing NMS BlockDisplay.
     * Used for both initial spawn and later updates.
     */
    private void applySettingsToDisplay(Display.BlockDisplay nmsDisplay, int interpolationDurationTicks) {
        if (location == null || location.getWorld() == null) return;
        if (material == null || !material.isBlock()) return;

        // World position
        nmsDisplay.setPos(location.getX(), location.getY(), location.getZ());

        // Blockstate from Material
        CraftBlockData cbd = (CraftBlockData) material.createBlockData();
        nmsDisplay.setBlockState(cbd.getState());

        // No billboard (no facing-to-player)
        nmsDisplay.setBillboardConstraints(Display.BillboardConstraints.FIXED);

        // Euler (deg, YXZ) → Quaternion
        Quaternionf rotQ = DisplayMath.eulerToQuaternion(rotationEulerDeg);

        // Half-extents in local space
        Vector3f half = new Vector3f(scale).mul(0.5f);

        // Center at local origin even when rotated:
        // translation + rotQ * half = 0  ⇒  translation = - rotQ * half
        Vector3f translation = new Vector3f(half);
        translation.rotate(rotQ).negate();

        nmsDisplay.setTransformation(new Transformation(
                translation,            // local translation
                new Quaternionf(rotQ),  // rotation around local origin
                new Vector3f(scale),    // local scale
                new Quaternionf()       // right rotation = identity
        ));

        // Apply glow via Bukkit wrapper
        org.bukkit.entity.BlockDisplay bukkitDisplay =
                (org.bukkit.entity.BlockDisplay) nmsDisplay.getBukkitEntity();

        if (!glowing) {
            bukkitDisplay.setGlowing(false);
            bukkitDisplay.setGlowColorOverride(null);
        } else {
            bukkitDisplay.setGlowing(true);
            bukkitDisplay.setGlowColorOverride(glowColor);
        }

        // Built-in interpolation (1.20+ Display API)
        if (interpolationDurationTicks > 0) {

            nmsDisplay.setTransformationInterpolationDuration(interpolationDurationTicks);
            nmsDisplay.setTransformationInterpolationDelay(0);
        } else {
            // 0 duration disables interpolation
            nmsDisplay.setTransformationInterpolationDuration(0);
            nmsDisplay.setTransformationInterpolationDelay(0);
        }
    }

    // ------------------------------------------------------------------------
    // NMS construction for hitbox (Interaction), packet-only
    // ------------------------------------------------------------------------

    /**
     * Build a packet-only Interaction entity to act as a click hitbox.
     * The client will see it as an entity (for raytracing) but we never
     * register it in the world server-side – only via packets.
     *
     * The hitbox size is derived from the BlockDisplay scale to avoid
     * overlapping hitboxes and mis-clicks.
     */
    private Interaction createHitboxNmsEntity() {
        if (location == null || location.getWorld() == null) return null;

        ServerLevel nmsWorld = ((CraftWorld) location.getWorld()).getHandle();

        float width  = Math.max(0.1f, scale.x);
        float height = Math.max(0.1f, scale.y);

        Interaction hitbox = new Interaction(EntityType.INTERACTION, nmsWorld);

        // Interaction-BB geht typischerweise von (x, y, z) nach oben.
        // Wir wollen, dass der Mittelpunkt der BB bei der Display-Mitte liegt.
        double x = location.getX();
        double y = location.getY() - (height / 2.0f); // center on display
        double z = location.getZ();

        hitbox.setPos(x, y, z);

        // je nach NMS-Version ggf. setWidth/setHeight statt setInteractionWidth/Height
        hitbox.setWidth(width);
        hitbox.setHeight(height);

        hitbox.setInvulnerable(true);
        hitbox.noPhysics = true;

        return hitbox;
    }

    // ------------------------------------------------------------------------
    // Static hook used by Netty packet listener
    // ------------------------------------------------------------------------

    /**
     * Called from the Netty handler when a ServerboundInteractPacket arrives.
     * If the entity id matches one of our hitboxes, the click handler is fired.
     */
    public static void handlePacketClick(int entityId, Player player) {
        BlockDisplayNode node = NODES_BY_HITBOX_ID.get(entityId);
        if (node == null) return;

        if (node.clickHandler != null) {
            node.clickHandler.accept(new ClickContext(player, node));
        }
        // hoverHandler could be triggered here for more complex logic if desired
    }

    // ------------------------------------------------------------------------
    // Simple context object for click callbacks
    // ------------------------------------------------------------------------

    public static final class ClickContext {
        private final Player player;
        private final BlockDisplayNode node;

        public ClickContext(Player player, BlockDisplayNode node) {
            this.player = player;
            this.node = node;
        }

        public Player player() {
            return player;
        }

        public BlockDisplayNode node() {
            return node;
        }
    }
}


