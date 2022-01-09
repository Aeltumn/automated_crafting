package com.aeltumn.autocraft;

import com.aeltumn.autocraft.api.CraftingRecipe;
import com.aeltumn.autocraft.api.RecipeType;
import com.aeltumn.autocraft.impl.BukkitRecipe;
import com.aeltumn.autocraft.impl.recipes.FireworksRecipe;
import com.aeltumn.autocraft.impl.recipes.SuspicousStewRecipe;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

public class RecipeLoader {
    private final AutomatedCrafting instance;
    private final Set<NamespacedKey> loaded = new HashSet<>();
    private final Set<CraftingRecipe> recipes = new HashSet<>();

    public RecipeLoader(final AutomatedCrafting inst) {
        instance = inst;

        //We load the recipes the first tick after the server is done starting so
        //all recipes are loaded from data packs. We don't load asynchronously.
        Bukkit.getScheduler().runTask(inst, () -> {
            reload(null);
        });
    }

    /**
     * Get a list containing the names of all files that were loaded, includes .json!
     * For example:
     * [ acacia_boat.json, iron_ingot.json, furnace.json ]
     */
    @Deprecated(since = "2.6")
    public Set<String> getLoadedFileNames() {
        return loaded.stream().map(f -> f.getKey() + ".json").collect(Collectors.toSet());
    }

    /**
     * Returns a list of the keys of all loaded recipes.
     */
    public Set<NamespacedKey> getLoadedKeys() {
        return loaded;
    }

    /**
     * Returns the list of recipes that have been loaded.
     */
    public Set<CraftingRecipe> getLoadedRecipes() {
        return recipes;
    }

    /**
     * Get all recipes that will create the given itemstack. If the given itemstack
     * has a displayname and the recipe result does not the recipe will not be returned!
     */
    public Set<CraftingRecipe> getRecipesFor(final ItemStack item) {
        return recipes.stream().filter(f -> f.creates(item)).collect(Collectors.toSet());
    }

    /**
     * Reloads all recipes from the minecraft jar and otherwise from Bukkit.
     */
    protected void reload(final CommandSender listener) {
        if (listener != null)
            listener.sendMessage("(Re)loading recipes...");
        instance.getLogger().info("(Re)loading recipes...");

        long t = System.currentTimeMillis();
        recipes.clear();
        loaded.clear();

        // Load recipes from Bukkit, skip any that we already know.
        Iterator<Recipe> it = Bukkit.recipeIterator();
        while (it.hasNext()) {
            loadRecipe(it.next());
        }

        // Custom recipes
        loadRecipe(new FireworksRecipe(NamespacedKey.minecraft("fireworks_duration_1"), 1));
        loadRecipe(new FireworksRecipe(NamespacedKey.minecraft("fireworks_duration_2"), 2));
        loadRecipe(new FireworksRecipe(NamespacedKey.minecraft("fireworks_duration_3"), 3));

        for (var ingredient : SuspicousStewRecipe.INGREDIENTS.keySet()) {
            loadRecipe(new SuspicousStewRecipe(NamespacedKey.minecraft("suspicious_stew_" + ingredient.name().toLowerCase()), ingredient));
        }

        // Check if any recipes were loaded
        long j = recipes.size();
        if (j > 0) {
            if (listener != null)
                listener.sendMessage("(Re)loaded " + j + " recipes, took " + (System.currentTimeMillis() - t) + " ms...");
            instance.getLogger().info("(Re)loaded " + j + " recipes, took " + (System.currentTimeMillis() - t) + " ms...");
        }
    }

    /**
     * Loads a new recipe directly from a {@link CraftingRecipe} instance.
     */
    public void loadRecipe(CraftingRecipe recipe) {
        // Ignore recipes that have already been loaded
        if (loaded.contains(recipe.getKey())) return;

        // Ignore all unknown recipes
        if (recipe.getType() == RecipeType.UNKNOWN) return;

        recipes.add(recipe);
        loaded.add(recipe.getKey());
    }

    /**
     * Loads a new recipe from a {@link Recipe} instance.
     */
    public void loadRecipe(Recipe recipe) {
        if (recipe instanceof Keyed) {
            BukkitRecipe bukkitRecipe = new BukkitRecipe(recipe);
            loadRecipe(bukkitRecipe);
        }
    }
}
