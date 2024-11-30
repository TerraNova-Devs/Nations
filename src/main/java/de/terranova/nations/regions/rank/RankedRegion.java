package de.terranova.nations.regions.rank;

import de.terranova.nations.regions.npc.NPCr;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

import java.sql.Timestamp;

public interface RankedRegion {

    @ApiStatus.OverrideOnly
    default void onLevelUP(){}

    Rank getRank();

    void dataBaseCallRank(RankObjective progressRankObjective);

}
