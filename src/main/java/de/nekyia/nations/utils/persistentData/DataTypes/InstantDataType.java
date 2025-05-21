package de.nekyia.nations.utils.persistentData.DataTypes;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.time.Instant;

public class InstantDataType implements PersistentDataType<byte[], Instant> {
    @Override
    public @NotNull Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @Override
    public @NotNull Class<Instant> getComplexType() {
        return Instant.class;
    }

    @Override
    public byte @NotNull [] toPrimitive(@NotNull Instant instant, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        byte[] data;
        try {
            oos = new ObjectOutputStream(bos);
            oos.writeObject(instant);
            data = bos.toByteArray();
            oos.flush();
            oos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return data;
    }

    @Override
    public @NotNull Instant fromPrimitive(byte @NotNull [] bytes, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            ois.close();
            return (Instant) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
