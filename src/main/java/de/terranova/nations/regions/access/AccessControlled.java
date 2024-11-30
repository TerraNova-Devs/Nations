package de.terranova.nations.regions.access;

import de.terranova.nations.regions.bank.Bank;

import java.util.HashMap;
import java.util.UUID;

public interface AccessControlled {

    Access getAccess();
    void dataBaseCallAccess(UUID PUUID, AccessLevel access);
    HashMap<UUID, AccessLevel> dataBaseRetrieveAccess();



}
