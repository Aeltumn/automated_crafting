package nl.dgoossens.autocraft.compat;

import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.handlers.RecipeHandler;
import me.wolfyscript.customcrafting.recipes.types.CustomRecipe;
import me.wolfyscript.customcrafting.recipes.types.ShapelessCraftingRecipe;
import me.wolfyscript.customcrafting.recipes.types.workbench.ShapedCraftRecipe;
import me.wolfyscript.customcrafting.recipes.types.workbench.ShapelessCraftRecipe;
import me.wolfyscript.utilities.api.custom_items.CustomItem;
import nl.dgoossens.autocraft.api.CompatClass;
import nl.dgoossens.autocraft.helpers.BukkitRecipe;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Compatibility with:
 * CustomCrafting v1.5 and newer by WolfyScript
 */
public class CustomCraftingCompat implements CompatClass {
    public Set<nl.dgoossens.autocraft.api.CraftingRecipe> load() {
        Set<nl.dgoossens.autocraft.api.CraftingRecipe> loadedRecipes = new HashSet<>();
        RecipeHandler recipeHandler = CustomCrafting.getRecipeHandler();
        for(CustomRecipe cr : recipeHandler.getRecipes().values()) {
            if(cr instanceof ShapedCraftRecipe)
                loadedRecipes.add(new BukkitRecipe(cr.getCustomResult(), ((ShapedCraftRecipe) cr).getShape(), ((ShapedCraftRecipe) cr).getIngredients()
                        .entrySet().parallelStream().collect(Collectors.toMap(Map.Entry::getKey, e -> new ArrayList<>(e.getValue())))));
            else if(cr instanceof ShapelessCraftRecipe) {
                Map<Character, List<CustomItem>> m = ((ShapelessCraftingRecipe) cr).getIngredients();
                loadedRecipes.add(new BukkitRecipe(cr.getCustomResult(), m.entrySet().parallelStream().map(f -> new ArrayList(f.getValue())).collect(Collectors.toList())));
            }
        }
        return loadedRecipes;
    }
}
