package de.terranova.nations.settlements.RegionTypes;

import de.terranova.nations.settlements.AccessLevel;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public interface Access {

    default AccessLevel getAccess(UUID uuid) {
        if(getAccessLevels().containsKey(uuid)) {
            return getAccessLevels().get(uuid);
        }
        return null;
    }

    HashMap<UUID, AccessLevel> getAccessLevels();

    void setAccessLevels(HashMap<UUID, AccessLevel> accessLevels);

    default void removeAccess(UUID uuid){
        HashMap<UUID, AccessLevel> accessLevels = getAccessLevels();
        accessLevels.remove(uuid);
        dataBaseCallAccess(uuid, null);
        setAccessLevels(accessLevels);
    };

    default void setAccessLevel(UUID uuid, AccessLevel accessLevel){
        HashMap<UUID, AccessLevel> accessLevels = getAccessLevels();
        if(getAccessLevels().containsKey(uuid)) {
            accessLevels.replace(uuid, accessLevel);
        } else {
            accessLevels.put(uuid, accessLevel);
        }
        dataBaseCallAccess(uuid, accessLevel);
        setAccessLevels(accessLevels);
    }

    default boolean hasAccess(AccessLevel access, AccessLevel neededAcess) {
        return access.getWeight() >= neededAcess.getWeight();
    }

    void dataBaseCallAccess(UUID PUUID, AccessLevel access);

    HashMap<UUID, AccessLevel> dataBaseRetrieveAccess();

}
