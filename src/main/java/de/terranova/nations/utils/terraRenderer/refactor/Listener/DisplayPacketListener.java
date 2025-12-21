package de.terranova.nations.utils.terraRenderer.refactor.Listener;

import de.terranova.nations.utils.terraRenderer.refactor.BlockDisplayNode;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Installs a Netty handler per player to intercept ServerboundInteractPacket.
 * If the packet's entityId matches one of our hitbox entities, we forward the
 * click to BlockDisplayNode.handlePacketClick(...).
 */
public class DisplayPacketListener implements Listener {

    private static final String HANDLER_NAME_PREFIX = "terraRenderer_interact_";

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        inject(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        uninject(event.getPlayer());
    }

    private void inject(Player player) {
        var handle = ((CraftPlayer) player).getHandle();
        var connection = handle.connection;      // ServerGamePacketListenerImpl
        var networkManager = connection.connection; // net.minecraft.network.Connection
        Channel channel = networkManager.channel;

        String handlerName = HANDLER_NAME_PREFIX + player.getUniqueId();

        if (channel.pipeline().get(handlerName) != null) {
            // already injected
            return;
        }

        channel.pipeline().addBefore("packet_handler", handlerName, new ChannelDuplexHandler() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                if (msg instanceof ServerboundInteractPacket packet) {
                    int entityId = packet.getEntityId();
                    BlockDisplayNode.handlePacketClick(entityId, player);
                }
                super.channelRead(ctx, msg);
            }
        });
    }

    private void uninject(Player player) {
        try {
            var handle = ((CraftPlayer) player).getHandle();
            var connection = handle.connection;
            var networkManager = connection.connection;
            Channel channel = networkManager.channel;

            String handlerName = HANDLER_NAME_PREFIX + player.getUniqueId();

            if (channel.pipeline().get(handlerName) != null) {
                channel.pipeline().remove(handlerName);
            }
        } catch (Exception ignored) {
            // player might already be disconnected, channel closed, etc.
        }
    }
}

