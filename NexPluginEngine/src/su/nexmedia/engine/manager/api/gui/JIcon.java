package su.nexmedia.engine.manager.api.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.utils.StringUT;

public class JIcon {

    private Material m;
    private int amount;
    private String name;
    private List<String> lore;
    private boolean enchanted;

    private ItemStack item;
    private GuiClick click;

    public JIcon(@NotNull JIcon clon) {
        this(clon.m, clon.amount);

        if (clon.item != null) {
            this.item = new ItemStack(clon.item);
        }
        else {
            this.item = null;
        }

        if (clon.lore != null) {
            this.lore = new ArrayList<>(clon.lore);
        }
        else {
            this.lore = null;
        }

        this.enchanted = clon.enchanted;
        this.click = clon.click;
    }

    public JIcon(@NotNull Material m, int amount) {
        this.m = m;
        this.amount = amount;
    }

    public JIcon(@NotNull Material m) {
        this(m, 1);
    }

    public JIcon(@NotNull ItemStack item) {
        this.m = item.getType();
        this.amount = item.getAmount();
        this.item = new ItemStack(item);
    }

    @NotNull
    public JIcon setName(@NotNull String name) {
        this.name = StringUT.color(name);
        return this;
    }

    @NotNull
    public JIcon setLore(@NotNull List<String> s) {
        this.lore = StringUT.color(s);
        return this;
    }

    @NotNull
    public JIcon addLore(@NotNull String... s) {
        if (this.lore == null)
            this.lore = new ArrayList<>();

        for (String s1 : s) {
            this.lore.add(StringUT.color(s1));
        }
        return this;
    }

    @NotNull
    public JIcon setEnchanted(boolean b) {
        this.enchanted = b;
        return this;
    }

    @NotNull
    public JIcon clearLore() {
        if (this.lore != null) {
            this.lore.clear();
        }
        else {
            this.lore = new ArrayList<>();
        }
        return this;
    }

    @Nullable
    public GuiClick getClick() {
        return this.click;
    }

    @NotNull
    public JIcon setClick(@Nullable GuiClick click) {
        this.click = click;
        return this;
    }

    public void click(@NotNull Player p, @NotNull ClickType type, @NotNull InventoryClickEvent e) {
        if (this.click == null)
            return;

        this.click.click(p, type, e);
    }

    @NotNull
    public ItemStack build() {
        if (this.item == null) {
            this.item = new ItemStack(m, amount);
        }

        ItemMeta meta = this.item.getItemMeta();
        if (meta == null)
            return this.item;

        if (this.name != null && !this.name.isEmpty()) {
            meta.setDisplayName(this.name);
        }
        if (this.lore != null && !this.lore.isEmpty()) {
            meta.setLore(this.lore);
        }
        if (this.enchanted) {
            meta.addEnchant(Enchantment.ARROW_DAMAGE, 1, true);
        }
        // meta.addItemFlags(ItemFlag.values());
        this.item.setItemMeta(meta);

        return this.item;
    }
}
