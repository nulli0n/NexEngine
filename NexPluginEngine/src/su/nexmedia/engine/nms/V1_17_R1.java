package su.nexmedia.engine.nms;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Multimap;

import io.netty.channel.Channel;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTCompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutAnimation;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.PlayerInteractManager;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.ai.attributes.AttributeBase;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemArmor;
import net.minecraft.world.item.ItemAxe;
import net.minecraft.world.item.ItemSword;
import net.minecraft.world.item.ItemTool;
import net.minecraft.world.item.ItemTrident;
import net.minecraft.world.level.World;
import su.nexmedia.engine.utils.Reflex;
import su.nexmedia.engine.utils.random.Rnd;


public class V1_17_R1 implements NMS {

    @Override
    @NotNull
    public Channel getChannel(@NotNull Player p) {
        return ((CraftPlayer) p).getHandle().b.a().k;
    }

    @Override
    public void sendPacket(@NotNull Player p, @NotNull Object packet) {
        ((CraftPlayer) p).getHandle().b.sendPacket((Packet<?>) packet);
    }

    @Override
    public void sendAttackPacket(@NotNull Player p, int id) {
        CraftPlayer player = (CraftPlayer) p;
        Entity entity = player.getHandle();
        PacketPlayOutAnimation packet = new PacketPlayOutAnimation(entity, id);
        player.getHandle().b.sendPacket(packet);
    }

    @Override
    public boolean breakBlock(@NotNull Player player, @NotNull Block block) {
        BlockPosition position = new BlockPosition(block.getX(), block.getY(), block.getZ());
        EntityPlayer entityPlayer = ((CraftPlayer)player).getHandle();
        PlayerInteractManager manager = entityPlayer.d;
        return manager.breakBlock(position);
    }

    @Deprecated
    @Override
    public void openChestAnimation(@NotNull Block chest, boolean open) {
        if (chest.getState() instanceof Chest) {
            Location lo = chest.getLocation();
            org.bukkit.World bWorld = lo.getWorld();
            if (bWorld == null)
                return;

            World world = ((CraftWorld) bWorld).getHandle();
            BlockPosition position = new BlockPosition(lo.getX(), lo.getY(), lo.getZ());
            // TileEntityChest tileChest = (TileEntityChest) world.getTileEntity(position);
            world.playBlockAction(position, world.getType(position).getBlock(), 1, open ? 1 : 0);
        }
    }

    @Override
    @NotNull
    public String toJSON(@NotNull ItemStack item) {
        NBTTagCompound c = new NBTTagCompound();
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        c = nmsItem.save(c);
        String js = c.toString();
        if (js.length() > 32767) {
            ItemStack item2 = new ItemStack(item.getType());
            return toJSON(item2);
        }

        return js;
    }

    @Override
    @Nullable
    public String toBase64(@NotNull ItemStack item) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutput = new DataOutputStream(outputStream);

        NBTTagList nbtTagListItems = new NBTTagList();
        NBTTagCompound nbtTagCompoundItem = new NBTTagCompound();

        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

        nmsItem.save(nbtTagCompoundItem);

        nbtTagListItems.add(nbtTagCompoundItem);

        try {
            NBTCompressedStreamTools.a(nbtTagCompoundItem, (DataOutput) dataOutput);
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return new BigInteger(1, outputStream.toByteArray()).toString(32);
    }

    @Override
    @Nullable
    public ItemStack fromBase64(@NotNull String data) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(data, 32).toByteArray());

        NBTTagCompound nbtTagCompoundRoot;
        try {
            nbtTagCompoundRoot = NBTCompressedStreamTools.a((DataInput) new DataInputStream(inputStream));
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        net.minecraft.world.item.ItemStack nmsItem = net.minecraft.world.item.ItemStack.a(nbtTagCompoundRoot); // .createStack(nbtTagCompoundRoot);
        ItemStack item = CraftItemStack.asBukkitCopy(nmsItem);

        return item;
    }

    @Deprecated
    @Override
    @NotNull
    public String getNbtString(@NotNull ItemStack item) {
        return CraftItemStack.asNMSCopy(item).getOrCreateTag().asString();
    }

    @Override
    @NotNull
    public ItemStack damageItem(@NotNull ItemStack item, int amount, @Nullable Player player) {
        // CraftItemStack craftItem = (CraftItemStack) item;
        net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);

        EntityPlayer nmsPlayer = player != null ? ((CraftPlayer) player).getHandle() : null;
        nmsStack.isDamaged(amount, Rnd.rnd, nmsPlayer);

        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    @Deprecated
    @Override
    @NotNull
    public String fixColors(@NotNull String str) {
        str = str.replace("\n", "%n%"); // CraftChatMessage wipes all lines out.

        IChatBaseComponent baseComponent = CraftChatMessage.fromStringOrNull(str);
        String singleColor = CraftChatMessage.fromComponent(baseComponent);
        return singleColor.replace("%n%", "\n");
    }

    @Nullable
    private Multimap<AttributeBase, AttributeModifier> getAttributes(@NotNull ItemStack itemStack) {
        Item item = CraftItemStack.asNMSCopy(itemStack).getItem();
        Multimap<AttributeBase, AttributeModifier> attMap = null;

        if (item instanceof ItemArmor) {
            ItemArmor tool = (ItemArmor) item;
            attMap = tool.a(tool.b());
        }
        else if (item instanceof ItemTool) {
            ItemTool tool = (ItemTool) item;
            attMap = tool.a(EnumItemSlot.a);
        }
        else if (item instanceof ItemSword) {
            ItemSword tool = (ItemSword) item;
            attMap = tool.a(EnumItemSlot.a);
        }
        else if (item instanceof ItemTrident) {
            ItemTrident tool = (ItemTrident) item;
            attMap = tool.a(EnumItemSlot.a);
        }

        return attMap;
    }

    private double getAttributeValue(@NotNull ItemStack item, @NotNull AttributeBase base) {
        Multimap<AttributeBase, AttributeModifier> attMap = this.getAttributes(item);
        if (attMap == null)
            return 0D;

        Collection<AttributeModifier> att = attMap.get(base);
        double damage = (att == null || att.isEmpty()) ? 0 : att.stream().findFirst().get().getAmount();

        return damage;// + 1;
    }

    @Override
    public boolean isWeapon(@NotNull ItemStack itemStack) {
        Item item = CraftItemStack.asNMSCopy(itemStack).getItem();
        return item instanceof ItemSword || item instanceof ItemAxe || item instanceof ItemTrident;
    }

    @Override
    public boolean isTool(@NotNull ItemStack itemStack) {
        Item item = CraftItemStack.asNMSCopy(itemStack).getItem();
        return item instanceof ItemTool;
    }

    @Override
    public boolean isArmor(@NotNull ItemStack itemStack) {
        Item item = CraftItemStack.asNMSCopy(itemStack).getItem();
        return item instanceof ItemArmor;
    }

    @Override
    public double getDefaultDamage(@NotNull ItemStack itemStack) {
        return this.getAttributeValue(itemStack, GenericAttributes.f);
    }

    @Override
    public double getDefaultSpeed(@NotNull ItemStack itemStack) {
        return this.getAttributeValue(itemStack, GenericAttributes.h);
    }

    @Override
    public double getDefaultArmor(@NotNull ItemStack itemStack) {
        return this.getAttributeValue(itemStack, GenericAttributes.i);
    }

    @Override
    public double getDefaultToughness(@NotNull ItemStack itemStack) {
        return this.getAttributeValue(itemStack, GenericAttributes.j);
    }

    @Override
    @Nullable
    public org.bukkit.entity.@Nullable Entity getPacketEntity(@NotNull Object packet, @NotNull String fieldId) {
        Integer entityId = (Integer) Reflex.getFieldValue(packet, fieldId);
        if (entityId == null) return null;

        CraftServer server = (CraftServer) Bukkit.getServer();
        Entity nmsEntity = null;
        for (WorldServer worldServer : server.getServer().getWorlds()) {
            nmsEntity = worldServer.getEntity(entityId.intValue());
            if (nmsEntity != null) {
                break;
            }
        }

        if (nmsEntity == null) return null;

        return Bukkit.getServer().getEntity(nmsEntity.getUniqueID());
    }
}
