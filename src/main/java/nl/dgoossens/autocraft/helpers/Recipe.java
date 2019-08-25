package nl.dgoossens.autocraft.helpers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import nl.dgoossens.autocraft.AutomatedCrafting;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Recipe {
    @Expose private String type = ""; //crafting_shaped, crafting_shapeless or we don't care
    @Expose private JsonItem result;

    //Shaped Recipes
    @Expose private String[] pattern;
    @Expose private Map<Character, JsonElement> key = new HashMap<>(); //JsonElement is either array of JsonItem or JsonItem

    //Shapeless Recipes
    @Expose private Set<JsonElement> ingredients = new HashSet<>();

    public Recipe() {}
    public Recipe(org.bukkit.inventory.Recipe bukkitRecipe) {
        result = new JsonItem(bukkitRecipe.getResult());
        if(bukkitRecipe instanceof ShapedRecipe) {
            type = "crafting_shaped";
            pattern = ((ShapedRecipe) bukkitRecipe).getShape();
            ((ShapedRecipe) bukkitRecipe).getChoiceMap().forEach((k, v) -> {
                JsonElement value = null;
                JsonArray jsonArray = new JsonArray();
                if(v instanceof RecipeChoice.ExactChoice) {
                    ((RecipeChoice.ExactChoice) v).getChoices()
                            .forEach(i -> jsonArray.add(AutomatedCrafting.GSON_ITEM.toJsonTree(new JsonItem(i))));
                } else if(v instanceof RecipeChoice.MaterialChoice) {
                    ((RecipeChoice.MaterialChoice) v).getChoices()
                            .forEach(i -> jsonArray.add(AutomatedCrafting.GSON_ITEM.toJsonTree(new JsonItem(i))));
                } else if(v!=null) //V can be null for some reason.
                    value = AutomatedCrafting.GSON_ITEM.toJsonTree(new JsonItem(v.getItemStack()));

                if(value==null) value = jsonArray;
                key.put(k, value);
            });
        } else if(bukkitRecipe instanceof ShapelessRecipe) {
            type = "crafting_shapeless";
            ingredients = new HashSet<>();
            ((ShapelessRecipe) bukkitRecipe).getIngredientList().forEach(in -> ingredients.add(AutomatedCrafting.GSON_ITEM.toJsonTree(new JsonItem(in))));
        }
    }

    public String getType() { return type.startsWith("minecraft:") ? type.substring("minecraft:".length()) : type; }
    public JsonItem getResult() { return result; }
    public String[] getPattern() { return pattern; }
    public Map<Character, JsonElement> getKeys() { return key; }
    public Set<JsonElement> getIngredients() { return ingredients; }

    @Override
    public String toString() {
        return AutomatedCrafting.GSON_ITEM.toJson(this);
    }
}
