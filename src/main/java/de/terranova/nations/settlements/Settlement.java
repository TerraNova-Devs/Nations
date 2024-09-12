package de.terranova.nations.settlements;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.database.SettleDBstuff;
import de.terranova.nations.settlements.level.Objective;
import de.terranova.nations.worldguard.SettlementClaim;
import de.terranova.nations.worldguard.SettlementFlag;
import de.terranova.nations.worldguard.math.Vectore2;
import io.th0rgal.oraxen.api.OraxenItems;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.HologramTrait;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.*;

public class Settlement {

    public final UUID id;
    public final Vectore2 location;
    public String name;
    public int level;

    public Objective objective;
    public HashMap<UUID, AccessLevelEnum> membersAccess = new HashMap<>();
    public int claims;
    public ProtectedRegion region;
    NPC npc;


    //Beim neu erstellen
    public Settlement(UUID settlementUUID, UUID owner, Location location, String name) {

        //INIT
        this.id = settlementUUID;
        this.name = name;
        this.location = SettlementClaim.getSChunkMiddle(location);
        NationsPlugin.settlementManager.locations.add(this.location);
        this.level = 1;
        this.membersAccess.put(owner, AccessLevelEnum.MAJOR);
        this.region = getWorldguardRegion();
        this.objective = new Objective(0, 0, 0, 0, 0, null, null, null, null);
        //POST INIT
        this.npc = createNPC(name, location, settlementUUID);
        this.claims = SettlementClaim.getClaimAnzahl(this.id);
        Set<com.sk89q.worldedit.world.entity.EntityType> set = new HashSet<>(Arrays.asList(com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:zombie_villager"), com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:zombie"), com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:spider"),
                com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:skeleton"), com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:enderman"), com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:phantom"), com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:drowned"),
                com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:witch"), com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:pillager"), com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:husk"),
                com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:creeper")
        ));
        region.setFlag(Flags.DENY_SPAWN, set);
        region.setFlag(Flags.PVP, StateFlag.State.DENY);

    }

    //Von der Datenbank
    public Settlement(UUID settlementUUID, HashMap<UUID, AccessLevelEnum> membersAccess, Vectore2 location, String name, int level, Objective objective) {
        this.id = settlementUUID;
        this.name = name;
        this.location = SettlementClaim.getSChunkMiddle(location);
        NationsPlugin.settlementManager.locations.add(SettlementClaim.getSChunkMiddle(location));
        this.level = level;
        this.membersAccess = membersAccess;
        this.region = getWorldguardRegion();
        this.objective = objective;
        this.claims = SettlementClaim.getClaimAnzahl(settlementUUID);
        //funktioniert nicht im Constructor
        getCitizensNPCbySUUID();
    }

    private NPC createNPC(String name, Location location, UUID settlementUUID) {
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, name);

        SkinTrait skinTrait = npc.getOrAddTrait(SkinTrait.class);
        skinTrait.setSkinPersistent(name, TownSkins.BEGGAR.getSkinSign(), TownSkins.BEGGAR.getSkinTexture());

        LookClose lookTrait = npc.getOrAddTrait(LookClose.class);
        lookTrait.toggle();

        SettlementTrait settlementTrait = npc.getOrAddTrait(SettlementTrait.class);
        settlementTrait.setUUID(settlementUUID);

        HologramTrait hologramTrait = npc.getOrAddTrait(HologramTrait.class);
        hologramTrait.addLine(String.format("<#B0EB94>Level: [%s]", this.level));
        npc.setAlwaysUseNameHologram(true);
        npc.setName(String.format("<gradient:#AAE3E9:#DFBDEA>&l%s</gradient>", this.name.replaceAll("_", " ")));
        npc.spawn(location);
        return npc;
    }

    private void getCitizensNPCbySUUID() {
        if (npc == null) {
            for (NPC npc : CitizensAPI.getNPCRegistry()) {

                if (!npc.hasTrait(SettlementTrait.class)) {

                    continue;
                }

                if (npc.getOrAddTrait(SettlementTrait.class).getUUID().equals(this.id)) {
                    this.npc = npc;
                }
            }
        }
    }

    public void tpNPC(Location location) {
        for (NPC npc : CitizensAPI.getNPCRegistry()) {
            if (!npc.hasTrait(SettlementTrait.class)) {
                continue;
            }
            if (npc.getOrAddTrait(SettlementTrait.class).getUUID().equals(this.id)) {
                npc.teleport(location, PlayerTeleportEvent.TeleportCause.COMMAND);
            }
        }

    }

    public void reskinNpc(TownSkins skin) {
        for (NPC npc : CitizensAPI.getNPCRegistry()) {
            if (!npc.hasTrait(SettlementTrait.class)) {
                continue;
            }
            if (npc.getOrAddTrait(SettlementTrait.class).getUUID().equals(this.id)) {
                SkinTrait skinTrait = npc.getOrAddTrait(SkinTrait.class);
                skinTrait.setSkinPersistent(skin.name(), skin.getSkinSign(), skin.getSkinTexture());
            }
        }
    }

    public void rename(String name) {

        getCitizensNPCbySUUID();

        this.name = name;
        npc.setName(String.format("<gradient:#AAE3E9:#DFBDEA>&l%s</gradient>", this.name.replaceAll("_", " ")));

        ProtectedPolygonalRegion newregion = new ProtectedPolygonalRegion(name, region.getPoints(), region.getMinimumPoint().y(), region.getMaximumPoint().y());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(Objects.requireNonNull(Bukkit.getWorld("world"))));
        newregion.copyFrom(region);
        assert regions != null;
        regions.removeRegion(region.getId());
        regions.addRegion(newregion);
        this.region = newregion;
        SettleDBstuff.rename(this.id, name);
    }

    public Collection<UUID> getEveryUUIDWithCertainAccessLevel(AccessLevelEnum access) {
        Collection<UUID> output = new ArrayList<>();
        for (UUID uuid : membersAccess.keySet()) {
            if (membersAccess.get(uuid).equals(access)) {
                output.add(uuid);
            }
        }
        return output;
    }

    public Collection<String> getEveryMemberNameWithCertainAccessLevel(AccessLevelEnum access) {
        Collection<String> output = new ArrayList<>();
        for (UUID uuid : membersAccess.keySet()) {
            if (membersAccess.get(uuid).equals(access)) {
                OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
                if (p.getName() == null) continue;
                output.add(p.getName());
            }
        }
        return output;
    }

    public Optional<AccessLevelEnum> promoteOrAdd(Player target, Player p) throws SQLException {
        if (!this.membersAccess.containsKey(target.getUniqueId())) {
            this.membersAccess.put(target.getUniqueId(), AccessLevelEnum.CITIZEN);
            SettleDBstuff.changeMemberAccess(this.id, target.getUniqueId(), AccessLevelEnum.CITIZEN);
            SettlementClaim.addOrRemoveFromSettlement(target, this, true);
            return Optional.of(AccessLevelEnum.CITIZEN);
        }
        AccessLevelEnum accessLevelEnum = this.membersAccess.get(target.getUniqueId());
        if (accessLevelEnum.equals(AccessLevelEnum.CITIZEN)) {
            this.membersAccess.replace(target.getUniqueId(), AccessLevelEnum.COUNCIL);
            SettleDBstuff.changeMemberAccess(this.id, target.getUniqueId(), AccessLevelEnum.COUNCIL);
            return Optional.of(AccessLevelEnum.COUNCIL);
        }
        if (accessLevelEnum.equals(AccessLevelEnum.COUNCIL)) {
            this.membersAccess.replace(target.getUniqueId(), AccessLevelEnum.VICE);
            SettleDBstuff.changeMemberAccess(this.id, target.getUniqueId(), AccessLevelEnum.VICE);
            return Optional.of(AccessLevelEnum.VICE);
        }
        if (accessLevelEnum.equals(AccessLevelEnum.VICE) || accessLevelEnum.equals(AccessLevelEnum.MAJOR))
            p.sendMessage(Chat.errorFade(String.format("Der Spieler %s hat bereits den h\u00F6chstm\u00F6glichen Rang erreicht.", PlainTextComponentSerializer.plainText().serialize(target.displayName()))));
        return Optional.empty();
    }

    public Optional<AccessLevelEnum> demoteOrRemove(Player target, Player p) throws SQLException {
        if (!this.membersAccess.containsKey(target.getUniqueId()) || this.membersAccess.get(target.getUniqueId()).equals(AccessLevelEnum.MAJOR))
            return Optional.empty();
        AccessLevelEnum accessLevelEnum = this.membersAccess.get(target.getUniqueId());
        if (accessLevelEnum.equals(AccessLevelEnum.CITIZEN)) {
            this.membersAccess.remove(target.getUniqueId());
            SettleDBstuff.changeMemberAccess(this.id, target.getUniqueId(), AccessLevelEnum.REMOVE);
            SettlementClaim.addOrRemoveFromSettlement(target, this, false);
            p.sendMessage(Chat.greenFade(String.format("Der Spieler %s wurde von deiner Stadt entfernt.", PlainTextComponentSerializer.plainText().serialize(target.displayName()))));
            return Optional.of(AccessLevelEnum.REMOVE);
        }
        if (accessLevelEnum.equals(AccessLevelEnum.COUNCIL)) {
            this.membersAccess.replace(target.getUniqueId(), AccessLevelEnum.CITIZEN);
            SettleDBstuff.changeMemberAccess(this.id, target.getUniqueId(), AccessLevelEnum.CITIZEN);
            return Optional.of(AccessLevelEnum.CITIZEN);
        }
        if (accessLevelEnum.equals(AccessLevelEnum.VICE)) {
            this.membersAccess.replace(target.getUniqueId(), AccessLevelEnum.COUNCIL);
            SettleDBstuff.changeMemberAccess(this.id, target.getUniqueId(), AccessLevelEnum.COUNCIL);
            return Optional.of(AccessLevelEnum.COUNCIL);
        }
        p.sendMessage(Chat.errorFade(String.format("Der Spieler %s hat bereits den h\u00F6chstm\u00F6glichen Rang erreicht.", PlainTextComponentSerializer.plainText().serialize(target.displayName()))));
        return Optional.empty();
    }

    public void levelUP() {
        Objective progressObjective = this.objective;
        Objective goalObjective;

        if (!(NationsPlugin.levelObjectives.size() + 1 == this.level)) {
            goalObjective = NationsPlugin.levelObjectives.get(this.level);
        } else {
            goalObjective = new Objective(0, 0, 0, 0, 0, "Coming Soon...", "Coming Soon...", "Coming Soon...", "Coming Soon...");
        }

        boolean canLevelup = progressObjective.getObjective_a() == goalObjective.getObjective_a() && progressObjective.getObjective_b() == goalObjective.getObjective_b() &&
                progressObjective.getObjective_c() == goalObjective.getObjective_c() && progressObjective.getObjective_d() == goalObjective.getObjective_d();

        if (!canLevelup) return;

        this.level++;
        this.objective = new Objective(this.objective.getScore(), 0, 0, 0, 0, null, null, null, null);
        SettleDBstuff.setLevel(this.id, level);
        getCitizensNPCbySUUID();
        HologramTrait hologramTrait = npc.getOrAddTrait(HologramTrait.class);
        hologramTrait.clear();
        hologramTrait.addLine(String.format("<#B0EB94>Level: [%s]", this.level));
    }

    public void contributeObjective(Player p, String objective) {
        Objective progressObjective = this.objective;
        Objective goalObjective;

        if (!(NationsPlugin.levelObjectives.size() + 1 == this.level)) {
            goalObjective = NationsPlugin.levelObjectives.get(this.level);
        } else {
            goalObjective = new Objective(0, 0, 0, 0, 0, "Coming Soon...", "Coming Soon...", "Coming Soon...", "Coming Soon...");
        }

        switch (objective) {
            case "a":
                int chargeda = chargeStrict(p, goalObjective.getMaterial_a(), goalObjective.getObjective_a() - progressObjective.getObjective_a(), false);
                if (chargeda <= 0) return;
                progressObjective.setObjective_a(progressObjective.getObjective_a() + chargeda);
                this.setObjectives(progressObjective);
            case "b":
                int chargedb = chargeStrict(p, goalObjective.getMaterial_b(), goalObjective.getObjective_b() - progressObjective.getObjective_b(), false);
                if (chargedb <= 0) return;
                progressObjective.setObjective_b(progressObjective.getObjective_b() + chargedb);
                this.setObjectives(progressObjective);
            case "c":
                int chargedc = chargeStrict(p, goalObjective.getMaterial_c(), goalObjective.getObjective_c() - progressObjective.getObjective_c(), false);
                if (chargedc <= 0) return;
                progressObjective.setObjective_c(progressObjective.getObjective_c() + chargedc);
                this.setObjectives(progressObjective);
            case "d":
                int chargedd = chargeStrict(p, goalObjective.getMaterial_d(), goalObjective.getObjective_d() - progressObjective.getObjective_d(), false);
                if (chargedd <= 0) return;
                progressObjective.setObjective_d(progressObjective.getObjective_d() + chargedd);
                this.setObjectives(progressObjective);
        }


    }

    private Integer chargeStrict(Player p, String itemString, int amount, boolean onlyFullCharge) {

        ItemStack item;

        if (OraxenItems.exists(itemString)) {
            item = OraxenItems.getItemById(itemString).build();
        } else {
            item = new ItemStack(Material.valueOf(itemString));
        }

        ItemStack[] stacks = p.getInventory().getContents();
        int total = 0;

        if (onlyFullCharge) {
            for (ItemStack stack : stacks) {
                if (stack == null || !stack.isSimilar(item)) continue;
                total += stack.getAmount();
            }
            if (total < amount) return -1;
        }

        total = amount;


        for (int i = 0; i < stacks.length; i++) {
            if (stacks[i] == null || !stacks[i].isSimilar(item)) continue;


            int stackAmount = stacks[i].getAmount();
            int n = total;
            if (stackAmount < total) {
                stacks[i] = null;
                total -= stackAmount;
            } else {
                stacks[i].setAmount(stackAmount - total);
                total -= total;
                break;
            }
        }
        p.getInventory().setContents(stacks);
        p.updateInventory();
        return amount - total;

    }

    public ProtectedRegion getWorldguardRegion() {

        World world = Bukkit.getWorld("world");
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        assert world != null;
        RegionManager regions = container.get(BukkitAdapter.adapt(world));
        assert regions != null;
        for (ProtectedRegion region : regions.getRegions().values()) {
            if (region.getFlag(SettlementFlag.SETTLEMENT_UUID_FLAG) == null) continue;
            UUID settlementUUID = UUID.fromString(Objects.requireNonNull(region.getFlag(SettlementFlag.SETTLEMENT_UUID_FLAG)));
            if (this.id.equals(settlementUUID)) {
                return region;
            }

        }
        return null;
    }

    public void setObjectives(Objective objective) {
        this.objective = objective;
        SettleDBstuff.syncObjectives(this.id, this.objective.getObjective_a(), this.objective.getObjective_b(), this.objective.getObjective_c(), this.objective.getObjective_d());
    }

    public int getMaxClaims() {
        int claims = 9;
        if (this.level <= 1) return claims;
        for (int i = 0; i <= this.level - 2; i++) claims += SettlementManager.claimsPerLevel.get(i);
        return claims;
    }
}

