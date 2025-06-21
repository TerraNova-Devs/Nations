package de.terranova.nations.regions.base;

import java.util.List;

public interface RegionFactory {

    String getType();

    //For new Creations facilitates more information
    Region createWithContext(RegionContext ctx);

    //Too load least neccesairy from saved data
    Region createFromArgs(List<String> args);
}
