package nl.dgoossens.autocraft.compat;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.handlers.RecipeHandler;
import me.wolfyscript.customcrafting.recipes.types.ICustomRecipe;
import me.wolfyscript.customcrafting.recipes.types.workbench.ShapedCraftRecipe;
import me.wolfyscript.customcrafting.recipes.types.workbench.ShapelessCraftRecipe;
import me.wolfyscript.utilities.api.custom_items.CustomItem;
import nl.dgoossens.autocraft.api.CompatClass;
import nl.dgoossens.autocraft.helpers.BukkitRecipe;
import org.bukkit.inventory.ItemStack;

/**
 * Compatibility with:
 * CustomCrafting v1.6 and newer by WolfyScript
 */
public class CustomCraftingCompat implements CompatClass {
    public Set<nl.dgoossens.autocraft.api.CraftingRecipe> load() {
        Set<nl.dgoossens.autocraft.api.CraftingRecipe> loadedRecipes = new HashSet<>();
        RecipeHandler recipeHandler = CustomCrafting.getInst().getRecipeHandler();
        for (ICustomRecipe cr : recipeHandler.getRecipes().values()) {
            if (cr instanceof ShapedCraftRecipe) {
                Map<Character, List<CustomItem>> ingredients = ((ShapedCraftRecipe) cr).getIngredients();
                loadedRecipes.add(new BukkitRecipe(cr.getCustomResult().create(), ((ShapedCraftRecipe) cr).getShape(),
                        ingredients.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, this::parseIntoItemStacks))));
            } else if (cr instanceof ShapelessCraftRecipe) {
                Map<Character, List<CustomItem>> m = ((ShapelessCraftRecipe) cr).getIngredients();
                loadedRecipes.add(new BukkitRecipe(cr.getCustomResult().create(),
                        m.entrySet().stream().map(this::parseIntoItemStacks).collect(Collectors.toList())));
            }
        }
        return loadedRecipes;
    }

    private List<ItemStack> parseIntoItemStacks(Map.Entry<Character, List<CustomItem>> customItems) {
        return customItems.getValue().stream().map(CustomItem::create).collect(Collectors.toList());
    }
}
