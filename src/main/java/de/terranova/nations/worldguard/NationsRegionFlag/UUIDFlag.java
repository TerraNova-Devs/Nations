package de.terranova.nations.worldguard.NationsRegionFlag;

import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class UUIDFlag extends Flag<UUID> {
    private final UUID defaultValue;

    protected UUIDFlag(String name) {
        super(name);
        this.defaultValue = null;
    }

    public UUIDFlag(String name, UUID defaultValue) {
        super(name);
        this.defaultValue = defaultValue;
    }

    public UUIDFlag(String name, RegionGroup defaultGroup) {
        super(name, defaultGroup);
        this.defaultValue = null;
    }

    public UUIDFlag(String name, RegionGroup defaultGroup, UUID defaultValue) {
        super(name, defaultGroup);
        this.defaultValue = defaultValue;
    }

    @Override
    public UUID parseInput(FlagContext context) throws InvalidFlagFormat {
        return UUID.fromString(context.getUserInput());
    }

    @Nullable
    public UUID getDefault() {
        return this.defaultValue;
    }

    @Override
    public UUID unmarshal(@Nullable Object o) {
        return o instanceof UUID ? UUID.fromString((String) o) : null;
    }

    @Override
    public Object marshal(UUID o) {
        return o;
    }
}
