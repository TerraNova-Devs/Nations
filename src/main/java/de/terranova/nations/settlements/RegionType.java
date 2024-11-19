package de.terranova.nations.settlements;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import de.terranova.nations.citizens.SettleTrait;
import de.terranova.nations.database.SettleDBstuff;
import de.terranova.nations.worldguard.NationsRegionFlag.RegionFlag;
import de.terranova.nations.worldguard.NationsRegionFlag.SettleFlag;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public abstract class RegionType {

    public UUID id;
    public String name;
    public ProtectedRegion region;
    protected NPC npc;
    String type;

    public static List<String> regionTypes = List.of("settle","outpost");

    public RegionType(String name, UUID id, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public abstract void remove();

    protected NPC createNPC(String name, Location location, UUID settlementUUID) {
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, name);

        SkinTrait skinTrait = npc.getOrAddTrait(SkinTrait.class);
        skinTrait.setSkinPersistent(name, TownSkins.BEGGAR.getSkinSign(), TownSkins.BEGGAR.getSkinTexture());

        LookClose lookTrait = npc.getOrAddTrait(LookClose.class);
        lookTrait.toggle();

        SettleTrait settleTrait = npc.getOrAddTrait(SettleTrait.class);
        settleTrait.setUUID(settlementUUID);

        npc.setAlwaysUseNameHologram(true);
        npc.setName(String.format("<gradient:#AAE3E9:#DFBDEA>&l%s</gradient>", this.name.replaceAll("_", " ")));
        npc.spawn(location);
        return npc;
    }

    protected void getCitizensNPCbySUUID() {
        if (npc == null) {
            for (NPC npc : CitizensAPI.getNPCRegistry()) {

                if (!npc.hasTrait(SettleTrait.class)) {

                    continue;
                }

                if (npc.getOrAddTrait(SettleTrait.class).getUUID().equals(this.id)) {
                    this.npc = npc;
                }
            }
        }
    }

    public void tpNPC(Location location) {
        for (NPC npc : CitizensAPI.getNPCRegistry()) {
            if (!npc.hasTrait(SettleTrait.class)) {
                continue;
            }
            if (npc.getOrAddTrait(SettleTrait.class).getUUID().equals(this.id)) {
                npc.teleport(location, PlayerTeleportEvent.TeleportCause.COMMAND);
            }
        }

    }

    public void reskinNpc(TownSkins skin) {
        for (NPC npc : CitizensAPI.getNPCRegistry()) {
            if (!npc.hasTrait(SettleTrait.class)) {
                continue;
            }
            if (npc.getOrAddTrait(SettleTrait.class).getUUID().equals(this.id)) {
                SkinTrait skinTrait = npc.getOrAddTrait(SkinTrait.class);
                skinTrait.setSkinPersistent(skin.name(), skin.getSkinSign(), skin.getSkinTexture());
            }
        }
    }

    public void rename(String name) {

        renameNPC(name);
        renameRegion(name);
    }

    public void renameNPC(String name) {

        getCitizensNPCbySUUID();
        this.name = name;
        npc.setName(String.format("<gradient:#AAE3E9:#DFBDEA>&l%s</gradient>", this.name.replaceAll("_", " ")));

    }

    public void renameRegion(String name) {

        ProtectedPolygonalRegion newregion = new ProtectedPolygonalRegion(name, region.getPoints(), region.getMinimumPoint().y(), region.getMaximumPoint().y());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(Objects.requireNonNull(Bukkit.getWorld("world"))));
        newregion.copyFrom(region);
        assert regions != null;
        regions.removeRegion(region.getId());
        regions.addRegion(newregion);
        this.region = newregion;
        SettleDBstuff settleDB = new SettleDBstuff(this.id);
        settleDB.rename(name);

    }

    public ProtectedRegion getWorldguardRegion() {

        World world = Bukkit.getWorld("world");
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        assert world != null;
        RegionManager regions = container.get(BukkitAdapter.adapt(world));
        assert regions != null;
        for (ProtectedRegion region : regions.getRegions().values()) {
            if (region.getFlag(RegionFlag.REGION_UUID_FLAG) == null) continue;
            UUID settlementUUID = UUID.fromString(Objects.requireNonNull(region.getFlag(RegionFlag.REGION_UUID_FLAG)));
            if (this.id.equals(settlementUUID)) {
                return region;
            }

        }
        return null;
    }



    public void removeNPC() {
        getCitizensNPCbySUUID();
        this.npc.destroy();
    }

    public void removeWGRegion() {
        ProtectedRegion region = getWorldguardRegion();
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(Objects.requireNonNull(Bukkit.getWorld("world"))));
        assert regions != null;
        regions.removeRegion(region.getId());
    }

}
