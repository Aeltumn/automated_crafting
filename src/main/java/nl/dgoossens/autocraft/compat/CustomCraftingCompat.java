package nl.dgoossens.autocraft.compat;

import java.util.HashSet;
import java.util.Set;
import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.handlers.DataHandler;
import nl.dgoossens.autocraft.RecipeLoader;
import nl.dgoossens.autocraft.api.CompatClass;
import nl.dgoossens.autocraft.api.CraftingRecipe;
import org.bukkit.inventory.Recipe;

/**
 * Compatibility with:
 * CustomCrafting v1.6.6.0 and newer by WolfyScript
 */
public class CustomCraftingCompat implements CompatClass {

    public Set<CraftingRecipe> load(RecipeLoader loader) {
        Set<CraftingRecipe> loadedRecipes = new HashSet<>();
        DataHandler recipeHandler = CustomCrafting.inst().getDataHandler();
        for (Recipe cr : recipeHandler.getMinecraftRecipes()) {
            loader.loadRecipe(cr);
        }
        return loadedRecipes;
    }
}
