package de.terranova.nations.regions.rank;

import de.mcterranova.terranovaLib.InventoryUtil.ItemTransfer;
import de.terranova.nations.NationsPlugin;
import de.terranova.nations.database.SettleDBstuff;
import org.bukkit.entity.Player;

public class Rank {

    private int level;
    private RankObjective rankObjective;
    private RankedRegion rankedRegion;

    public Rank(RankedRegion rankedRegion, int level, RankObjective rankObjective) {
        this.rankedRegion = rankedRegion;
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
        rankedRegion.onLevelUP();
    }

    public void contributeObjective(Player p, String objective) {
        RankObjective progressRankObjective = this.rankObjective;
        RankObjective goalRankObjective;

        if (!(NationsPlugin.levelObjectives.size() + 1 == this.level)) {
            goalRankObjective = NationsPlugin.levelObjectives.get(this.level);
        } else {
            goalRankObjective = new RankObjective(0, 0, 0, 0, 0, "Coming Soon...", "Coming Soon...", "Coming Soon...");
        }

        switch (objective) {
            case "a":
                int chargeda = ItemTransfer.charge(p, goalRankObjective.getMaterial_a(), goalRankObjective.getObjective_a() - progressRankObjective.getObjective_a(), false);
                if (chargeda <= 0) return;
                progressRankObjective.setObjective_a(progressRankObjective.getObjective_a() + chargeda);
                this.rankObjective = progressRankObjective;
                rankedRegion.dataBaseCallRank(progressRankObjective);
                break;
            case "b":
                int chargedb = ItemTransfer.charge(p, goalRankObjective.getMaterial_b(), goalRankObjective.getObjective_b() - progressRankObjective.getObjective_b(), false);
                if (chargedb <= 0) return;
                progressRankObjective.setObjective_b(progressRankObjective.getObjective_b() + chargedb);
                this.rankObjective = progressRankObjective;
                rankedRegion.dataBaseCallRank(progressRankObjective);
                break;
            case "c":
                int chargedc = ItemTransfer.charge(p, goalRankObjective.getMaterial_c(), goalRankObjective.getObjective_c() - progressRankObjective.getObjective_c(), false);
                if (chargedc <= 0) return;
                progressRankObjective.setObjective_c(progressRankObjective.getObjective_c() + chargedc);
                this.rankObjective = progressRankObjective;
                rankedRegion.dataBaseCallRank(progressRankObjective);
                break;
        }
    }

    public int getLevel() {
        return level;
    }

    public RankObjective getRankObjective() {
        return rankObjective;
    }
}