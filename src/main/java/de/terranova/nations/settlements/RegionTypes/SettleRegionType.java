package de.terranova.nations.settlements.RegionTypes;

import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import de.mcterranova.terranovaLib.InventoryUtil.ItemTransfer;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.database.SettleDBstuff;
import de.terranova.nations.settlements.AccessLevel;
import de.terranova.nations.settlements.RegionType;
import de.terranova.nations.settlements.level.Objective;
import de.terranova.nations.worldguard.RegionClaimFunctions;
import de.terranova.nations.worldguard.math.Vectore2;
import de.terranova.nations.worldguard.math.claimCalc;
import net.citizensnpcs.trait.HologramTrait;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.*;

public class SettleRegionType extends RegionType implements Bank, GridRegion, Access {

    public final Vectore2 location;
    public int level;
    private int bank;
    public Objective objective;
    private HashMap<UUID, AccessLevel> accessLevel = new HashMap<>();
    public List<Transaction> transactionHistory = new ArrayList<>();
    public int claims;
    private boolean isCashInProgress = false;

    //Beim neu erstellen
    public SettleRegionType(String name, Player p) {
        super(name, UUID.randomUUID(), "settle");
        this.location = RegionClaimFunctions.getSChunkMiddle(p.getLocation());
        NationsPlugin.settleManager.locationCache.add(this.location);
        NationsPlugin.settleManager.nameCache.add(this.name);
        this.level = 1;
        this.accessLevel.put(p.getUniqueId(), AccessLevel.MAJOR);
        this.region = RegionClaimFunctions.createClaim(name, p, this.id);
        this.objective = new Objective(0, 0, 0, 0, 0, null, null, null);
        this.npc = createNPC(name, p.getLocation(), this.id);
        setLevel();
        this.claims = RegionClaimFunctions.getClaimAnzahl(this.id);
        Set<EntityType> set = getDeniedSpawnEntityTypes();
        region.setFlag(Flags.DENY_SPAWN, set);
        region.setFlag(Flags.PVP, StateFlag.State.DENY);
    }

    //Von der Datenbank
    public SettleRegionType(UUID settlementUUID, Vectore2 location, String name, int level, Objective objective) {
        super(name, settlementUUID, "settle");
        this.location = RegionClaimFunctions.getSChunkMiddle(location);
        NationsPlugin.settleManager.locationCache.add(RegionClaimFunctions.getSChunkMiddle(location));
        NationsPlugin.settleManager.nameCache.add(this.name);
        this.level = level;
        this.transactionHistory = dataBaseRetrieveBank();
        this.accessLevel = dataBaseRetrieveAccess();
        this.region = getWorldguardRegion();
        this.objective = objective;
        this.bank = objective.getSilver();
        this.claims = RegionClaimFunctions.getClaimAnzahl(settlementUUID);
        //funktioniert nicht im Constructor
        getCitizensNPCbySUUID();
    }

    //Bedingungen Überprüfen
    public static void conditionCheck(Player p, String name) {
        if (!isValidName(name)) {
            p.sendMessage(Chat.errorFade("Bitte verwende keine Sonderzeichen im Stadtnamen. Statt Leerzeichen _ verwenden. Nicht weniger als 3 oder mehr als 20 Zeichen verwenden."));
            return;
        }
        if (isInBlacklistedBiome(p)) {
            p.sendMessage(Chat.errorFade("Bitte platziere deinen ersten Claim auf Festland oder Inseln. (Strand ausgenommen)"));
            return;
        }
        if (!NationsPlugin.settleManager.isNameAvaible(name)) {
            p.sendMessage(Chat.errorFade("Der Name ist leider bereits vergeben."));
            return;
        }
        if (RegionClaimFunctions.checkAreaForSettles(p)) {
            p.sendMessage(Chat.errorFade("Der Claim ist bereits in Besitz eines anderen Spielers."));
            return;
        }
        if (isTooCloseToAnotherSettlement(p)) {
            p.sendMessage(Chat.errorFade("Du bist zu nah an einer anderen Stadt, mindestens <#8769FF>2000<#FFD7FE> Blöcke Abstand muss eingehalten werden."));
            p.sendMessage(Chat.errorFade(String.format("Die nächste Stadt ist <#8769FF>%s<#FFD7FE> meter von dir entfernt.", (int) Math.floor(getClosestSettlementDistance(p)))));
            return;
        }

        createNewSettlement(p, name);
    }

    private static boolean isValidName(String name) {
        return name.matches("^[a-zA-Z0-9_]{1,20}$");
    }

    private static boolean isInBlacklistedBiome(Player p) {
        List<String> biomeblacklist = new ArrayList<>(Arrays.asList("RIVER", "DEEP_COLD_OCEAN", "COLD_OCEAN", "DEEP_LUKEWARM_OCEAN", "LUKEWARM_OCEAN", "OCEAN", "DEEP_OCEAN", "WARM_OCEAN", "DEEP_WARM_OCEAN", "BEACH", "GRAVEL_BEACH", "SNOWY_BEACH"));
        String currentbiome = p.getWorld().getBiome(p.getLocation()).toString();
        return biomeblacklist.contains(currentbiome);
    }

    private static boolean isTooCloseToAnotherSettlement(Player p) {
        return getClosestSettlementDistance(p) < 2000;
    }

    private static double getClosestSettlementDistance(Player p) {
        double minDistance = Integer.MAX_VALUE;
        for (Vectore2 location : NationsPlugin.settleManager.locationCache) {
            double distance = claimCalc.abstand(location, new Vectore2(p.getLocation()));
            if (distance < minDistance) {
                minDistance = distance;
            }
        }
        return minDistance;
    }

    private static void createNewSettlement(Player p, String name) {
        SettleRegionType settle = new SettleRegionType(name, p);
        NationsPlugin.settleManager.addSettlement(settle.id, settle);
        SettleDBstuff.addSettlement(settle.id, settle.name, new Vectore2(p.getLocation()), p.getUniqueId());
        NationsPlugin.settleManager.addSettlementToPl3xmap(settle);
        p.sendMessage(Chat.greenFade("Deine Stadt " + settle.name + " wurde erfolgreich gegründet."));
    }

    private Set<EntityType> getDeniedSpawnEntityTypes() {
        return new HashSet<>(Arrays.asList(
                com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:zombie_villager"),
                com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:zombie"),
                com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:spider"),
                com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:skeleton"),
                com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:enderman"),
                com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:phantom"),
                com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:drowned"),
                com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:witch"),
                com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:pillager"),
                com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:husk"),
                com.sk89q.worldedit.world.entity.EntityType.REGISTRY.get("minecraft:creeper")
        ));
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

    public void setLevel() {
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
            goalObjective = new Objective(0, 0, 0, 0, 0, "Coming Soon...", "Coming Soon...", "Coming Soon...");
        }

        boolean canLevelup = progressObjective.getObjective_a() == goalObjective.getObjective_a() && progressObjective.getObjective_b() == goalObjective.getObjective_b() &&
                progressObjective.getObjective_c() == goalObjective.getObjective_c();

        if (!canLevelup) return;

        this.level++;
        this.objective = new Objective(this.objective.getScore(), 0, 0, 0, 0, null, null, null);
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
            goalObjective = new Objective(0, 0, 0, 0, 0, "Coming Soon...", "Coming Soon...", "Coming Soon...");
        }

        switch (objective) {
            case "a":
                int chargeda = ItemTransfer.charge(p, goalObjective.getMaterial_a(), goalObjective.getObjective_a() - progressObjective.getObjective_a(), false);
                if (chargeda <= 0) return;
                progressObjective.setObjective_a(progressObjective.getObjective_a() + chargeda);
                this.setObjectives(progressObjective);
            case "b":
                int chargedb = ItemTransfer.charge(p, goalObjective.getMaterial_b(), goalObjective.getObjective_b() - progressObjective.getObjective_b(), false);
                if (chargedb <= 0) return;
                progressObjective.setObjective_b(progressObjective.getObjective_b() + chargedb);
                this.setObjectives(progressObjective);
            case "c":
                int chargedc = ItemTransfer.charge(p, goalObjective.getMaterial_c(), goalObjective.getObjective_c() - progressObjective.getObjective_c(), false);
                if (chargedc <= 0) return;
                progressObjective.setObjective_c(progressObjective.getObjective_c() + chargedc);
                this.setObjectives(progressObjective);
        }


    }

    public void setObjectives(Objective objective) {
        this.objective = objective;
        SettleDBstuff settleDB = new SettleDBstuff(this.id);
        settleDB.syncObjectives(this.objective.getObjective_a(), this.objective.getObjective_b(), this.objective.getObjective_c());
    }


    //Bank
    @Override
    public int getBank() {
        return this.bank;
    }

    @Override
    public void setBank(int i) {
        this.bank = i;
    }

    @Override
    public boolean getCashInProgress() {
        return isCashInProgress;
    }

    @Override
    public void setCashInProgress(boolean state) {
        this.isCashInProgress = state;
    }

    @Override
    public void dataBaseCallBank(int value, int amount, String username, Timestamp timestamp) {
        SettleDBstuff settleDB = new SettleDBstuff(this.id);
        settleDB.cash(bank, amount, username, timestamp);
    }

    @Override
    public List<Transaction> dataBaseRetrieveBank() {
        SettleDBstuff settleDB = new SettleDBstuff(this.id);
        try {
            return settleDB.getTransactionHistory();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return List.of();
    }

    @Override
    public List<Transaction> getTransactionHistory() {
        return this.transactionHistory;
    }

    @Override
    public void setTransactionHistory(List<Transaction> transactions) {
        this.transactionHistory = transactions;
    }

    //Access
    @Override
    public HashMap<UUID, AccessLevel> getAccessLevels() {
        return this.accessLevel;
    }

    @Override
    public void setAccessLevels(HashMap<UUID, AccessLevel> accessLevels) {
        this.accessLevel = accessLevels;
    }

    @Override
    public void dataBaseCallAccess(UUID PUUID, AccessLevel access) {
        SettleDBstuff settleDB = new SettleDBstuff(this.id);
        settleDB.changeMemberAccess(PUUID, access);
    }

    @Override
    public HashMap<UUID, AccessLevel> dataBaseRetrieveAccess() {
        SettleDBstuff settleDB = new SettleDBstuff(this.id);
        try {
            return settleDB.getMembersAccess();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    //GridRegion
    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public Vectore2 getLocation() {
        return this.location;
    }

    @Override
    public int getClaims() {
        return this.claims;
    }

    @Override
    public void setClaims(int claims) {
        this.claims = claims;
    }


}
