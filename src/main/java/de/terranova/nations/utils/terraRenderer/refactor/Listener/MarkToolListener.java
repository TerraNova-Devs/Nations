package de.terranova.nations.utils.terraRenderer.refactor.Listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import de.terranova.nations.utils.terraRenderer.refactor.BlockDisplayNode;
import de.terranova.nations.utils.terraRenderer.refactor.DisplayGroups.DisplayCube;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public final class MarkToolListener implements Listener {

    /* ============================================================
       STATIC GLOBAL STATE
       ============================================================ */

    private static final Map<UUID, DisplayCube> ACTIVE_CUBES = new HashMap<>();
    private static final Map<UUID, DisplayCube> CACHED_CUBES = new HashMap<>();
    private static final Map<UUID, List<BlockDisplayNode>> ACTIVE_MARKERS = new HashMap<>();
    private static final Map<UUID, RegionSelection> ACTIVE_REGIONS = new HashMap<>();

    private static final int INTERPOLATION_TICKS = 10;

    /* ============================================================
       STATIC API (used by region creation)
       ============================================================ */

    public static Optional<RegionSelection> getSelection(UUID playerId) {
        return Optional.ofNullable(ACTIVE_REGIONS.get(playerId));
    }

    public static void clearSelection(UUID playerId) {
        ACTIVE_REGIONS.remove(playerId);
        clearDisplays(playerId);
        CACHED_CUBES.remove(playerId);
    }

    /* ============================================================
       REGION DATA CLASS
       ============================================================ */

    public static final class RegionSelection {
        public final World world;
        public int minX, minY, minZ;
        public int maxX, maxY, maxZ;

        public RegionSelection(World world, int x, int y, int z) {
            this.world = Objects.requireNonNull(world, "world");
            this.minX = this.maxX = x;
            this.minY = this.maxY = y;
            this.minZ = this.maxZ = z;
        }
    }

    /* ============================================================
       HELPER METHODS
       ============================================================ */

    private static boolean isTool(ItemStack item) {
        if (item == null || item.getType() != Material.BLAZE_ROD) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return false;
        return "tool".equalsIgnoreCase(ChatColor.stripColor(meta.getDisplayName()));
    }

    /* ============================================================
       EVENT HANDLERS
       ============================================================ */

    @EventHandler
    public void onToolInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Action action = event.getAction();
        if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!isTool(item)) return;

        Block clicked = event.getClickedBlock();
        BlockFace face = event.getBlockFace();
        if (clicked == null || face == null) return;

        event.setCancelled(true);

        UUID uuid = player.getUniqueId();

        if (action == Action.LEFT_CLICK_BLOCK) {
            RegionSelection region = createRegionFromClicked(clicked, face);
            ACTIVE_REGIONS.put(uuid, region);
            renderOrUpdate(player, region, false);
            return;
        }

        // RIGHT CLICK
        RegionSelection region = ACTIVE_REGIONS.get(uuid);
        boolean extended = false;

        if (region == null || !region.world.equals(clicked.getWorld())) {
            region = createRegionFromClicked(clicked, face);
            ACTIVE_REGIONS.put(uuid, region);
        } else {
            extended = extendRegion(region, clicked, player.isSneaking());
            if (!extended) return;
        }

        renderOrUpdate(player, region, extended);
    }

    @EventHandler
    public void onItemHeldChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
        ItemStack oldItem = player.getInventory().getItem(event.getPreviousSlot());

        boolean wasHoldingTool = isTool(oldItem);
        boolean isHoldingTool = isTool(newItem);

        if (wasHoldingTool && !isHoldingTool) {
            // Player switched away from tool - cache and hide the cube
            hideCube(uuid, player);
        } else if (!wasHoldingTool && isHoldingTool) {
            // Player switched to tool - restore cached cube if exists
            showCube(uuid, player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        clearSelection(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        clearSelection(event.getPlayer().getUniqueId());
    }

    /* ============================================================
       VISIBILITY MANAGEMENT
       ============================================================ */

    private static void hideCube(UUID uuid, Player player) {
        DisplayCube cube = ACTIVE_CUBES.remove(uuid);
        if (cube != null) {
            cube.despawn(List.of(player));
            CACHED_CUBES.put(uuid, cube);
        }
    }

    private static void showCube(UUID uuid, Player player) {
        DisplayCube cube = CACHED_CUBES.get(uuid);
        RegionSelection region = ACTIVE_REGIONS.get(uuid);

        if (cube != null && region != null) {
            cube.spawn(List.of(player));
            ACTIVE_CUBES.put(uuid, cube);
        }
    }

    /* ============================================================
       REGION LOGIC
       ============================================================ */

    private static RegionSelection createRegionFromClicked(Block clicked, BlockFace face) {
        Block base = clicked.getRelative(face);
        return new RegionSelection(
                base.getWorld(),
                base.getX(),
                base.getY(),
                base.getZ()
        );
    }

    private static boolean extendRegion(RegionSelection r, Block clicked, boolean includeClicked) {
        int cx = clicked.getX();
        int cy = clicked.getY();
        int cz = clicked.getZ();

        int oldMinX = r.minX, oldMaxX = r.maxX;
        int oldMinY = r.minY, oldMaxY = r.maxY;
        int oldMinZ = r.minZ, oldMaxZ = r.maxZ;

        int dxNeg = r.minX - cx;
        int dxPos = cx - r.maxX;
        int dyNeg = r.minY - cy;
        int dyPos = cy - r.maxY;
        int dzNeg = r.minZ - cz;
        int dzPos = cz - r.maxZ;

        int best = 0;
        char axis = 0;
        int sign = 0;

        if (dxNeg > best) { best = dxNeg; axis = 'X'; sign = -1; }
        if (dxPos > best) { best = dxPos; axis = 'X'; sign = +1; }
        if (dyNeg > best) { best = dyNeg; axis = 'Y'; sign = -1; }
        if (dyPos > best) { best = dyPos; axis = 'Y'; sign = +1; }
        if (dzNeg > best) { best = dzNeg; axis = 'Z'; sign = -1; }
        if (dzPos > best) { best = dzPos; axis = 'Z'; sign = +1; }

        if (best == 0) return false;

        switch (axis) {
            case 'X' -> {
                if (sign < 0) {
                    int newMin = includeClicked ? cx : cx + 1;
                    if (newMin < r.minX) r.minX = newMin;
                } else {
                    int newMax = includeClicked ? cx : cx - 1;
                    if (newMax > r.maxX) r.maxX = newMax;
                }
            }
            case 'Y' -> {
                if (sign < 0) {
                    int newMin = includeClicked ? cy : cy + 1;
                    if (newMin < r.minY) r.minY = newMin;
                } else {
                    int newMax = includeClicked ? cy : cy - 1;
                    if (newMax > r.maxY) r.maxY = newMax;
                }
            }
            case 'Z' -> {
                if (sign < 0) {
                    int newMin = includeClicked ? cz : cz + 1;
                    if (newMin < r.minZ) r.minZ = newMin;
                } else {
                    int newMax = includeClicked ? cz : cz - 1;
                    if (newMax > r.maxZ) r.maxZ = newMax;
                }
            }
            default -> {
            }
        }

        return r.minX != oldMinX || r.maxX != oldMaxX
                || r.minY != oldMinY || r.maxY != oldMaxY
                || r.minZ != oldMinZ || r.maxZ != oldMaxZ;
    }


    /* ============================================================
       RENDERING
       ============================================================ */

    private static void renderOrUpdate(Player player, RegionSelection region, boolean interpolate) {
        UUID uuid = player.getUniqueId();
        List<Player> viewers = List.of(player);

        Location from = new Location(region.world, region.minX, region.minY, region.minZ);
        Location to   = new Location(region.world, region.maxX + 1, region.maxY + 1, region.maxZ + 1);

        DisplayCube cube = ACTIVE_CUBES.get(uuid);
        if (cube == null) {
            cube = CACHED_CUBES.get(uuid);
        }

        int ticks = interpolate ? INTERPOLATION_TICKS : 0;

        if (cube == null) {
            cube = new DisplayCube(from, to, 0.10f, Material.WHITE_CONCRETE, true, 0xAA11EE);
            cube.spawn(viewers);
            ACTIVE_CUBES.put(uuid, cube);
        } else {
            cube.update(from, to, viewers, ticks);
            ACTIVE_CUBES.put(uuid, cube);
            CACHED_CUBES.put(uuid, cube);
        }
    }

    private static void clearDisplays(UUID uuid) {
        List<Player> viewers = Bukkit.getPlayer(uuid) == null
                ? List.of()
                : List.of(Bukkit.getPlayer(uuid));

        DisplayCube cube = ACTIVE_CUBES.remove(uuid);
        if (cube != null) cube.despawn(viewers);

        List<BlockDisplayNode> nodes = ACTIVE_MARKERS.remove(uuid);
        if (nodes != null) nodes.forEach(n -> n.despawn(viewers));
    }

    public static com.sk89q.worldedit.regions.Region toWorldEdit(
            MarkToolListener.RegionSelection sel
    ) {
        return new CuboidRegion(
                BukkitAdapter.adapt(sel.world),
                BlockVector3.at(sel.minX, sel.minY, sel.minZ),
                BlockVector3.at(sel.maxX, sel.maxY, sel.maxZ)
        );
    }
}