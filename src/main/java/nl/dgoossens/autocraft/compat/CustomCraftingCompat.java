package nl.dgoossens.autocraft.compat;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import me.wolfyscript.customcrafting.Registry;
import me.wolfyscript.customcrafting.recipes.types.workbench.ShapedCraftRecipe;
import me.wolfyscript.customcrafting.recipes.types.workbench.ShapelessCraftRecipe;
import me.wolfyscript.customcrafting.utils.recipe_item.Ingredient;
import nl.dgoossens.autocraft.RecipeLoader;
import nl.dgoossens.autocraft.api.CompatClass;
import nl.dgoossens.autocraft.api.CraftingRecipe;
import nl.dgoossens.autocraft.helpers.BukkitRecipe;
import org.bukkit.inventory.ItemStack;

/**
 * Compatibility with:
 * CustomCrafting v1.6.6.0 and newer by WolfyScript
 */
public class CustomCraftingCompat implements CompatClass {

    public Set<CraftingRecipe> load(RecipeLoader loader) {
        Set<CraftingRecipe> loadedRecipes = new HashSet<>();
        Registry.RECIPES.getAvailable().forEach(customRecipe -> {
            if (customRecipe instanceof ShapedCraftRecipe) {
                Map<Character, Ingredient> ingredients = ((ShapedCraftRecipe) customRecipe).getIngredients();
                loadedRecipes.add(new BukkitRecipe(customRecipe.getResult().getItemStack(), ((ShapedCraftRecipe) customRecipe).getShape(),
                        ingredients.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, this::parseIntoItemStacks))));
            } else if (customRecipe instanceof ShapelessCraftRecipe) {
                Map<Character, Ingredient> m = ((ShapelessCraftRecipe) customRecipe).getIngredients();
                loadedRecipes.add(new BukkitRecipe(customRecipe.getResult().getItemStack(),
                        m.entrySet().stream().map(this::parseIntoItemStacks).collect(Collectors.toList())));
            }
        });
        return loadedRecipes;
    }

    private List<ItemStack> parseIntoItemStacks(Map.Entry<Character, Ingredient> customItems) {
        return customItems.getValue().getBukkitChoices();
    }
}
