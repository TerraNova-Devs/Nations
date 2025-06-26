package de.terranova.nations.regions.modules.access;

import java.util.UUID;

public interface PropertyAccessControlled {

    PropertyAccess getAccess();
    UUID getId();
}
