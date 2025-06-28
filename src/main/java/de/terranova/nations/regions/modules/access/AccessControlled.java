package de.terranova.nations.regions.modules.access;

import java.util.UUID;

public interface AccessControlled {

    Access getAccess();
    UUID getId();

}
