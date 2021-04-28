package su.nexmedia.engine.utils.craft;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.manager.api.Loadable;
import su.nexmedia.engine.utils.craft.api.IAbstractRecipe;

public class CraftManager implements Loadable {

    private NexEngine plugin;
    private Set<NamespacedKey> registered;

    public CraftManager(@NotNull NexEngine engine) {
        this.plugin = engine;
    }

    @Override
    public void setup() {
        this.registered = new HashSet<>();
    }

    @Override
    public void shutdown() {
        this.unregisterAll();
        this.registered.clear();
    }

    public boolean register(@NotNull IAbstractRecipe recipe) {
        Recipe bukkitRecipe = recipe.getRecipe();
        try {
            if (!this.plugin.getServer().addRecipe(bukkitRecipe)) {
                this.plugin.error("Could not register recipe: '" + recipe.getId() + "': Unknown reason.");
                return false;
            }
        }
        catch (Exception ex) {
            this.plugin.error("Could not register recipe: '" + recipe.getId() + "': ");
            ex.printStackTrace();
            return false;
        }

        this.discoverRecipe(recipe.getKey());
        this.plugin.info("Recipe registered: '" + recipe.getId() + "' !");
        return true;
    }

    public void discoverRecipe(@NotNull NamespacedKey key) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player == null)
                continue;

            player.discoverRecipe(key);
            // this.plugin.info("Recipe undiscover for " + p.getName() + ": " + b + " (" +
            // key.getKey() + ")");
        }
    }

    private void undiscoverRecipe(@NotNull NamespacedKey key) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player == null)
                continue;

            player.undiscoverRecipe(key);
            // this.plugin.info("Recipe undiscover for " + p.getName() + ": " + b + " (" +
            // key.getKey() + ")");
        }
    }

    public void unregisterAll() {
        Iterator<Recipe> iter = this.plugin.getServer().recipeIterator();
        while (iter.hasNext()) {
            Recipe recipe = iter.next();
            NamespacedKey recipeKey = getRecipeKey(recipe);
            if (recipeKey != null && this.registered.remove(recipeKey)) {
                this.undiscoverRecipe(recipeKey);
                this.plugin.info("Recipe unregistered: '" + recipeKey.getKey() + "' !");
                iter.remove();
            }
        }
    }

    public void unregister(@NotNull IAbstractRecipe recipe) {
        this.unregister(recipe.getId());
    }

    public void unregister(@NotNull String id) {
        id = id.toLowerCase();
        Iterator<Recipe> iter = this.plugin.getServer().recipeIterator();
        while (iter.hasNext()) {
            Recipe recipe = iter.next();
            NamespacedKey key = getRecipeKey(recipe);
            if (key != null && key.getKey().endsWith(id) && this.registered.remove(key)) {
                this.undiscoverRecipe(key);
                this.plugin.info("Recipe unregistered: '" + id + "' !");
                iter.remove();
            }
        }
    }

    @Nullable
    public static NamespacedKey getRecipeKey(@NotNull Recipe recipe) {
        if (recipe instanceof ShapedRecipe) {
            return (((ShapedRecipe) recipe).getKey());
        }
        else if (recipe instanceof ShapelessRecipe) {
            return (((ShapelessRecipe) recipe).getKey());
        }
        else if (recipe instanceof FurnaceRecipe) {
            return (((FurnaceRecipe) recipe).getKey());
        }
        return null;
    }
}
