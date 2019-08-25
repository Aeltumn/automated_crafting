package nl.dgoossens.autocraft.helpers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import nl.dgoossens.autocraft.AutomatedCrafting;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
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

    private static final Class<?> recipeChoice = ReflectionHelper.getOptionalNMSClass("org.bukkit.inventory.RecipeChoice").orElse(null);
    private static final Class<?> exactChoice = ReflectionHelper.getOptionalNMSClass("org.bukkit.inventory.RecipeChoice.ExactChoice").orElse(null);
    private static final Class<?> materialChoice = ReflectionHelper.getOptionalNMSClass("org.bukkit.inventory.RecipeChoice.MaterialChoice").orElse(null);

    public Recipe() {}
    public Recipe(org.bukkit.inventory.Recipe bukkitRecipe) {
        result = new JsonItem(bukkitRecipe.getResult());
        if(bukkitRecipe instanceof ShapedRecipe) {
            type = "crafting_shaped";
            pattern = ((ShapedRecipe) bukkitRecipe).getShape();
            try {
                //1.13+ only
                Map<Character, Object> choiceMap = (Map<Character, Object>) ShapedRecipe.class.getMethod("getChoiceMap").invoke(bukkitRecipe);
                choiceMap.forEach((k, v) -> {
                    JsonElement value = null;
                    JsonArray jsonArray = new JsonArray();
                    if(exactChoice.isAssignableFrom(v.getClass()) || materialChoice.isAssignableFrom(v.getClass())) {
                        try {
                            Set<Object> choices = (Set<Object>) v.getClass().getMethod("getChoices").invoke(v);
                            for(Object o : choices) {
                                if(o instanceof Material) jsonArray.add(AutomatedCrafting.GSON_ITEM.toJsonTree(new JsonItem((Material) o)));
                                else jsonArray.add(AutomatedCrafting.GSON_ITEM.toJsonTree(new JsonItem((ItemStack) o)));
                            }
                        } catch(Exception x) {}
                    } else if(v!=null) {//V can be null for some reason.
                        ItemStack val = null;
                        try {
                            val = (ItemStack) recipeChoice.getMethod("getItemStack").invoke(v);
                        } catch(Exception x) {}
                        if(val!=null)
                            value = AutomatedCrafting.GSON_ITEM.toJsonTree(new JsonItem(val));
                    }

                    if(value==null) value = jsonArray;
                    key.put(k, value);
                });
            } catch(Exception x) {
                ((ShapedRecipe) bukkitRecipe).getIngredientMap().forEach((k, v) -> {
                    if(v==null) return;
                    key.put(k, AutomatedCrafting.GSON_ITEM.toJsonTree(new JsonItem(v)));
                });
            }
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
