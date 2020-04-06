package nl.dgoossens.autocraft.helpers;

import nl.dgoossens.autocraft.AutomatedCrafting;
import nl.dgoossens.autocraft.api.CraftingRecipe;
import nl.dgoossens.autocraft.api.RecipeType;
import org.bukkit.Material;
import org.bukkit.inventory.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Build a recipe from item stacks in code.
 * These recipes have a prebuilt list of items they search for that
 * can be re-used for both testing if the items are contained in the
 * inventory as for taking them.
 */
public class BukkitRecipe implements CraftingRecipe {
    private RecipeType type = RecipeType.UNKNOWN;
    private ItemStack result;
    private Set<RecipeRequirement> requirements;

    //Shaped Recipes
    private String[] pattern;
    private Map<Character, Collection<ItemStack>> key;

    //Shapeless Recipes
    private Collection<List<ItemStack>> ingredients;

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
        type = RecipeType.SHAPED;
        this.result = result;
        this.pattern = pattern;
        this.key = key;
    }

    public BukkitRecipe(ItemStack result, Collection<List<ItemStack>> ingredients) {
        type = RecipeType.SHAPELESS;
        this.result = result;
        this.ingredients = ingredients;
    }

    /**
     * Build a recipe from a bukkit recipe.
     */
    public BukkitRecipe(Recipe bukkitRecipe) {
        result = bukkitRecipe.getResult();
        if (bukkitRecipe instanceof ShapedRecipe) {
            type = RecipeType.SHAPED;
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
            } else {
                key = new HashMap<>();
                ((ShapedRecipe) bukkitRecipe).getIngredientMap().forEach((k, v) -> {
                    if (v == null) return;
                    key.put(k, Collections.singletonList(v));
                });
            }
        } else if (bukkitRecipe instanceof ShapelessRecipe) {
            type = RecipeType.SHAPELESS;
            //This system of using spigot's choicemap system doesn't work at the moment. It's the backup system anyways.
            if (MinecraftVersion.get().atLeast(MinecraftVersion.THIRTEEN) && exactChoice != null) {

            } else {
                ingredients = ((ShapelessRecipe) bukkitRecipe).getIngredientList().parallelStream().map(Collections::singletonList).collect(Collectors.toList());
            }
        }
    }

    public RecipeType getType() {
        return type;
    }

    /**
     * The requirements map for this recipe, can be cached.
     */
    private Set<RecipeRequirement> getRequirements() {
        if(requirements == null) {
            requirements = new HashSet<>();
            switch(type) {
                case SHAPED:
                    //Count how many times each character in the pattern occurrences
                    //Crafting recipes cannot have two items in a single slot.
                    Map<Character, Integer> occurrences = new HashMap<>();
                    for(String s : pattern) {
                        for(char c : s.toCharArray()) {
                            occurrences.put(c, occurrences.getOrDefault(c, 0) + 1);
                        }
                    }
                    //Put the corresponding item for each part of the shape into the requirements list
                    occurrences.forEach((c, i) -> {
                        RecipeRequirement rr = new RecipeRequirement(new ArrayList<>(key.getOrDefault(c, new ArrayList<>())), i);
                        //Return if invalid (key does not exit in map)
                        if(rr.isInvalid()) {
                            AutomatedCrafting.getInstance().warning("Warning shaped recipe with pattern [["+ String.join("], [", pattern) +"]] had character in pattern not in key map.");
                            return;
                        }

                        //Try to merge this recipe requirement and otherwise add it
                        if(!requirements.parallelStream().filter(r -> r.overlap(rr)).map(r -> r.increment(rr.amount)).findAny().isPresent())
                            requirements.add(rr);
                    });
                    break;
                case SHAPELESS:
                    ingredients.forEach(i -> {
                        RecipeRequirement rr = new RecipeRequirement(i, 1);

                        //Try to merge this recipe requirement and otherwise add it
                        if(!requirements.parallelStream().filter(r -> r.overlap(rr)).map(r -> r.increment(rr.amount)).findAny().isPresent())
                            requirements.add(rr);
                    });
                    break;
            }
        }
        return requirements;
    }

    @Override
    public boolean containsRequirements(Inventory inv) {
        Set<RecipeRequirement> requirements = getRequirements();

        //Test if any requirements are NOT met
        ItemStack[] contents = new ItemStack[inv.getStorageContents().length];
        for(int j = 0; j < contents.length; j++)
            contents[j] = inv.getStorageContents()[j].clone(); //Build clones so we can track which items we've already used as a component.
        for(RecipeRequirement r : requirements) {
            if(!r.isContainedInInventory(contents))
                return false;
        }
        return true;
    }

    @Override
    public ArrayList<ItemStack> takeMaterials(Inventory inv) {
        Set<RecipeRequirement> requirements = getRequirements();
        ArrayList<ItemStack> ret = new ArrayList<>();
        requirements.parallelStream().forEach(rr -> {
            int amountToTake = rr.amount;
            //Try each item one by one to see if we can take that item from our inventory.
            for(ItemStack i : rr.item) {
                int remain = takeFromInventory(inv, i, amountToTake);
                //This item was used and something was taken
                if(remain != amountToTake) {
                    ItemStack res = getContainerItem(i);
                    res.setAmount(amountToTake - remain); //How many did we take, that's how many of this container item should be put back.
                    ret.add(res);
                }
                amountToTake = remain;
                //We don't need to keep trying to complete this requirement if we've already done so.
                if(amountToTake <= 0) break;
            }
        });
        return ret;
    }

    public int takeFromInventory(Inventory inv, ItemStack item, int limit) {
        if (item == null) {
            return limit;
        } else {
            ItemStack[] its;
            int l = (its = inv.getStorageContents()).length;

            for(int j = 0; j < l; ++j) {
                ItemStack i = its[j];
                if (item.isSimilar(i)) {
                    int cap = Math.min(limit, i.getAmount());
                    i.setAmount(i.getAmount() - cap);
                    //TODO check if clearing the slot does what I assume it does.
                    if(i.getAmount() == 0) inv.clear(j); //Clear the slot
                    return limit - cap;
                }
            }

            return limit;
        }
    }

    @Override
    public boolean creates(ItemStack stack) {
        return result.isSimilar(stack);
    }

    @Override
    public ItemStack getResultDrop() {
        return result;
    }

    /**
     * A single recipe requirement.
     * Each recipe requirement is unique and no two recipe requirements
     * can contain similar items.
     *
     * Due to technical limitations weird edge-cases may occur when two requirements
     * are able to use the same item for crafting.
     */
    public static class RecipeRequirement {
        private List<ItemStack> item;
        private int amount;

        public RecipeRequirement(List<ItemStack> items, int amount) {
            this.item = items;
            this.amount = amount;
        }

        private boolean isInvalid() {
            return item == null || item.isEmpty();
        }

        //Increment the amount of this recipe with the given amount.
        private RecipeRequirement increment(int amount) {
            this.amount += amount;
            return this;
        }

        /**
         * Returns true if two requirements overlap.
         */
        private boolean overlap(RecipeRequirement other) {
            for(ItemStack i : item) {
                //If any item items overlap between the two we call them overlapping.
                if(other.item.parallelStream().anyMatch(j -> j.isSimilar(i))) {
                    if(item.size() != other.item.size())
                        AutomatedCrafting.getInstance().warning("A recipe incorrectly merged two recipe requirements, please make sure in recipes no two slots are allowed to contain the same item unless they are fully identical. E.g. don't have a shapeless recipe with paper and paper or leather.");
                    return true;
                }
            }
            return false;
        }

        private boolean isContainedInInventory(ItemStack[] itemList) {
            int amountToFind = amount;
            for(ItemStack it : itemList) {
                //If any item in our array of valid items is similar to this item we have found our match.
                if (item.parallelStream().anyMatch(f -> f.isSimilar(it))) {
                    int cap = Math.min(it.getAmount(), amountToFind);
                    it.setAmount(it.getAmount() - cap); //Decrease item by amount so we can use it again for the next item.
                    amountToFind -= cap;

                    //If we have at least the amount of any valid item in this inventory we call it good.
                    if(amountToFind <= 0)
                        return true;
                }
            }
            return false;
        }
    }
}
