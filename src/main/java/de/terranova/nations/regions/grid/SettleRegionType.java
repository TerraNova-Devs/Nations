package de.terranova.nations.regions.grid;

import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.database.SettleDBstuff;
import de.terranova.nations.regions.SettleManager;
import de.terranova.nations.regions.access.Access;
import de.terranova.nations.regions.access.AccessControlled;
import de.terranova.nations.regions.access.AccessLevel;
import de.terranova.nations.regions.bank.Bank;
import de.terranova.nations.regions.bank.BankHolder;
import de.terranova.nations.regions.bank.Transaction;
import de.terranova.nations.regions.base.GridRegionType;
import de.terranova.nations.regions.npc.NPCHolder;
import de.terranova.nations.regions.npc.NPCr;
import de.terranova.nations.regions.rank.Rank;
import de.terranova.nations.regions.rank.RankObjective;
import de.terranova.nations.regions.rank.RankedRegion;
import de.terranova.nations.worldguard.RegionClaimFunctions;
import de.terranova.nations.worldguard.math.Vectore2;
import de.terranova.nations.worldguard.math.claimCalc;
import net.citizensnpcs.trait.HologramTrait;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Stream;

public class SettleRegionType extends GridRegionType implements BankHolder, AccessControlled, NPCHolder, RankedRegion {

    public static final String type = "settle";

    private final Rank rank;
    private final NPCr npc;
    private final Access access;
    private final Bank bank;

    //Beim neu erstellen
    public SettleRegionType(String name, Player p) {
        super(isValidName(name, p), UUID.randomUUID(), type, RegionClaimFunctions.getSChunkMiddle(p.getLocation()));
        checkConditions(p);
        NationsPlugin.settleManager.locationCache.add(this.location);
        NationsPlugin.settleManager.nameCache.add(this.name);
        this.rank = new Rank(this, 1, new RankObjective(0, 0, 0, 0, 0, null, null, null));
        access = new Access(p.getUniqueId(), AccessLevel.MAJOR);
        this.region = RegionClaimFunctions.createClaim(name, p, this.id);
        this.npc = new NPCr(name, p.getLocation(), id);
        this.bank = new Bank(this);
        setLevel();
        this.claims = RegionClaimFunctions.getClaimAnzahl(this.id);
        Set<EntityType> set = getDeniedSpawnEntityTypes();
        region.setFlag(Flags.DENY_SPAWN, set);
        region.setFlag(Flags.PVP, StateFlag.State.DENY);
        createNewSettlement(p, name);
    }

    //Von der Datenbank
    public SettleRegionType(UUID settlementUUID, Vectore2 location, String name, int level, RankObjective rankObjective) {
        super(name, settlementUUID, "settle", RegionClaimFunctions.getSChunkMiddle(location));
        NationsPlugin.settleManager.locationCache.add(RegionClaimFunctions.getSChunkMiddle(location));
        NationsPlugin.settleManager.nameCache.add(this.name);
        this.npc = new NPCr(id);
        this.rank = new Rank(this, level, rankObjective);
        this.access = new Access(this);
        this.region = getWorldguardRegion();
        this.bank = new Bank(this, rankObjective.getSilver());
        this.claims = RegionClaimFunctions.getClaimAnzahl(settlementUUID);
        //funktioniert nicht im Constructor
        npc.getCitizensNPCbySUUID();
    }

    private static String isValidName(String name, Player p) {
        if (name.matches("^[a-zA-Z0-9_]{1,20}$")) {
            return name;
        }
        p.sendMessage(Chat.errorFade("Bitte verwende keine Sonderzeichen im Stadtnamen. Statt Leerzeichen _ verwenden. Nicht weniger als 3 oder mehr als 20 Zeichen verwenden."));
        throw new IllegalArgumentException("Bitte verwende keine Sonderzeichen im Stadtnamen. Statt Leerzeichen _ verwenden. Nicht weniger als 3 oder mehr als 20 Zeichen verwenden.");
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

    private static boolean isInBlacklistedBiome(Player p) {
        List<Biome> biomeblacklist = List.of(
                Biome.DEEP_OCEAN, Biome.OCEAN, Biome.WARM_OCEAN
                , Biome.FROZEN_OCEAN, Biome.LUKEWARM_OCEAN, Biome.COLD_OCEAN
                , Biome.DEEP_FROZEN_OCEAN, Biome.DEEP_LUKEWARM_OCEAN, Biome.DEEP_COLD_OCEAN
                , Biome.RIVER, Biome.BEACH, Biome.SNOWY_BEACH);
        return biomeblacklist.contains(p.getLocation().getBlock().getBiome());
    }

    public void checkConditions(Player p) {
        if (isInBlacklistedBiome(p)) {
            p.sendMessage(Chat.errorFade("Bitte platziere deinen ersten Claim auf Festland oder Inseln. (Strand ausgenommen)"));
            throw new IllegalArgumentException("Bitte platziere deinen ersten Claim auf Festland oder Inseln. (Strand ausgenommen)");
        }
        if (!NationsPlugin.settleManager.isNameAvaible(name)) {
            p.sendMessage(Chat.errorFade("Der Name ist leider bereits vergeben."));
            throw new IllegalArgumentException("Der Name ist leider bereits vergeben.");
        }
        if (RegionClaimFunctions.checkAreaForSettles(p)) {
            p.sendMessage(Chat.errorFade("Der Claim ist bereits in Besitz eines anderen Spielers."));
            throw new IllegalArgumentException("Der Claim ist bereits in Besitz eines anderen Spielers.");
        }
        if (isTooCloseToAnotherSettlement(p)) {
            p.sendMessage(Chat.errorFade("Du bist zu nah an einer anderen Stadt, mindestens <#8769FF>2000<#FFD7FE> Blöcke Abstand muss eingehalten werden."));
            p.sendMessage(Chat.errorFade(String.format("Die nächste Stadt ist <#8769FF>%s<#FFD7FE> meter von dir entfernt.", (int) Math.floor(getClosestSettlementDistance(p)))));
            throw new IllegalArgumentException("Du bist zu nah an einer anderen Stadt, mindestens <#8769FF>2000<#FFD7FE> Blöcke Abstand muss eingehalten werden.");
        }
    }

    private void createNewSettlement(Player p, String name) {
        NationsPlugin.settleManager.addSettlement(id, this);
        SettleDBstuff.addSettlement(id, name, new Vectore2(p.getLocation()), p.getUniqueId());
        NationsPlugin.settleManager.addSettlementToPl3xmap(this);
        p.sendMessage(Chat.greenFade("Deine Stadt " + name + " wurde erfolgreich gegründet."));
    }

    private Set<EntityType> getDeniedSpawnEntityTypes() {
        HashSet<EntityType> deniedEntityTypes = new HashSet<>();
        Stream.of("zombie_villager", "zombie", "spider", "skeleton", "enderman", "phantom",
                "drowned", "witch", "pillager", "husk", "creeper").forEach(entity -> deniedEntityTypes.add(new EntityType(entity)));
        return deniedEntityTypes;
    }


    //RegionType
    @Override
    public void rename(String name) {
        renameRegion(name);
        npc.renameNPC(name);
    }

    @Override
    public void remove() {
        SettleDBstuff settleDB = new SettleDBstuff(this.id, this.getType());
        settleDB.dropSettlement();
        npc.removeNPC();
        removeWGRegion();
        NationsPlugin.settleManager.locationCache.remove(this.location);
        NationsPlugin.settleManager.removeSettlement(this.id);
    }

    //GridRegionType
    @Override
    public int getMaxClaims() {
        int claims = 9;
        if (rank.getLevel() <= 1) return claims;
        for (int i = 0; i <= rank.getLevel() - 2 && i < SettleManager.claimsPerLevel.size(); i++) {
            claims += SettleManager.claimsPerLevel.get(i);
        }
        return claims;
    }

    //Bank
    @Override
    public Bank getBank() {
        return this.bank;
    }

    @Override
    public void dataBaseCallTransaction(int value, int amount, String username, Timestamp timestamp) {
        SettleDBstuff settleDB = new SettleDBstuff(this.id,this.getType());
        settleDB.cash(bank.getCredit(), amount, username, timestamp);
    }

    @Override
    public List<Transaction> dataBaseRetrieveBank() {
        SettleDBstuff settleDB = new SettleDBstuff(this.id,this.getType());
        try {
            return settleDB.getTransactionHistory();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return List.of();
    }

    //Access
    @Override
    public Access getAccess() {
        return this.access;
    }

    @Override
    public void dataBaseCallAccess(UUID PUUID, AccessLevel access) {
        SettleDBstuff settleDB = new SettleDBstuff(this.id, this.getType());
        settleDB.changeMemberAccess(PUUID, access);
    }

    @Override
    public HashMap<UUID, AccessLevel> dataBaseRetrieveAccess() {
        SettleDBstuff settleDB = new SettleDBstuff(this.id, this.getType());
        try {
            return settleDB.getMembersAccess();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    //NPC
    @Override
    public NPCr getNPC() {
        return this.npc;
    }

    public void setLevel() {
        npc.getCitizensNPCbySUUID();
        HologramTrait hologramTrait = npc.getNPC().getOrAddTrait(HologramTrait.class);
        hologramTrait.clear();
        hologramTrait.addLine(String.format("<#B0EB94>Level: [%s]", rank.getLevel()));
    }

    //Rank
    @Override
    public void onLevelUP() {
        SettleDBstuff settleDB = new SettleDBstuff(this.id, this.getType());
        settleDB.setLevel(rank.getLevel());
        setLevel();
    }

    @Override
    public Rank getRank() {
        return this.rank;
    }

    @Override
    public void dataBaseCallRank(RankObjective progressRankObjective) {
        SettleDBstuff settleDB = new SettleDBstuff(this.id, this.getType());
        settleDB.syncObjectives(rank.getRankObjective().getObjective_a(), rank.getRankObjective().getObjective_b(), rank.getRankObjective().getObjective_c());
    }

}
