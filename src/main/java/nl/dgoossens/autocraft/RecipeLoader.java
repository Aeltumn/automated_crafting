package nl.dgoossens.autocraft;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import nl.dgoossens.autocraft.api.CraftingRecipe;
import nl.dgoossens.autocraft.api.RecipeType;
import nl.dgoossens.autocraft.compat.CustomCraftingCompat;
import nl.dgoossens.autocraft.helpers.BukkitRecipe;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

public class RecipeLoader {
    private final AutomatedCrafting instance;
    private final Set<CraftingRecipe> loadedRecipes = new HashSet<>();
    private final Set<String> loadedFilenames = new HashSet<>();

    public RecipeLoader(final AutomatedCrafting inst) {
        instance = inst;

        //We load the recipes once on start, this is all we do on 1.12.
        reload(null);
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
        Iterator<org.bukkit.inventory.Recipe> it = Bukkit.recipeIterator();
        while (it.hasNext()) {
            org.bukkit.inventory.Recipe bukkitRecipe = it.next();
            if (bukkitRecipe instanceof Keyed) {
                if (!loadedFilenames.contains(((Keyed) bukkitRecipe).getKey().getKey() + ".json")) {
                    //Have we already loaded it?
                    BukkitRecipe r = new BukkitRecipe(bukkitRecipe);
                    if (r.getType() == RecipeType.UNKNOWN) continue; //We don't want unknown recipes!
                    loadedFilenames.add(((Keyed) bukkitRecipe).getKey().getKey() + ".json");
                    loadedRecipes.add(r);
                }
            }
        }

        //Check if something was loaded this iteration
        long j = loadedRecipes.size();
        if (j > 0) {
            if (listener != null)
                listener.sendMessage("(Re)loaded " + j + " bukkit recipes, took " + (System.currentTimeMillis() - t) + " ms...");
            instance.getLogger().info("(Re)loaded " + j + " bukkit recipes, took " + (System.currentTimeMillis() - t) + " ms...");
        }

        //Set up for next iteration
        t = System.currentTimeMillis();

        //Custom compatibility
        if (Bukkit.getPluginManager().isPluginEnabled("CustomCrafting"))
            loadedRecipes.addAll(new CustomCraftingCompat().load());

        //Check if something was loaded this iteration
        j = loadedRecipes.size() - j;
        if (j > 0) {
            if (listener != null)
                listener.sendMessage("(Re)loaded " + j + " custom recipes from compatible plugins, took " + (System.currentTimeMillis() - t) + " ms...");
            instance.getLogger().info("(Re)loaded " + j + " custom recipes from compatible plugins, took " + (System.currentTimeMillis() - t) + " ms...");
        }
    }
}
