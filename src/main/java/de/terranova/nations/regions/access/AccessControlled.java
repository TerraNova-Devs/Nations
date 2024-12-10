package de.terranova.nations.regions.access;

import de.terranova.nations.regions.bank.Bank;

import java.util.HashMap;
import java.util.UUID;

public interface AccessControlled {

    Access getAccess();
    UUID getId();

}
