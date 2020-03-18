package nl.dgoossens.autocraft.helpers;

import nl.dgoossens.autocraft.api.CraftingRecipe;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.*;

/**
 * Build a recipe from item stacks in code.
 */
public class BukkitRecipe implements CraftingRecipe {
    private String type = ""; //crafting_shaped, crafting_shapeless or we don't care
    private ItemStack result;

    //Shaped Recipes
    private String[] pattern;
    private Map<Character, Collection<ItemStack>> key;

    //Shapeless Recipes
    private Collection<ItemStack> ingredients;

    //A few NMS classes we use because 1.12 is outdated and doesn't support cool recipes yet.
    private static final Class<?> recipeChoice = getClass("org.bukkit.inventory.RecipeChoice").orElse(null);
    private static final Class<?> exactChoice = recipeChoice == null ? null : recipeChoice.getDeclaredClasses()[0];
    private static final Class<?> materialChoice = recipeChoice == null ? null : recipeChoice.getDeclaredClasses()[1];

    //Get a class and put it in an optional.
    private static Optional<Class<?>> getClass(String className) {
        try {
            return Optional.ofNullable(Class.forName(className));
        } catch (Exception x) {
            return Optional.empty();
        }
    }

    public BukkitRecipe(ItemStack result, String[] pattern, Map<Character, Collection<ItemStack>> key) {
        type = "crafting_shaped";
        this.result = result;
        this.pattern = pattern;
        this.key = key;
    }

    public BukkitRecipe(ItemStack result, Collection<ItemStack> ingredients) {
        type = "crafting_shapeless";
        this.result = result;
        this.ingredients = ingredients;
    }

    /**
     * Build a recipe from a bukkit recipe.
     */
    public BukkitRecipe(Recipe bukkitRecipe) {
        result = bukkitRecipe.getResult();
        if (bukkitRecipe instanceof ShapedRecipe) {
            type = "crafting_shaped";
            pattern = ((ShapedRecipe) bukkitRecipe).getShape();
            //This system of using spigot's choicemap system doesn't work at the moment. It's the backup system anyways.
            if (MinecraftVersion.get().atLeast(MinecraftVersion.THIRTEEN) && exactChoice != null) {
                try {
                    key = new HashMap<>();
                    //This uses a Draft API so this is the backup system! We prefer loading it ourselves.
                    Map<Character, Object> choiceMap = (Map<Character, Object>) ShapedRecipe.class.getMethod("getChoiceMap").invoke(bukkitRecipe);
                    choiceMap.forEach((k, v) -> {
                        List<ItemStack> values = new ArrayList<>();
                        if (v != null) { //V can be null for some reason.
                            if (exactChoice.isAssignableFrom(v.getClass()) || materialChoice.isAssignableFrom(v.getClass())) {
                                try {
                                    List<Object> choices = (List<Object>) v.getClass().getMethod("getChoices").invoke(v);
                                    for (Object o : choices) {
                                        if (o instanceof Material) values.add(new ItemStack((Material) o));
                                        else values.add((ItemStack) o);
                                    }
                                } catch (Exception x) {
                                    x.printStackTrace();
                                }
                            } else {
                                ItemStack val = null;
                                try {
                                    val = (ItemStack) recipeChoice.getMethod("getItemStack").invoke(v);
                                } catch (Exception x) {
                                    x.printStackTrace();
                                }
                                if (val != null) values.add(val);
                            }
                        }
                        key.put(k, values);
                    });
                    return;
                } catch (Exception x) {
                    x.printStackTrace();
                }
            }
            key = new HashMap<>();
            ((ShapedRecipe) bukkitRecipe).getIngredientMap().forEach((k, v) -> {
                if (v == null) return;
                key.put(k, Arrays.asList(v));
            });
        } else if (bukkitRecipe instanceof ShapelessRecipe) {
            type = "crafting_shapeless";
            ingredients = new ArrayList<>(((ShapelessRecipe) bukkitRecipe).getIngredientList());
        }
    }

    public String getType() {
        return type.startsWith("minecraft:") ? type.substring("minecraft:".length()) : type;
    }

    @Override
    public boolean creates(ItemStack stack) {
        return result.isSimilar(stack);
    }

    @Override
    public ItemStack getResultDrop() {
        return result;
    }
}
