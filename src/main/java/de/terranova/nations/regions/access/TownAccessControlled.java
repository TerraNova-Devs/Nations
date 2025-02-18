package de.terranova.nations.regions.access;

import java.util.UUID;

public interface TownAccessControlled {

    TownAccess getAccess();
    UUID getId();

}
