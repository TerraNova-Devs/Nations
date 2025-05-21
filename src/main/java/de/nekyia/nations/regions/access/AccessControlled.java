package de.nekyia.nations.regions.access;

import java.util.UUID;

public interface AccessControlled {

    Access getAccess();
    UUID getId();

}
