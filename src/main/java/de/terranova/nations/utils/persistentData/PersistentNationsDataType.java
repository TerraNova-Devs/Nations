package de.terranova.nations.utils.persistentData;

import de.terranova.nations.utils.persistentData.DataTypes.InstantDataType;
import de.terranova.nations.utils.persistentData.DataTypes.UUIDDataType;
import org.bukkit.persistence.PersistentDataType;

import java.time.Instant;
import java.util.UUID;

public interface PersistentNationsDataType {

    PersistentDataType<byte[], Instant> Instant = new InstantDataType();
    PersistentDataType<byte[], UUID> UUID = new UUIDDataType();

}
