package de.terranova.nations.regions.rank;

import org.jetbrains.annotations.ApiStatus;

public interface RankedRegion {

    @ApiStatus.OverrideOnly
    default void onLevelUP(){}

    Rank getRank();

}
