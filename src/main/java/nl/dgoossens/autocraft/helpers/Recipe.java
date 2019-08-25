package nl.dgoossens.autocraft.helpers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import nl.dgoossens.autocraft.AutomatedCrafting;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.*;

public class Recipe {
    @Expose private String type = ""; //crafting_shaped, crafting_shapeless or we don't care
    @Expose private JsonItem result;

    //Shaped Recipes
    @Expose private String[] pattern;
    @Expose private Map<Character, JsonElement> key; //JsonElement is either array of JsonItem or JsonItem

    //Shapeless Recipes
    @Expose private Set<JsonElement> ingredients;

    //A few NMS classes we use because 1.12 is outdated and doesn't support cool recipes yet.
    private static final Class<?> recipeChoice = getClass("org.bukkit.inventory.RecipeChoice").orElse(null);
    private static final Class<?> exactChoice = recipeChoice==null ? null : recipeChoice.getDeclaredClasses()[0];
    private static final Class<?> materialChoice = recipeChoice==null ? null : recipeChoice.getDeclaredClasses()[1];

    //Get a class and put it in an optional.
    private static Optional<Class<?>> getClass(String className) {
        try {
            return Optional.ofNullable(Class.forName(className));
        } catch(Exception x) { return Optional.empty(); }
    }

    public Recipe() {} //Needed for GSON, probably.
    public Recipe(org.bukkit.inventory.Recipe bukkitRecipe) {
        result = new JsonItem(bukkitRecipe.getResult());
        if(bukkitRecipe instanceof ShapedRecipe) {
            type = "crafting_shaped";
            pattern = ((ShapedRecipe) bukkitRecipe).getShape();
            key = new HashMap<>();
            //This system of using spigot's choicemap system doesn't work at the moment. It's the backup system anyways.
            if(MinecraftVersion.get().atLeast(MinecraftVersion.THIRTEEN) && exactChoice!=null) {
                try {
                    //This uses a Draft API so this is the backup system! We prefer loading it ourselves.
                    Map<Character, Object> choiceMap = (Map<Character, Object>) ShapedRecipe.class.getMethod("getChoiceMap").invoke(bukkitRecipe);
                    choiceMap.forEach((k, v) -> {
                        JsonElement value = null;
                        JsonArray jsonArray = new JsonArray();
                        if(v!=null) { //V can be null for some reason.
                            if(exactChoice.isAssignableFrom(v.getClass()) || materialChoice.isAssignableFrom(v.getClass())) {
                                try {
                                    List<Object> choices = (List<Object>) v.getClass().getMethod("getChoices").invoke(v);
                                    for(Object o : choices) {
                                        if(o instanceof Material) jsonArray.add(AutomatedCrafting.GSON_ITEM.toJsonTree(new JsonItem((Material) o)));
                                        else jsonArray.add(AutomatedCrafting.GSON_ITEM.toJsonTree(new JsonItem((ItemStack) o)));
                                    }
                                } catch(Exception x) { x.printStackTrace(); }
                            } else {
                                ItemStack val = null;
                                try {
                                    val = (ItemStack) recipeChoice.getMethod("getItemStack").invoke(v);
                                } catch(Exception x) { x.printStackTrace(); }
                                if(val!=null)
                                    value = AutomatedCrafting.GSON_ITEM.toJsonTree(new JsonItem(val));
                            }
                        }
                        if(value==null) value = jsonArray;
                        key.put(k, value);
                    });
                    return;
                } catch(Exception x) { x.printStackTrace(); }
            }
            ((ShapedRecipe) bukkitRecipe).getIngredientMap().forEach((k, v) -> {
                if(v==null) return;
                key.put(k, AutomatedCrafting.GSON_ITEM.toJsonTree(new JsonItem(v)));
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
