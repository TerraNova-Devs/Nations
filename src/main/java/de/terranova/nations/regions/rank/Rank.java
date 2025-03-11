package de.terranova.nations.regions.rank;

import de.mcterranova.terranovaLib.InventoryUtil.ItemTransfer;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.regions.base.GridRegion;
import de.terranova.nations.regions.base.RegionListener;
import org.bukkit.entity.Player;

public class Rank implements RegionListener {

    RankDatabase rankDatabase;
    GridRegion regionType;
    private int level;
    private RankObjective rankObjective;
    private final RankedRegion rankedRegion;

    public Rank(GridRegion regionType) {
        if (!(regionType instanceof RankedRegion rankedRegionn)) throw new IllegalArgumentException();
        this.rankedRegion = rankedRegionn;
        this.rankDatabase = new RankDatabase(regionType.getId());
        rankDatabase.validateRank();
        int[] data = rankDatabase.fetchRank();
        this.level = data[0];
        this.rankObjective = new RankObjective(-1, -1, data[1], data[2], data[3], null, null, null);
        this.regionType = regionType;
        regionType.addListener(this);
    }

    public void levelUP() {

        RankObjective progressRankObjective = this.rankObjective;
        RankObjective goalRankObjective;

        if (!(NationsPlugin.levelObjectives.size() + 1 == this.level)) {
            goalRankObjective = NationsPlugin.levelObjectives.get(this.level);
        } else {
            goalRankObjective = new RankObjective(0, 0, 0, 0, 0, "Coming Soon...", "Coming Soon...", "Coming Soon...");
        }

        if (!(progressRankObjective.getObjective_a() == goalRankObjective.getObjective_a() && progressRankObjective.getObjective_b() == goalRankObjective.getObjective_b() &&
                progressRankObjective.getObjective_c() == goalRankObjective.getObjective_c())) return;

        this.level++;
        this.rankObjective = new RankObjective(this.rankObjective.getScore(), 0, 0, 0, 0, null, null, null);
        NationsPlugin.nationsLogger.logInfo("(LevelUp) Type: " + regionType.getType() + ", ID: " + regionType.getId() + ", Name: " + regionType.getName() + ", Level: " + (level - 1) + " -> " + level);
        rankedRegion.onLevelUP();
        rankDatabase.levelUp();
    }

    public void contributeObjective(Player p, String objective) {
        RankObjective progressRankObjective = this.rankObjective;
        RankObjective goalRankObjective;

        if (!(NationsPlugin.levelObjectives.size() + 1 == this.level)) {
            goalRankObjective = NationsPlugin.levelObjectives.get(this.level);
        } else {
            goalRankObjective = new RankObjective(0, 0, 0, 0, 0, "Coming Soon...", "Coming Soon...", "Coming Soon...");
        }
        int charged;
        switch (objective) {
            case "a":
                charged = ItemTransfer.charge(p, goalRankObjective.getMaterial_a(), goalRankObjective.getObjective_a() - progressRankObjective.getObjective_a(), false);
                if (charged <= 0) return;
                NationsPlugin.nationsLogger.logInfo("(UpgradeContribute) Type: " + regionType.getType() + ", ID: " + regionType.getId() + ", Name: " + regionType.getName() + ", Username: " + p.getName() + ", Material: " + goalRankObjective.getMaterial_a() + ", Amount: " + charged);
                rankedRegion.onContribute(goalRankObjective.getMaterial_a(), charged, p.getName());
                progressRankObjective.setObjective_a(progressRankObjective.getObjective_a() + charged);
                this.rankObjective = progressRankObjective;
                rankDatabase.setObjective("obj_a", progressRankObjective.getObjective_a());
                break;
            case "b":
                charged = ItemTransfer.charge(p, goalRankObjective.getMaterial_b(), goalRankObjective.getObjective_b() - progressRankObjective.getObjective_b(), false);
                if (charged <= 0) return;
                NationsPlugin.nationsLogger.logInfo("(UpgradeContribute) Type: " + regionType.getType() + ", ID: " + regionType.getId() + ", Name: " + regionType.getName() + ", Username: " + p.getName() + ", Material: " + goalRankObjective.getMaterial_b() + ", Amount: " + charged);
                rankedRegion.onContribute(goalRankObjective.getMaterial_b(), charged, p.getName());
                progressRankObjective.setObjective_b(progressRankObjective.getObjective_b() + charged);
                this.rankObjective = progressRankObjective;
                rankDatabase.setObjective("obj_b", progressRankObjective.getObjective_b());
                break;
            case "c":
                charged = ItemTransfer.charge(p, goalRankObjective.getMaterial_c(), goalRankObjective.getObjective_c() - progressRankObjective.getObjective_c(), false);
                if (charged <= 0) return;
                NationsPlugin.nationsLogger.logInfo("(UpgradeContribute) Type: " + regionType.getType() + ", ID: " + regionType.getId() + ", Name: " + regionType.getName() + ", Username: " + p.getName() + ", Material: " + goalRankObjective.getMaterial_c() + ", Amount: " + charged);
                rankedRegion.onContribute(goalRankObjective.getMaterial_c(), charged, p.getName());
                progressRankObjective.setObjective_c(progressRankObjective.getObjective_c() + charged);
                this.rankObjective = progressRankObjective;
                rankDatabase.setObjective("obj_c", progressRankObjective.getObjective_c());
                break;
        }
    }


    public int getLevel() {
        return level;
    }

    public RankObjective getRankObjective() {
        return rankObjective;
    }

    @Override
    public void onRegionRenamed(String newRegionName) {

    }

    @Override
    public void onRegionRemoved() {
        rankDatabase.removeRank();
    }
}
