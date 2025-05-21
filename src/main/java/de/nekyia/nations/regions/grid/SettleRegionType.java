package de.nekyia.nations.regions.grid;

import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import de.nekyia.nations.regions.access.Access;
import de.nekyia.nations.regions.access.AccessControlled;
import de.nekyia.nations.regions.access.AccessLevel;
import de.nekyia.nations.regions.bank.Bank;
import de.nekyia.nations.regions.bank.BankHolder;
import de.nekyia.nations.regions.base.GridRegionType;
import de.nekyia.nations.regions.RegionManager;
import de.nekyia.nations.regions.npc.NPCHolder;
import de.nekyia.nations.regions.npc.NPCr;
import de.nekyia.nations.pl3xmap.RegionLayer;
import de.nekyia.nations.regions.rank.Rank;
import de.nekyia.nations.regions.rank.RankedRegion;
import de.nekyia.nations.utils.Chat;
import de.nekyia.nations.worldguard.math.Vectore2;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SettleRegionType extends GridRegionType implements BankHolder, AccessControlled, NPCHolder, RankedRegion {

    public static final String REGION_TYPE = "settle";
    public static List<Integer> claimsPerLevel = new ArrayList<>(Arrays.asList(3, 3, 3, 3, 5, 3, 3, 3, 3, 5));
    private final Rank rank;
    private final NPCr npc;
    private final Access access;
    private final Bank bank;



    public SettleRegionType(String name, UUID ruuid, Vectore2 loc) {
        super(name, ruuid, REGION_TYPE, loc);
        addNameToCache(this.name);
        this.rank = new Rank(this);
        this.access = new Access(this);
        this.npc = new NPCr(this);
        npc.hologramNPC(new String[]{String.format("<#B0EB94>Level: [%s]", rank.getLevel())});
        this.bank = new Bank(this);
        RegionLayer.updateRegion(this);
        this.region = getWorldguardRegion();
    }

    private Set<EntityType> getDeniedSpawnEntityTypes() {
        return Stream.of("zombie_villager", "zombie", "spider", "skeleton", "enderman", "phantom", "drowned", "witch", "pillager", "husk", "creeper").map(EntityType::new).collect(Collectors.toSet());
    }

    //GridRegionType
    @Override
    public void onGridCreation(Player p) {
        Set<EntityType> set = getDeniedSpawnEntityTypes();
        region.setFlag(Flags.DENY_SPAWN, set);
        region.setFlag(Flags.PVP, StateFlag.State.DENY);
        access.setAccessLevel(p.getUniqueId(), AccessLevel.MAJOR);
        RegionLayer.updateRegion(this);
        p.sendMessage(Chat.greenFade("Deine Stadt " + name + " wurde erfolgreich gegründet."));
    }

    @Override
    public int getMaxClaims() {
        int claims = 9;
        if (rank.getLevel() <= 1) return claims;
        for (int i = 0; i <= rank.getLevel() - 2 && i < claimsPerLevel.size(); i++) {
            claims += claimsPerLevel.get(i);
        }
        return claims;
    }

    @Override
    public void onGridRemove() {
        RegionManager.removeRegion(type,id);
        RegionLayer.removeRegion(this.id);
    }

    //Bank
    @Override
    public Bank getBank() {
        return this.bank;
    }

    //Access
    @Override
    public Access getAccess() {
        return this.access;
    }

    //NPC
    @Override
    public NPCr getNPC() {
        return this.npc;
    }

    //Rank
    @Override
    public Rank getRank() {
        return this.rank;
    }

    @Override
    public void onLevelUP() {
        npc.hologramNPC(new String[]{String.format("<#B0EB94>Level: [%s]", rank.getLevel())});
    }
}