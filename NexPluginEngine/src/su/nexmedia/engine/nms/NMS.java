package su.nexmedia.engine.nms;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.netty.channel.Channel;

public interface NMS {

    @Nullable
    public Entity getPacketEntity(@NotNull Object packet, @NotNull String fieldId);
    
    @NotNull
    public String toJSON(@NotNull ItemStack item);

    @Nullable
    public String toBase64(@NotNull ItemStack item);

    @Nullable
    public ItemStack fromBase64(@NotNull String data);

    @NotNull
    @Deprecated
    public String getNbtString(@NotNull ItemStack item);

    @Deprecated
    public void openChestAnimation(@NotNull Block chest, boolean open);

    /**
     * 
     * @param p
     * @param i 0 = main hand, 3 = off hand.
     */
    public void sendAttackPacket(@NotNull Player p, int i);

    public boolean breakBlock(@NotNull Player player, @NotNull Block block);
    
    @NotNull
    public Channel getChannel(@NotNull Player p);

    public void sendPacket(@NotNull Player p, @NotNull Object packet);

    @NotNull
    ItemStack damageItem(@NotNull ItemStack item, int amount, @Nullable Player player);

    @NotNull
    @Deprecated
    String fixColors(@NotNull String str);

    double getDefaultDamage(@NotNull ItemStack itemStack);

    double getDefaultSpeed(@NotNull ItemStack itemStack);

    double getDefaultArmor(@NotNull ItemStack itemStack);

    double getDefaultToughness(@NotNull ItemStack itemStack);

    public boolean isWeapon(@NotNull ItemStack itemStack);

    public boolean isTool(@NotNull ItemStack itemStack);

    public boolean isArmor(@NotNull ItemStack itemStack);
}
