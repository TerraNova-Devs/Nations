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
import net.citizensnpcs.trait.HologramTrait;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Stream;

public class SettleRegionType extends GridRegionType implements BankHolder, AccessControlled, NPCHolder, RankedRegion {

    public static final String REGION_TYPE = "settle";
    private Rank rank;
    private NPCr npc;
    private Access access;
    private Bank bank;

    //Beim neu erstellen
    public SettleRegionType(String name, Player p) {
        super(name, UUID.randomUUID(), REGION_TYPE, RegionClaimFunctions.getSChunkMiddle(p.getLocation()));
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
        this.bank = new Bank(this, this.name,rankObjective.getSilver());
        this.claims = RegionClaimFunctions.getClaimAnzahl(settlementUUID);
        //funktioniert nicht im Constructor
        npc.getCitizensNPCbySUUID();
    }

    @Override
    public void postInit(Player p) {
        NationsPlugin.settleManager.locationCache.add(this.location);
        NationsPlugin.settleManager.nameCache.add(this.name);
        this.region = RegionClaimFunctions.createClaim(name, p, this.id);
        this.claims = RegionClaimFunctions.getClaimAnzahl(this.id);
        Set<EntityType> set = getDeniedSpawnEntityTypes();
        region.setFlag(Flags.DENY_SPAWN, set);
        region.setFlag(Flags.PVP, StateFlag.State.DENY);
        NationsPlugin.settleManager.addSettlement(id, this);
        SettleDBstuff.addSettlement(id, name, new Vectore2(p.getLocation()), p.getUniqueId());
        this.rank = new Rank(this, 1, new RankObjective(0, 0, 0, 0, 0, null, null, null));
        this.access = new Access(this, p.getUniqueId(), AccessLevel.MAJOR);
        //Settlement braucht access
        NationsPlugin.settleManager.addSettlementsToPl3xmap();
        this.npc = new NPCr(name, p.getLocation(), id);
        //setLevel braucht NPC
        setLevel();
        this.bank = new Bank(this, this.name);

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
        SettleDBstuff settleDB = new SettleDBstuff(this.id, this.getType());
        settleDB.cash(bank.getCredit(), amount, username, timestamp);
    }

    @Override
    public List<Transaction> dataBaseRetrieveBank() {
        SettleDBstuff settleDB = new SettleDBstuff(this.id, this.getType());
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
