package de.terranova.nations.regions.rank;

import org.jetbrains.annotations.ApiStatus;

public interface RankedRegion {

    @ApiStatus.OverrideOnly
    default void onLevelUP(){}

    @ApiStatus.OverrideOnly
    default void onContribute(String material, int amount, String username){}

    Rank getRank();

}
