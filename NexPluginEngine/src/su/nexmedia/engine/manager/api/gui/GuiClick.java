package su.nexmedia.engine.manager.api.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface GuiClick {

    void click(@NotNull Player p, @Nullable Enum<?> type, @NotNull InventoryClickEvent e);
}
