package de.terranova.nations.settlements.RegionTypes;

import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.database.SettleDBstuff;
import de.terranova.nations.settlements.AccessLevel;
import de.terranova.nations.settlements.RegionType;
import de.terranova.nations.settlements.SettleManager;
import de.terranova.nations.settlements.level.Objective;
import de.terranova.nations.worldguard.RegionClaimFunctions;
import de.terranova.nations.worldguard.math.Vectore2;
import de.terranova.nations.worldguard.math.claimCalc;
import io.th0rgal.oraxen.api.OraxenItems;
import net.citizensnpcs.trait.HologramTrait;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.*;

public class SettleRegionType extends RegionType {

    public final Vectore2 location;
    public int level;

    public Objective objective;
    private HashMap<UUID, AccessLevel> accessLevel = new HashMap<>();
    public int claims;

    //Beim neu erstellen
    public SettleRegionType(String name, Player p) {
        super(name, UUID.randomUUID(), "settle");
        this.location = RegionClaimFunctions.getSChunkMiddle(p.getLocation());
        NationsPlugin.settleManager.locationCache.add(this.location);
        NationsPlugin.settleManager.nameCache.add(this.name);
        this.level = 1;
        this.accessLevel.put(p.getUniqueId(), AccessLevel.MAJOR);
        this.region = RegionClaimFunctions.createClaim(name, p, this.id);
        this.objective = new Objective(0, 0, 0, 0, 0, null, null, null, null);
        this.npc = createNPC(name, p.getLocation(), this.id);
        setLevel();
        this.claims = RegionClaimFunctions.getClaimAnzahl(this.id);
        Set<EntityType> set = new HashSet<>(Arrays.asList(com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:zombie_villager"), com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:zombie"), com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:spider"),
                com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:skeleton"), com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:enderman"), com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:phantom"), com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:drowned"),
                com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:witch"), com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:pillager"), com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:husk"),
                com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:creeper")
        ));
        region.setFlag(Flags.DENY_SPAWN, set);
        region.setFlag(Flags.PVP, StateFlag.State.DENY);
    }

    //Von der Datenbank
    public SettleRegionType(UUID settlementUUID, HashMap<UUID, AccessLevel> accessLevel, Vectore2 location, String name, int level, Objective objective) {
        super(name, settlementUUID, "settle");
        this.location = RegionClaimFunctions.getSChunkMiddle(location);
        NationsPlugin.settleManager.locationCache.add(RegionClaimFunctions.getSChunkMiddle(location));
        NationsPlugin.settleManager.nameCache.add(this.name);
        this.level = level;
        this.accessLevel = accessLevel;
        this.region = getWorldguardRegion();
        this.objective = objective;
        this.claims = RegionClaimFunctions.getClaimAnzahl(settlementUUID);
        //funktioniert nicht im Constructor
        getCitizensNPCbySUUID();
    }

    //Bedingungen Überprüfen
    public static SettleRegionType conditionCheck(Player p, String[] args) {
        if (!(args.length >= 2)) {
            p.sendMessage(Chat.errorFade("Syntax: /settle rename <name>"));
            return null;
        }
        String name = MiniMessage.miniMessage().stripTags(String.join("_", Arrays.copyOfRange(args, 1, args.length)));
        if (!name.matches("^[a-zA-Z0-9_]{1,20}$")) {
            p.sendMessage(Chat.errorFade("Bitte verwende keine Sonderzeichen im Stadtnamen. Statt Leerzeichen _ verwenden. Nicht weniger als 3 oder mehr als 20 Zeichen verwenden."));
            return null;
        }
        List<String> biomeblacklist = new ArrayList<>(Arrays.asList("RIVER", "DEEP_COLD_OCEAN", "COLD_OCEAN", "DEEP_LUKEWARM_OCEAN", "LUKEWARM_OCEAN", "OCEAN", "DEEP_OCEAN", "WARM_OCEAN", "DEEP_WARM_OCEAN", "BEACH", "GRAVEL_BEACH", "SNOWY_BEACH"));
        String currentbiome = p.getWorld().getBiome(p.getLocation()).toString();
        for (String biome : biomeblacklist) {
            if (biome.equalsIgnoreCase(currentbiome)) {
                p.sendMessage(Chat.errorFade("Bitte platziere deinen ersten Claim auf Festland oder Inseln. (Strand ausgenommen)"));
                return null;
            }
        }
        if (!NationsPlugin.settleManager.isNameAvaible(name)) {
            p.sendMessage(Chat.errorFade("Der Name ist leider bereits vergeben."));
            return null;
        }
        if (RegionClaimFunctions.checkAreaForSettles(p)) {
            p.sendMessage(Chat.errorFade("Der Claim ist bereits in Besitz eines anderen Spielers."));
            return null;
        }
        double abstand = Integer.MAX_VALUE;
        for (Vectore2 location : NationsPlugin.settleManager.locationCache) {
            double abstandneu = claimCalc.abstand(location, new Vectore2(p.getLocation()));
            if (abstand == Integer.MAX_VALUE || abstand > abstandneu) {
                abstand = abstandneu;

            }
        }
        if (abstand < 2000) {
            p.sendMessage(Chat.errorFade("Du bist zu nah an einer anderen Stadt, mindestens <#8769FF>2000<#FFD7FE> Bl\u00F6cke Abstand muss eingehalten werden."));
            p.sendMessage(Chat.errorFade(String.format("Die n\u00E4chste Stadt ist <#8769FF>%s<#FFD7FE> meter von dir entfernt.", (int) Math.floor(abstand))));
            return null;
        }

        return new SettleRegionType(name, p);
    }

    //remove the Region
    @Override
    public void remove() {
        removeNPC();
        removeWGRegion();
        NationsPlugin.settleManager.locationCache.remove(this.location);
    }

    public Collection<UUID> getEveryUUIDWithCertainAccessLevel(AccessLevel access) {
        Collection<UUID> output = new ArrayList<>();
        for (UUID uuid : accessLevel.keySet()) {
            if (accessLevel.get(uuid).equals(access)) {
                output.add(uuid);
            }
        }
        return output;
    }

    public Collection<String> getEveryMemberNameWithCertainAccessLevel(AccessLevel access) {
        Collection<String> output = new ArrayList<>();
        for (UUID uuid : accessLevel.keySet()) {
            if (accessLevel.get(uuid).equals(access)) {
                OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
                if (p.getName() == null) continue;
                output.add(p.getName());
            }
        }
        return output;
    }

    public void setAccessLevel(UUID uuid, AccessLevel access) {
        if(this.accessLevel.containsKey(uuid)) accessLevel.replace(uuid, access);
        else accessLevel.put(uuid, access);
    }

    public void setAccessLevelNotify(Player p,Player target, AccessLevel access) {
        UUID uuid = target.getUniqueId();

        if(!this.accessLevel.containsKey(uuid) && access.equals(AccessLevel.REMOVE)) {
            p.sendMessage(Chat.errorFade(String.format("Der Spieler %s ist gerade kein Mitglied der Stadt %s.", p.getName(), this.name)));
            return;
        } else if(this.accessLevel.containsKey(uuid) && accessLevel.get(uuid).equals(access)) {
            p.sendMessage(Chat.errorFade(String.format("Der Spieler %s hat den Rang %s bereits in der Stadt %s.", p.getName(), access, this.name)));
            return;
        }

        if(access.equals(AccessLevel.REMOVE)) accessLevel.remove(uuid);
        if(this.accessLevel.containsKey(uuid)) this.accessLevel.replace(uuid, access);
        else this.accessLevel.put(uuid, access);

        SettleDBstuff settleDB = new SettleDBstuff(this.id);
        settleDB.changeMemberAccess(uuid, access);

        p.sendMessage(Chat.greenFade(String.format("Der Rang von %s wurde innerhalb der Stadt %s zu %s geändert.", target.getName(),this.name, access)));
        target.sendMessage(Chat.greenFade(String.format("Dein Rang wurde innerhalb der Stadt %s zu %s geändert.", this.name, access)));
    }

    public AccessLevel getAccessLevel(UUID uuid) {
        return this.accessLevel.get(uuid);
    }

    public HashMap<UUID, AccessLevel> getAccessLevel() {
        return this.accessLevel;
    }

    @Deprecated
    public Optional<AccessLevel> promoteOrAdd(Player target, Player p) throws SQLException {
        SettleDBstuff settleDB = new SettleDBstuff(this.id);
        if (!this.accessLevel.containsKey(target.getUniqueId())) {
            this.accessLevel.put(target.getUniqueId(), AccessLevel.CITIZEN);
            settleDB.changeMemberAccess(target.getUniqueId(), AccessLevel.CITIZEN);
            RegionClaimFunctions.addOrRemoveFromSettlement(target, this, true);
            return Optional.of(AccessLevel.CITIZEN);
        }
        AccessLevel accessLevelEnum = this.accessLevel.get(target.getUniqueId());
        if (accessLevelEnum.equals(AccessLevel.CITIZEN)) {
            this.accessLevel.replace(target.getUniqueId(), AccessLevel.COUNCIL);
            settleDB.changeMemberAccess(target.getUniqueId(), AccessLevel.COUNCIL);
            return Optional.of(AccessLevel.COUNCIL);
        }
        if (accessLevelEnum.equals(AccessLevel.COUNCIL)) {
            this.accessLevel.replace(target.getUniqueId(), AccessLevel.VICE);
            settleDB.changeMemberAccess(target.getUniqueId(), AccessLevel.VICE);
            return Optional.of(AccessLevel.VICE);
        }
        if (accessLevelEnum.equals(AccessLevel.VICE) || accessLevelEnum.equals(AccessLevel.MAJOR))
            p.sendMessage(Chat.errorFade(String.format("Der Spieler %s hat bereits den h\u00F6chstm\u00F6glichen Rang erreicht.", PlainTextComponentSerializer.plainText().serialize(target.displayName()))));
        return Optional.empty();
    }

    @Deprecated
    public Optional<AccessLevel> demoteOrRemove(Player target, Player p) throws SQLException {
        if (!this.accessLevel.containsKey(target.getUniqueId()) || this.accessLevel.get(target.getUniqueId()).equals(AccessLevel.MAJOR))
            return Optional.empty();
        AccessLevel accessLevelEnum = this.accessLevel.get(target.getUniqueId());
        SettleDBstuff settleDB = new SettleDBstuff(this.id);
        if (accessLevelEnum.equals(AccessLevel.CITIZEN)) {
            this.accessLevel.remove(target.getUniqueId());
            settleDB.changeMemberAccess(target.getUniqueId(), AccessLevel.REMOVE);
            RegionClaimFunctions.addOrRemoveFromSettlement(target, this, false);
            p.sendMessage(Chat.greenFade(String.format("Der Spieler %s wurde von deiner Stadt entfernt.", PlainTextComponentSerializer.plainText().serialize(target.displayName()))));
            return Optional.of(AccessLevel.REMOVE);
        }
        if (accessLevelEnum.equals(AccessLevel.COUNCIL)) {
            this.accessLevel.replace(target.getUniqueId(), AccessLevel.CITIZEN);
            settleDB.changeMemberAccess(target.getUniqueId(), AccessLevel.CITIZEN);
            return Optional.of(AccessLevel.CITIZEN);
        }
        if (accessLevelEnum.equals(AccessLevel.VICE)) {
            this.accessLevel.replace(target.getUniqueId(), AccessLevel.COUNCIL);
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

}
