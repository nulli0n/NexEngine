package su.nexmedia.engine.nms.packets.events;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EnginePlayerPacketEvent extends EnginePacketEvent {

    public EnginePlayerPacketEvent(@NotNull Player reciever, @NotNull Object packet) {
        super(reciever, packet);
    }
}
