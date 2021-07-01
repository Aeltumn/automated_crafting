package nl.dgoossens.autocraft;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import nl.dgoossens.autocraft.api.CraftingRecipe;
import nl.dgoossens.autocraft.api.RecipeType;
import nl.dgoossens.autocraft.helpers.BukkitRecipe;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public class RecipeLoader {
    private final AutomatedCrafting instance;
    private final Set<CraftingRecipe> loadedRecipes = new HashSet<>();
    private final Set<String> loadedFilenames = new HashSet<>();

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
    public Set<String> getLoadedFileNames() {
        return loadedFilenames;
    }

    /**
     * Get all recipes that were loaded.
     * These recipes are always shapeless or shaped as we ignore furnace, stonecutter,
     * loom, etc. recipes.
     */
    public Set<CraftingRecipe> getLoadedRecipes() {
        return loadedRecipes;
    }

    /**
     * Get all recipes that will create the given itemstack. If the given itemstack
     * has a displayname and the recipe result does not the recipe will not be returned!
     */
    public Set<CraftingRecipe> getRecipesFor(final ItemStack item) {
        return loadedRecipes.stream().filter(f -> f.creates(item)).collect(Collectors.toSet());
    }

    /**
     * Reloads all recipes from the minecraft jar and otherwise from Bukkit.
     */
    protected void reload(final CommandSender listener) {
        if (listener != null)
            listener.sendMessage("(Re)loading recipes...");
        instance.getLogger().info("(Re)loading recipes...");

        long t = System.currentTimeMillis();
        loadedRecipes.clear();
        loadedFilenames.clear();

        //Load recipes from Bukkit, skip any that we already know.
        Iterator<Recipe> it = Bukkit.recipeIterator();
        while (it.hasNext()) {
            loadRecipe(it.next());
        }

        //Check if something was loaded this iteration
        long j = loadedRecipes.size();
        if (j > 0) {
            if (listener != null)
                listener.sendMessage("(Re)loaded " + j + " recipes, took " + (System.currentTimeMillis() - t) + " ms...");
            instance.getLogger().info("(Re)loaded " + j + " recipes, took " + (System.currentTimeMillis() - t) + " ms...");
        }

        //Set up for next iteration
        t = System.currentTimeMillis();

        //Check if something was loaded this iteration
        j = loadedRecipes.size() - j;
        if (j > 0) {
            if (listener != null)
                listener.sendMessage("(Re)loaded " + j + " custom recipes from compatible plugins, took " + (System.currentTimeMillis() - t) + " ms...");
            instance.getLogger().info("(Re)loaded " + j + " custom recipes from compatible plugins, took " + (System.currentTimeMillis() - t) + " ms...");
        }
    }

    /**
     * Loads a new recipe from a {@link Recipe} instance.
     */
    public void loadRecipe(Recipe recipe) {
        if (recipe instanceof Keyed) {
            if (!loadedFilenames.contains(((Keyed) recipe).getKey().getKey() + ".json")) {
                //Have we already loaded it?
                BukkitRecipe r = new BukkitRecipe(recipe);
                if (r.getType() == RecipeType.UNKNOWN) return; //We don't want unknown recipes!
                loadedFilenames.add(((Keyed) recipe).getKey().getKey() + ".json");
                loadedRecipes.add(r);
            }
        }
    }
}
