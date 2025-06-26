package de.terranova.nations.regions.modules.access;

import java.util.UUID;

public interface TownAccessControlled {

    TownAccess getAccess();
    UUID getId();

}
