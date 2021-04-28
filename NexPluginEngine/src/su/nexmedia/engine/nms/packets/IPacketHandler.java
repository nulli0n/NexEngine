package su.nexmedia.engine.nms.packets;

import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.nms.packets.events.EnginePlayerPacketEvent;
import su.nexmedia.engine.nms.packets.events.EngineServerPacketEvent;

public interface IPacketHandler {

    public void managePlayerPacket(@NotNull EnginePlayerPacketEvent event);

    public void manageServerPacket(@NotNull EngineServerPacketEvent event);
}
