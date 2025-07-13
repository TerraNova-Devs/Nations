package de.terranova.nations.regions.grid;

import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import de.terranova.nations.pl3xmap.RegionLayer;
import de.terranova.nations.regions.RegionManager;
import de.terranova.nations.regions.base.BoundaryRegion;
import de.terranova.nations.regions.base.GridRegion;
import de.terranova.nations.regions.base.Region;
import de.terranova.nations.regions.modules.HasChildren;
import de.terranova.nations.regions.modules.access.Access;
import de.terranova.nations.regions.modules.access.AccessControlled;
import de.terranova.nations.regions.modules.access.AccessLevel;
import de.terranova.nations.regions.modules.bank.Bank;
import de.terranova.nations.regions.modules.bank.BankHolder;
import de.terranova.nations.regions.modules.npc.NPCHolder;
import de.terranova.nations.regions.modules.npc.NPCr;
import de.terranova.nations.regions.modules.rank.Rank;
import de.terranova.nations.regions.modules.rank.RankedRegion;
import de.terranova.nations.utils.Chat;
import de.terranova.nations.worldguard.math.Vectore2;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SettleRegion extends GridRegion implements BankHolder, AccessControlled, NPCHolder, RankedRegion, HasChildren {

    public static final String REGION_TYPE = "settle";
    public static List<Integer> claimsPerLevel = new ArrayList<>(Arrays.asList(3, 3, 3, 3, 5, 3, 3, 3, 3, 5, 4, 4, 4, 4, 7, 4, 4, 4, 4, 10, 5, 5, 5, 5, 10));
    private final Rank rank;
    private final NPCr npc;
    private final Access access;
    private final Bank bank;
    private final Map<String, List<Region>> children = new HashMap<>();

    public SettleRegion(String name, UUID ruuid, Vectore2 loc) {
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
        RegionManager.removeRegion(type, id);
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
        RegionLayer.updateRegion(this);
        npc.hologramNPC(new String[]{String.format("<#B0EB94>Level: [%s]", rank.getLevel())});
    }

    @Override
    public Map<String, List<Region>> getChildrenMap() {
        return children;
    }

    public int getAvaibleRegionPoints(){
        int points = getMaxClaims() * 50;
        List<Region> propertyRegions = children.get("property");

        if (propertyRegions != null) {
            points -= propertyRegions.stream()
                    .filter(r -> r instanceof BoundaryRegion)
                    .map(r -> (BoundaryRegion) r)
                    .mapToInt(br -> br.isPoly() ? 25 : 5)
                    .sum();
        } else {
            points = 0;
        }
        return points;
    }
}