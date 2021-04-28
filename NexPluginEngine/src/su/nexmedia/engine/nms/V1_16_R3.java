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

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Multimap;

import io.netty.channel.Channel;
import net.minecraft.server.v1_16_R3.AttributeBase;
import net.minecraft.server.v1_16_R3.AttributeModifier;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.EnumItemSlot;
import net.minecraft.server.v1_16_R3.GenericAttributes;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.Item;
import net.minecraft.server.v1_16_R3.ItemArmor;
import net.minecraft.server.v1_16_R3.ItemAxe;
import net.minecraft.server.v1_16_R3.ItemSword;
import net.minecraft.server.v1_16_R3.ItemTool;
import net.minecraft.server.v1_16_R3.ItemTrident;
import net.minecraft.server.v1_16_R3.NBTCompressedStreamTools;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.NBTTagList;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketPlayOutAnimation;
import net.minecraft.server.v1_16_R3.World;
import su.nexmedia.engine.utils.random.Rnd;

public class V1_16_R3 implements NMS {

    @Override
    @NotNull
    public Channel getChannel(@NotNull Player p) {
        return ((CraftPlayer) p).getHandle().playerConnection.networkManager.channel;
    }

    @Override
    public void sendPacket(@NotNull Player p, @NotNull Object packet) {
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket((Packet<?>) packet);
    }

    @Override
    public void sendAttackPacket(@NotNull Player p, int id) {
        CraftPlayer player = (CraftPlayer) p;
        net.minecraft.server.v1_16_R3.Entity entity = (net.minecraft.server.v1_16_R3.Entity) player.getHandle();
        PacketPlayOutAnimation packet = new PacketPlayOutAnimation(entity, id);
        player.getHandle().playerConnection.sendPacket(packet);
    }

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
        net.minecraft.server.v1_16_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
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

        net.minecraft.server.v1_16_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

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

        net.minecraft.server.v1_16_R3.ItemStack nmsItem = net.minecraft.server.v1_16_R3.ItemStack.a(nbtTagCompoundRoot); // .createStack(nbtTagCompoundRoot);
        ItemStack item = (ItemStack) CraftItemStack.asBukkitCopy(nmsItem);

        return item;
    }

    @Override
    @NotNull
    public String getNbtString(@NotNull ItemStack item) {
        return CraftItemStack.asNMSCopy(item).getOrCreateTag().asString();
    }

    @Override
    @NotNull
    public ItemStack damageItem(@NotNull ItemStack item, int amount, @Nullable Player player) {
        // CraftItemStack craftItem = (CraftItemStack) item;
        net.minecraft.server.v1_16_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);

        EntityPlayer nmsPlayer = player != null ? ((CraftPlayer) player).getHandle() : null;
        nmsStack.isDamaged(amount, Rnd.rnd, nmsPlayer);

        return CraftItemStack.asBukkitCopy(nmsStack);
    }

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
            attMap = tool.a(EnumItemSlot.MAINHAND);
        }
        else if (item instanceof ItemSword) {
            ItemSword tool = (ItemSword) item;
            attMap = tool.a(EnumItemSlot.MAINHAND);
        }
        else if (item instanceof ItemTrident) {
            ItemTrident tool = (ItemTrident) item;
            attMap = tool.a(EnumItemSlot.MAINHAND);
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
        return this.getAttributeValue(itemStack, GenericAttributes.ATTACK_DAMAGE);
    }

    @Override
    public double getDefaultSpeed(@NotNull ItemStack itemStack) {
        return this.getAttributeValue(itemStack, GenericAttributes.ATTACK_SPEED);
    }

    @Override
    public double getDefaultArmor(@NotNull ItemStack itemStack) {
        return this.getAttributeValue(itemStack, GenericAttributes.ARMOR);
    }

    @Override
    public double getDefaultToughness(@NotNull ItemStack itemStack) {
        return this.getAttributeValue(itemStack, GenericAttributes.ARMOR_TOUGHNESS);
    }
}
