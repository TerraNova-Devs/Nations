package de.terranova.nations.utils.terraRenderer.refactor;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Utility for spawning and removing NMS entities via packets only
 * (entities are NOT added to the world).
 */
public final class DisplayPackets {

    private DisplayPackets() {}

    /**
     * Sends spawn + metadata packets for a given NMS entity to the given players.
     * The entity is NOT registered in the world – this is purely packet-based.
     */
    public static void spawn(Entity nmsEntity, Collection<Player> players) {
        if (nmsEntity == null || players == null || players.isEmpty()) return;

        ServerLevel nmsWorld = (ServerLevel) nmsEntity.level();

        ServerEntity serverEntity = new ServerEntity(
                nmsWorld,
                nmsEntity,
                0,
                false,
                packet -> {},
                Collections.emptySet()
        );

        Packet<?> spawnPacket = nmsEntity.getAddEntityPacket(serverEntity);

        var dataItems = nmsEntity.getEntityData().packAll();
        ClientboundSetEntityDataPacket dataPacket =
                new ClientboundSetEntityDataPacket(nmsEntity.getId(), dataItems);

        for (Player p : players) {
            if (p == null || !p.isOnline()) continue;

            var handle = ((CraftPlayer) p).getHandle();
            handle.connection.send(spawnPacket);
            handle.connection.send(dataPacket);
        }
    }

    /**
     * Sends a remove-entity packet for the given id to the given players.
     */
    public static void remove(int entityId, Collection<Player> players) {
        if (players == null || players.isEmpty()) return;

        ClientboundRemoveEntitiesPacket removePacket =
                new ClientboundRemoveEntitiesPacket(entityId);

        for (Player p : players) {
            if (p == null || !p.isOnline()) continue;

            var handle = ((CraftPlayer) p).getHandle();
            handle.connection.send(removePacket);
        }
    }

    /**
     * Sends an update (teleport + metadata) for an existing packet-only entity.
     * Used for smooth interpolation updates of BlockDisplayNode / Display entities.
     */
    public static void update(Entity nmsEntity, Collection<Player> players) {
        if (nmsEntity == null || players == null || players.isEmpty()) return;

        // --- Teleport part (position / rotation) ---
        Vec3 position = new Vec3(
                nmsEntity.getX(),
                nmsEntity.getY(),
                nmsEntity.getZ()
        );

        // Use the entity's current delta movement (or Vec3.ZERO if you prefer)
        Vec3 deltaMovement = nmsEntity.getDeltaMovement();

        PositionMoveRotation change = new PositionMoveRotation(
                position,
                deltaMovement,
                nmsEntity.getYRot(), // yaw
                nmsEntity.getXRot()  // pitch
        );

        Set<Relative> relatives = EnumSet.noneOf(Relative.class); // absolute position/rotation

        ClientboundTeleportEntityPacket teleportPacket =
                new ClientboundTeleportEntityPacket(
                        nmsEntity.getId(),
                        change,
                        relatives,
                        nmsEntity.onGround()
                );

        // --- Metadata part (includes Display transformation + interpolation fields) ---
        var dataItems = nmsEntity.getEntityData().packAll();
        ClientboundSetEntityDataPacket dataPacket =
                new ClientboundSetEntityDataPacket(nmsEntity.getId(), dataItems);

        for (Player p : players) {
            if (p == null || !p.isOnline()) continue;

            var handle = ((CraftPlayer) p).getHandle();
            handle.connection.send(teleportPacket);
            handle.connection.send(dataPacket);
        }
    }
}
