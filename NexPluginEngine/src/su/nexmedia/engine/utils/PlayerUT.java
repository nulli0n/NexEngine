package su.nexmedia.engine.utils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PlayerUT {

    public static void execCmd(@NotNull Player player, @NotNull String cmd) {
        CommandSender cs = player;
        boolean op = false;
        if (cmd.startsWith("[OP]")) {
            cmd = cmd.replace("[OP]", "");
            if (!player.isOp()) {
                op = true;
                player.setOp(op);
            }
        }
        else if (cmd.startsWith("[CONSOLE]")) {
            cmd = cmd.replace("[CONSOLE]", "");
            cs = Bukkit.getConsoleSender();
        }
        cmd = cmd.trim().replace("%player%", player.getName());
        Bukkit.dispatchCommand(cs, cmd);
        if (op) {
            player.setOp(false);
        }
    }

    @NotNull
    public static List<String> getPlayerNames() {
        List<String> list = Bukkit.getServer().getOnlinePlayers().stream().map(Player::getName)
                .collect(Collectors.toList());
        return list;
    }

    @NotNull
    public static String getIP(@NotNull Player player) {
        InetSocketAddress inet = player.getAddress();
        return inet == null ? "null" : getIP(inet.getAddress());
    }

    @NotNull
    public static String getIP(@NotNull InetAddress inet) {
        return inet.toString().replace("\\/", "").replace("/", "");
    }

    public static void setExp(@NotNull Player player, long amount) {
        amount += getTotalExperience(player);

        if (amount > 2147483647L) {
            amount = 2147483647L;
        }
        if (amount < 0L) {
            amount = 0L;
        }

        setTotalExperience(player, (int) amount);
    }

    public static void setTotalExperience(@NotNull Player player, int exp) {
        if (exp < 0) {
            throw new IllegalArgumentException("Experience is negative!");
        }
        player.setExp(0.0F);
        player.setLevel(0);
        player.setTotalExperience(0);

        int amount = exp;
        while (amount > 0) {
            int expToLevel = getExpAtLevel(player);
            amount -= expToLevel;
            if (amount >= 0) {
                player.giveExp(expToLevel);
            }
            else {
                amount += expToLevel;
                player.giveExp(amount);
                amount = 0;
            }
        }
    }

    private static int getExpAtLevel(@NotNull Player player) {
        return getExpAtLevel(player.getLevel());
    }

    public static int getExpAtLevel(int level) {
        if (level <= 15) {
            return 2 * level + 7;
        }
        if ((level >= 16) && (level <= 30)) {
            return 5 * level - 38;
        }
        return 9 * level - 158;
    }

    public static int getExpToLevel(int level) {
        int currentLevel = 0;
        int exp = 0;
        while (currentLevel < level) {
            exp += getExpAtLevel(currentLevel);
            currentLevel++;
        }
        if (exp < 0) {
            exp = Integer.MAX_VALUE;
        }
        return exp;
    }

    public static int getTotalExperience(@NotNull Player player) {
        int exp = Math.round(getExpAtLevel(player) * player.getExp());
        int currentLevel = player.getLevel();
        while (currentLevel > 0) {
            currentLevel--;
            exp += getExpAtLevel(currentLevel);
        }
        if (exp < 0) {
            exp = Integer.MAX_VALUE;
        }
        return exp;
    }

    public static int getExpUntilNextLevel(@NotNull Player player) {
        int exp = Math.round(getExpAtLevel(player) * player.getExp());
        int nextLevel = player.getLevel();
        return getExpAtLevel(nextLevel) - exp;
    }

    public static boolean hasEmptyInventory(@NotNull Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (!ItemUT.isAir(item)) {
                return false;
            }
        }
        return true;
    }

    public static int countItem(@NotNull Player player, @NotNull ItemStack item) {
        int userHas = 0;
        for (ItemStack itemHas : player.getInventory().getContents()) {
            if (!ItemUT.isAir(itemHas) && itemHas.isSimilar(item)) {
                userHas += itemHas.getAmount();
            }
        }
        return userHas;
    }

    public static void takeItem(@NotNull Player player, @NotNull ItemStack item, int amount) {
        int taken = 0;

        Inventory inventory = player.getInventory();

        for (ItemStack has : inventory.getContents()) {
            if (has == null || !has.isSimilar(item)) continue;

            int hasAmount = has.getAmount();
            if (taken + hasAmount > amount) {
                int diff = (taken + hasAmount) - amount;
                has.setAmount(diff);
                return;
            }
            
            has.setAmount(0); // Properly take item from any slot.
            if ((taken += hasAmount) == amount) {
                return;
            }
        }
    }
}
