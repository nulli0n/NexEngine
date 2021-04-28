package su.nexmedia.engine.nms.packets.events;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EngineServerPacketEvent extends EnginePacketEvent {

    public EngineServerPacketEvent(@NotNull Player reciever, @NotNull Object packet) {
        super(reciever, packet);
    }
}
