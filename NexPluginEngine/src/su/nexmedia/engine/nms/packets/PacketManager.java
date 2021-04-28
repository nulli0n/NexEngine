package su.nexmedia.engine.nms.packets;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.manager.IManager;
import su.nexmedia.engine.nms.packets.events.EnginePlayerPacketEvent;
import su.nexmedia.engine.nms.packets.events.EngineServerPacketEvent;

public class PacketManager extends IManager<NexEngine> {

    protected static final Set<IPacketHandler> PACKET_HANDLERS = new HashSet<>();
    private static final String INJECTOR_ID = "nex_handler";

    public PacketManager(@NotNull NexEngine plugin) {
        super(plugin);
    }

    @Override
    public final void setup() {
        this.registerListeners();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player == null)
                continue;
            this.injectPlayer(player);
        }
    }

    @Override
    public final void shutdown() {
        this.unregisterListeners();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player == null)
                continue;
            this.removePlayer(player);
        }
        PACKET_HANDLERS.clear();
    }

    public void registerHandler(@NotNull IPacketHandler ipr) {
        PACKET_HANDLERS.add(ipr);
    }

    public void unregisterHandler(@NotNull IPacketHandler ipr) {
        PACKET_HANDLERS.remove(ipr);
    }

    @NotNull
    public Set<IPacketHandler> getHandlers() {
        return PACKET_HANDLERS;
    }

    public Channel getChannel(@NotNull Player player) {
        return this.plugin.getNMS().getChannel(player);
    }

    public void sendPacket(@NotNull Player player, @NotNull Object packet) {
        this.plugin.getNMS().sendPacket(player, packet);
    }

    private final void removePlayer(@NotNull Player player) {
        Channel channel = this.getChannel(player);
        if (channel.pipeline().get(INJECTOR_ID) != null) {
            channel.pipeline().remove(INJECTOR_ID);
        }
        /*
         * channel.eventLoop().submit(() -> { channel.pipeline().remove(INJECTOR_ID);
         * return null; });
         */
    }

    private final void injectPlayer(@NotNull Player player) {
        ChannelPipeline pipe = this.getChannel(player).pipeline();
        if (pipe.get(INJECTOR_ID) != null)
            return;

        ChannelDuplexHandler cdx = new ChannelDuplexHandler() {

            // From Player to Server (In)
            @Override
            public void channelRead(ChannelHandlerContext cont, Object packet) throws Exception {
                EngineServerPacketEvent e = new EngineServerPacketEvent(player, packet);
                plugin.getPluginManager().callEvent(e);
                if (e.isCancelled())
                    return;

                // System.out.print("PACKET IN: " + packet.toString());
                super.channelRead(cont, e.getPacket());
            }

            // From Server to Player (Out)
            @Override
            public void write(ChannelHandlerContext cont, Object packet, ChannelPromise prom) throws Exception {
                EnginePlayerPacketEvent e = new EnginePlayerPacketEvent(player, packet);
                plugin.getPluginManager().callEvent(e);
                if (e.isCancelled())
                    return;

                super.write(cont, e.getPacket(), prom);
            }
        };

        try {
            pipe.addBefore("packet_handler", INJECTOR_ID, cdx);
        }
        catch (Exception ex) {
            this.plugin.error("Could not add packet listener for " + player.getName() + " !");
            ex.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(PlayerJoinEvent e) {
        this.injectPlayer(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onQuit(PlayerQuitEvent e) {
        this.removePlayer(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPacketOut(EnginePlayerPacketEvent e) {
        for (IPacketHandler handler : this.getHandlers()) {
            handler.managePlayerPacket(e);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPacketIn(EngineServerPacketEvent e) {
        for (IPacketHandler handler : this.getHandlers()) {
            handler.manageServerPacket(e);
        }
    }
}
