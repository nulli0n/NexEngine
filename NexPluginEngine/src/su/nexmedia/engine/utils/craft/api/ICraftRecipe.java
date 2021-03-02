package su.nexmedia.engine.utils.craft.api;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import su.nexmedia.engine.NexPlugin;

public class ICraftRecipe extends IAbstractRecipe {

	private boolean isShape;
	private String[] shape;
	private ItemStack[] ings;
	
	public ICraftRecipe(@NotNull NexPlugin<?> plugin, @NotNull String id, @NotNull ItemStack result, boolean isShape) {
		super(plugin, id, result);
		this.isShape = isShape;
		this.shape = new String[] {"ABC", "DEF", "GHI"};
		this.ings = new ItemStack[(int) Math.pow(this.shape.length, 2)];
		for (int i = 0; i < this.ings.length; i++) {
			this.ings[i] = new ItemStack(Material.AIR);
		}
	}
	
	public boolean isShaped() {
		return this.isShape;
	}
	
	public ItemStack[] getIngredients() {
		return this.ings;
	}
	
	@NotNull
	public String[] getShape() {
		return this.shape;
	}
	
	@Override
	public void addIngredient(int pos, @Nullable ItemStack item) {
		if (pos >= Math.pow(shape.length, 2)) {
			throw new IllegalArgumentException("Ingredient slot is out of shape size!");
		}
		
		if (item == null) item = new ItemStack(Material.AIR);
		this.ings[pos] = item;
	}

	@SuppressWarnings("deprecation")
	@Override
	@NotNull
	public Recipe getRecipe() {
		ItemStack result = this.getResult();
		NamespacedKey key = this.getKey();
		ItemStack[] ingredients = this.getIngredients();
		
		if (this.isShaped()) {
			char[] shapeChars = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I'};
			
			ShapedRecipe recipe = new ShapedRecipe(key, result);
			recipe.shape(this.getShape());
			
			for (int pos = 0; pos < ingredients.length; pos++) {
				char letter = shapeChars[pos];
				ItemStack ing = ingredients[pos];
				if (ing.hasItemMeta()) {
					recipe.setIngredient(letter, new RecipeChoice.ExactChoice(ing));
				}
				else {
					recipe.setIngredient(letter, ing.getType());
				}
			}
			return recipe;
		}
		
		ShapelessRecipe recipe = new ShapelessRecipe(key, result);
		for (ItemStack ing : ingredients) {
			if (ing.hasItemMeta()) {
				recipe.addIngredient(new RecipeChoice.ExactChoice(ing));
			}
			else {
				recipe.addIngredient(ing.getType());
			}
		}
		return recipe;
	}
}
