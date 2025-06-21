package de.terranova.nations.regions.rank;

import de.terranova.nations.NationsPlugin;
import de.terranova.nations.regions.base.GridRegion;
import de.terranova.nations.regions.base.RegionListener;

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
        this.level = rankDatabase.fetchRank();
        this.rankObjective = new RankObjective(-1, -1);
        this.regionType = regionType;
        regionType.addListener(this);
    }

    public void levelUP() {

        RankObjective progressRankObjective = this.rankObjective;
        RankObjective goalRankObjective;

        if (!(NationsPlugin.levelObjectives.size() + 1 == this.level)) {
            goalRankObjective = NationsPlugin.levelObjectives.get(this.level);
        } else {
            goalRankObjective = new RankObjective(0, 0);
        }

        this.level++;
        this.rankObjective = new RankObjective(this.rankObjective.getScore(), 0);
        NationsPlugin.nationsLogger.logInfo("(LevelUp) Type: " + regionType.getType() + ", ID: " + regionType.getId() + ", Name: " + regionType.getName() + ", Level: " + (level - 1) + " -> " + level);
        rankedRegion.onLevelUP();
        rankDatabase.levelUp();
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
