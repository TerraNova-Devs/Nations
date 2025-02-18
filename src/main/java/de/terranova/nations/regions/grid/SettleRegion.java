package de.terranova.nations.regions.grid;

import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import de.mcterranova.terranovaLib.utils.Chat;
import de.terranova.nations.regions.access.TownAccess;
import de.terranova.nations.regions.access.TownAccessControlled;
import de.terranova.nations.regions.access.TownAccessLevel;
import de.terranova.nations.regions.bank.Bank;
import de.terranova.nations.regions.bank.BankHolder;
import de.terranova.nations.regions.RegionManager;
import de.terranova.nations.regions.base.GridRegion;
import de.terranova.nations.regions.npc.NPCHolder;
import de.terranova.nations.regions.npc.NPCr;
import de.terranova.nations.pl3xmap.RegionLayer;
import de.terranova.nations.regions.rank.Rank;
import de.terranova.nations.regions.rank.RankedRegion;
import de.terranova.nations.worldguard.math.Vectore2;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SettleRegion extends GridRegion implements BankHolder, TownAccessControlled, NPCHolder, RankedRegion {

    public static final String REGION_TYPE = "settle";
    public static List<Integer> claimsPerLevel = new ArrayList<>(Arrays.asList(3, 3, 3, 3, 5, 3, 3, 3, 3, 5));
    private final Rank rank;
    private final NPCr npc;
    private final TownAccess access;
    private final Bank bank;



    public SettleRegion(String name, UUID ruuid, Vectore2 loc) {
        super(name, ruuid, REGION_TYPE, loc);
        addNameToCache(this.name);
        this.rank = new Rank(this);
        this.access = new TownAccess(this);
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
        access.setAccessLevel(p.getUniqueId(), TownAccessLevel.MAJOR);
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
    public TownAccess getAccess() {
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