package de.terranova.nations.regions.access;

import java.util.UUID;

public interface PropertyAccessControlled {

    PropertyAccess getAccess();
    UUID getId();
}
