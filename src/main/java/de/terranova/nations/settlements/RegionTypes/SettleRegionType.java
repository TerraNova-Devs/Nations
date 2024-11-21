package de.terranova.nations.settlements.RegionTypes;

import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import de.mcterranova.terranovaLib.utils.Chat;
import de.mcterranova.terranovaLib.violetData.violetSerialization;
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
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

import static de.mcterranova.terranovaLib.violetData.violetSerialization.*;

public class SettleRegionType extends RegionType {

    public final Vectore2 location;
    public int level;
    public int bank;

    public Objective objective;
    public HashMap<UUID, AccessLevel> accessLevel = new HashMap<>();
    public List<Transaction> transactionHistory = new ArrayList<>();
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
        this.objective = new Objective(0, 0, 0, 0, 0, null, null, null);
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
    public SettleRegionType(UUID settlementUUID, HashMap<UUID, AccessLevel> accessLevel, Vectore2 location, String name, int level, Objective objective, List<Transaction> transactions) {
        super(name, settlementUUID, "settle");
        this.location = RegionClaimFunctions.getSChunkMiddle(location);
        NationsPlugin.settleManager.locationCache.add(RegionClaimFunctions.getSChunkMiddle(location));
        NationsPlugin.settleManager.nameCache.add(this.name);
        this.level = level;
        this.transactionHistory = transactions;
        this.accessLevel = accessLevel;
        this.region = getWorldguardRegion();
        this.objective = objective;
        this.bank = objective.getSilver();
        this.claims = RegionClaimFunctions.getClaimAnzahl(settlementUUID);
        //funktioniert nicht im Constructor
        getCitizensNPCbySUUID();
    }

    //Bedingungen Überprüfen
    public static void conditionCheck(Player p, String name) {;
        if (!name.matches("^[a-zA-Z0-9_]{1,20}$")) {
            p.sendMessage(Chat.errorFade("Bitte verwende keine Sonderzeichen im Stadtnamen. Statt Leerzeichen _ verwenden. Nicht weniger als 3 oder mehr als 20 Zeichen verwenden."));
            return;
        }
        List<String> biomeblacklist = new ArrayList<>(Arrays.asList("RIVER", "DEEP_COLD_OCEAN", "COLD_OCEAN", "DEEP_LUKEWARM_OCEAN", "LUKEWARM_OCEAN", "OCEAN", "DEEP_OCEAN", "WARM_OCEAN", "DEEP_WARM_OCEAN", "BEACH", "GRAVEL_BEACH", "SNOWY_BEACH"));
        String currentbiome = p.getWorld().getBiome(p.getLocation()).toString();
        for (String biome : biomeblacklist) {
            if (biome.equalsIgnoreCase(currentbiome)) {
                p.sendMessage(Chat.errorFade("Bitte platziere deinen ersten Claim auf Festland oder Inseln. (Strand ausgenommen)"));
                return;
            }
        }
        if (!NationsPlugin.settleManager.isNameAvaible(name)) {
            p.sendMessage(Chat.errorFade("Der Name ist leider bereits vergeben."));
            return;
        }
        if (RegionClaimFunctions.checkAreaForSettles(p)) {
            p.sendMessage(Chat.errorFade("Der Claim ist bereits in Besitz eines anderen Spielers."));
            return;
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
            return;
        }

        SettleRegionType settle = new SettleRegionType(name, p);

        NationsPlugin.settleManager.addSettlement(settle.id, settle);
        SettleDBstuff.addSettlement(settle.id, settle.name, new Vectore2(p.getLocation()), p.getUniqueId());
        NationsPlugin.settleManager.addSettlementToPl3xmap(settle);

        p.sendMessage(Chat.greenFade("Deine Stadt " + settle.name + " wurde erfolgreich gegründet."));
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


    public AccessLevel getAccessLevel(UUID uuid) {
        return this.accessLevel.get(uuid);
    }

    public HashMap<UUID, AccessLevel> getAccessLevel() {
        return this.accessLevel;
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
            goalObjective = new Objective(0, 0, 0, 0, 0,"Coming Soon...", "Coming Soon...", "Coming Soon...");
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
            goalObjective = new Objective(0, 0, 0, 0, 0,  "Coming Soon...", "Coming Soon...", "Coming Soon...");
        }

        switch (objective) {
            case "a":
                int chargeda = charge(p, goalObjective.getMaterial_a(), goalObjective.getObjective_a() - progressObjective.getObjective_a(), false);
                if (chargeda <= 0) return;
                progressObjective.setObjective_a(progressObjective.getObjective_a() + chargeda);
                this.setObjectives(progressObjective);
            case "b":
                int chargedb = charge(p, goalObjective.getMaterial_b(), goalObjective.getObjective_b() - progressObjective.getObjective_b(), false);
                if (chargedb <= 0) return;
                progressObjective.setObjective_b(progressObjective.getObjective_b() + chargedb);
                this.setObjectives(progressObjective);
            case "c":
                int chargedc = charge(p, goalObjective.getMaterial_c(), goalObjective.getObjective_c() - progressObjective.getObjective_c(), false);
                if (chargedc <= 0) return;
                progressObjective.setObjective_c(progressObjective.getObjective_c() + chargedc);
                this.setObjectives(progressObjective);
        }


    }

    private static boolean cashInProgress = false;

    public void cashIn(Player p,int amount) {
        if(cashInProgress){
            p.sendMessage(Chat.errorFade("A error occured while using the bank please try again."));
            return;
        }
        cashInProgress = true;
        int charged = charge(p,"terranova_silver",amount,false);
        SettleDBstuff settleDB = new SettleDBstuff(this.id);
        if(transactionHistory.size() >= 50) transactionHistory.removeFirst();
        Timestamp time = databaseTimestampSE(Instant.now());
        transactionHistory.add(new Transaction(p.getName(),charged, time));
        Bukkit.getLogger().severe(String.format("Spieler %s -> Stadt %s -> %s eingezahlt, Gesamtbetrag: %s",p.getName(),charged,this.name, this.bank+charged));

        settleDB.cash( bank+charged, charged,p.getName(),time);



        bank+=charged;
        p.sendMessage(Chat.greenFade(String.format("Du hast erfolgreich %s in die Stadtkasse %s's eingezahlt, neuer Gesamtbetrag: %s.",charged,this.name,this.bank)));
        cashInProgress = false;
    }

    public void cashOut(Player p,int amount) {
        if(cashInProgress){
            p.sendMessage(Chat.errorFade("A error occured while using the bank please try again."));
            return;
        }
        cashInProgress = true;
        int credited;
        if(amount <= bank) {
            credited = credit(p, "terranova_silver", amount, false);
        } else {
            credited = credit(p, "terranova_silver", bank, false);
        }
        SettleDBstuff settleDB = new SettleDBstuff(this.id);

        if(transactionHistory.size() >= 50) transactionHistory.removeFirst();
        transactionHistory.add(new Transaction(p.getName(),-credited, Instant.now()));
        Bukkit.getLogger().severe(String.format("Spieler %s -> Stadt %s -> %s abgehoben, Gesamtbetrag: %s",p.getName(),-credited,this.name, this.bank-credited));
        Timestamp time = databaseTimestampSE(Instant.now());
        settleDB.cash(bank-credited,-credited,p.getName(),time);
        bank-=credited;
        p.sendMessage(Chat.greenFade(String.format("Du hast erfolgreich %s von der Stadtkasse %s's abgehoben, neuer Gesamtbetrag: %s.",credited,this.name,this.bank)));
        cashInProgress = false;
    }

    private Integer charge(Player p, String itemString, int amount, boolean onlyFullCharge) {

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

    private Integer credit(Player p, String itemString, int amount, boolean onlyFullCredit) {

        ItemStack item;
        if (OraxenItems.exists(itemString)) {
            item = OraxenItems.getItemById(itemString).build();
        } else {
            item = new ItemStack(Material.valueOf(itemString));
        }

        ItemStack[] stacks = p.getInventory().getContents();
        int total = 0;
        if (onlyFullCredit) {
            for (ItemStack stack : stacks) {
                if(stack == null) {
                    total += 64;
                    continue;
                }
                if (stack.isSimilar(item)) total += 64 - stack.getAmount();
            }
            if (total < amount) return -1;
        }

        total = amount;

        int stackAmount;
        for (int i = 0; i < stacks.length; i++) {

            if(stacks[i] == null) {
                stackAmount = 0;
            } else if(stacks[i].isSimilar(item)) {
                stackAmount = stacks[i].getAmount();
            } else {
                continue;
            }

            if (64 - stackAmount < total) {
                stacks[i] = item.asQuantity(64);
                total -= 64 - stackAmount;
                p.sendMessage("" + total);
            } else {
                stacks[i] = item.asQuantity(total+stackAmount);
                total -=  total;
                p.sendMessage("" + total);
                break;
            }
        }

        p.getInventory().setContents(stacks);
        p.updateInventory();
        p.sendMessage("" + total);
        return amount - total;
    }

    public void setObjectives(Objective objective) {
        this.objective = objective;
        SettleDBstuff settleDB = new SettleDBstuff(this.id);
        settleDB.syncObjectives(this.objective.getObjective_a(), this.objective.getObjective_b(), this.objective.getObjective_c());
    }

    public int getMaxClaims() {
        int claims = 9;
        if (this.level <= 1) return claims;
        for (int i = 0; i <= this.level - 2; i++) claims += SettleManager.claimsPerLevel.get(i);
        return claims;
    }

}
