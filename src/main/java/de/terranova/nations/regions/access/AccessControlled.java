package de.terranova.nations.regions.access;

import java.util.UUID;

public interface AccessControlled {

    Access getAccess();
    UUID getId();

}
