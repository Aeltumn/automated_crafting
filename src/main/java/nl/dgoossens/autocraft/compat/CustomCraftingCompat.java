package nl.dgoossens.autocraft.compat;

import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.handlers.RecipeHandler;
import me.wolfyscript.customcrafting.recipes.workbench.CraftingRecipe;
import me.wolfyscript.customcrafting.recipes.workbench.ShapedCraftRecipe;
import me.wolfyscript.customcrafting.recipes.workbench.ShapelessCraftRecipe;
import nl.dgoossens.autocraft.helpers.Recipe;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Compatibility with:
 * CustomCrafting v1.4 and older by WolfyScript
 */
public class CustomCraftingCompat implements CompatClass {
    public void load(final Set<Recipe> loadedRecipes) {
        RecipeHandler recipeHandler = CustomCrafting.getRecipeHandler();
        for(CraftingRecipe cr : recipeHandler.getCraftingRecipes()) {
            if(cr instanceof ShapedCraftRecipe)
                loadedRecipes.add(new Recipe(cr.getCustomResult(), ((ShapedCraftRecipe) cr).getShape(), cr.getIngredients()
                        .entrySet().parallelStream().collect(Collectors.toMap(Map.Entry::getKey, e -> new ArrayList<>(e.getValue())))));
            else if(cr instanceof ShapelessCraftRecipe)
                loadedRecipes.add(new Recipe(cr.getCustomResult(), ((ShapelessCraftRecipe) cr).getIngredientList()));
        }
    }
}
