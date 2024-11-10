package de.terranova.nations.settlements.PropertyTypeClasses;

import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.database.SettleDBstuff;
import de.terranova.nations.settlements.AccessLevel;
import de.terranova.nations.settlements.PropertyType;
import de.terranova.nations.settlements.SettleManager;
import de.terranova.nations.settlements.level.Objective;
import de.terranova.nations.worldguard.SettleClaim;
import de.terranova.nations.worldguard.math.Vectore2;
import io.th0rgal.oraxen.api.OraxenItems;
import net.citizensnpcs.trait.HologramTrait;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.*;

public class SettlementPropertyType extends PropertyType {

    public final Vectore2 location;
    public int level;

    public Objective objective;
    public HashMap<UUID, AccessLevel> membersAccess = new HashMap<>();
    public int claims;

    //Beim neu erstellen
    public SettlementPropertyType(String name, Player p) {
        super(name, UUID.randomUUID());
        //INIT
        this.location = SettleClaim.getSChunkMiddle(p.getLocation());
        NationsPlugin.settleManager.locations.add(this.location);
        this.level = 1;
        this.membersAccess.put(p.getUniqueId(), AccessLevel.MAJOR);
        this.region = SettleClaim.createClaim(name, p, this.id);
        this.objective = new Objective(0, 0, 0, 0, 0, null, null, null, null);
        //POST INIT
        this.npc = createNPC(name, p.getLocation(), this.id);
        setLevel();
        this.claims = SettleClaim.getClaimAnzahl(this.id);
        Set<EntityType> set = new HashSet<>(Arrays.asList(com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:zombie_villager"), com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:zombie"), com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:spider"),
                com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:skeleton"), com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:enderman"), com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:phantom"), com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:drowned"),
                com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:witch"), com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:pillager"), com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:husk"),
                com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:creeper")
        ));
        region.setFlag(Flags.DENY_SPAWN, set);
        region.setFlag(Flags.PVP, StateFlag.State.DENY);
    }

    //Von der Datenbank
    public SettlementPropertyType(UUID settlementUUID, HashMap<UUID, AccessLevel> membersAccess, Vectore2 location, String name, int level, Objective objective) {
        super(name, settlementUUID);
        this.location = SettleClaim.getSChunkMiddle(location);
        NationsPlugin.settleManager.locations.add(SettleClaim.getSChunkMiddle(location));
        this.level = level;
        this.membersAccess = membersAccess;
        this.region = getWorldguardRegion();
        this.objective = objective;
        this.claims = SettleClaim.getClaimAnzahl(settlementUUID);
        //funktioniert nicht im Constructor
        getCitizensNPCbySUUID();
    }

    public Collection<UUID> getEveryUUIDWithCertainAccessLevel(AccessLevel access) {
        Collection<UUID> output = new ArrayList<>();
        for (UUID uuid : membersAccess.keySet()) {
            if (membersAccess.get(uuid).equals(access)) {
                output.add(uuid);
            }
        }
        return output;
    }

    public Collection<String> getEveryMemberNameWithCertainAccessLevel(AccessLevel access) {
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

    public Optional<AccessLevel> promoteOrAdd(Player target, Player p) throws SQLException {
        SettleDBstuff settleDB = new SettleDBstuff(this.id);
        if (!this.membersAccess.containsKey(target.getUniqueId())) {
            this.membersAccess.put(target.getUniqueId(), AccessLevel.CITIZEN);
            settleDB.changeMemberAccess(target.getUniqueId(), AccessLevel.CITIZEN);
            SettleClaim.addOrRemoveFromSettlement(target, this, true);
            return Optional.of(AccessLevel.CITIZEN);
        }
        AccessLevel accessLevelEnum = this.membersAccess.get(target.getUniqueId());
        if (accessLevelEnum.equals(AccessLevel.CITIZEN)) {
            this.membersAccess.replace(target.getUniqueId(), AccessLevel.COUNCIL);
            settleDB.changeMemberAccess(target.getUniqueId(), AccessLevel.COUNCIL);
            return Optional.of(AccessLevel.COUNCIL);
        }
        if (accessLevelEnum.equals(AccessLevel.COUNCIL)) {
            this.membersAccess.replace(target.getUniqueId(), AccessLevel.VICE);
            settleDB.changeMemberAccess(target.getUniqueId(), AccessLevel.VICE);
            return Optional.of(AccessLevel.VICE);
        }
        if (accessLevelEnum.equals(AccessLevel.VICE) || accessLevelEnum.equals(AccessLevel.MAJOR))
            p.sendMessage(Chat.errorFade(String.format("Der Spieler %s hat bereits den h\u00F6chstm\u00F6glichen Rang erreicht.", PlainTextComponentSerializer.plainText().serialize(target.displayName()))));
        return Optional.empty();
    }

    public Optional<AccessLevel> demoteOrRemove(Player target, Player p) throws SQLException {
        if (!this.membersAccess.containsKey(target.getUniqueId()) || this.membersAccess.get(target.getUniqueId()).equals(AccessLevel.MAJOR))
            return Optional.empty();
        AccessLevel accessLevelEnum = this.membersAccess.get(target.getUniqueId());
        SettleDBstuff settleDB = new SettleDBstuff(this.id);
        if (accessLevelEnum.equals(AccessLevel.CITIZEN)) {
            this.membersAccess.remove(target.getUniqueId());
            settleDB.changeMemberAccess(target.getUniqueId(), AccessLevel.REMOVE);
            SettleClaim.addOrRemoveFromSettlement(target, this, false);
            p.sendMessage(Chat.greenFade(String.format("Der Spieler %s wurde von deiner Stadt entfernt.", PlainTextComponentSerializer.plainText().serialize(target.displayName()))));
            return Optional.of(AccessLevel.REMOVE);
        }
        if (accessLevelEnum.equals(AccessLevel.COUNCIL)) {
            this.membersAccess.replace(target.getUniqueId(), AccessLevel.CITIZEN);
            settleDB.changeMemberAccess(target.getUniqueId(), AccessLevel.CITIZEN);
            return Optional.of(AccessLevel.CITIZEN);
        }
        if (accessLevelEnum.equals(AccessLevel.VICE)) {
            this.membersAccess.replace(target.getUniqueId(), AccessLevel.COUNCIL);
            settleDB.changeMemberAccess(target.getUniqueId(), AccessLevel.COUNCIL);
            return Optional.of(AccessLevel.COUNCIL);
        }
        p.sendMessage(Chat.errorFade(String.format("Der Spieler %s hat bereits den h\u00F6chstm\u00F6glichen Rang erreicht.", PlainTextComponentSerializer.plainText().serialize(target.displayName()))));
        return Optional.empty();
    }

    public void setLevel(){
        getCitizensNPCbySUUID();
        HologramTrait hologramTrait = npc.getOrAddTrait(HologramTrait.class);
        hologramTrait.clear();
        hologramTrait.addLine(String.format("<#B0EB94>Level: [%s]", this.level));
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
        SettleDBstuff settleDB = new SettleDBstuff(this.id);
        settleDB.setLevel(level);
        setLevel();
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

    public void setObjectives(Objective objective) {
        this.objective = objective;
        SettleDBstuff settleDB = new SettleDBstuff(this.id);
        settleDB.syncObjectives(this.objective.getObjective_a(), this.objective.getObjective_b(), this.objective.getObjective_c(), this.objective.getObjective_d());
    }

    public int getMaxClaims() {
        int claims = 9;
        if (this.level <= 1) return claims;
        for (int i = 0; i <= this.level - 2; i++) claims += SettleManager.claimsPerLevel.get(i);
        return claims;
    }

    public void remove() {
        removeNPC();
        removeWGRegion();
        NationsPlugin.settleManager.locations.remove(this.location);
    }

}
