package nl.dgoossens.autocraft.api;

import java.util.ArrayList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public interface CraftingRecipe {
    /**
     * The type of this recipe.
     */
    RecipeType getType();

    /**
     * Return true if this recipe can create this item stack.
     * NBT of input item matters!
     */
    boolean creates(ItemStack stack);

    /**
     * Get the item dropped if the crafting was successful.
     */
    ItemStack getResultDrop();

    /**
     * Check if an inventory contains the required items.
     */
    boolean containsRequirements(Inventory inv);

    /**
     * Take the materials from the inventory that are required for this craft.
     * Put resulting container items into the returned array list.
     */
    ArrayList<ItemStack> takeMaterials(Inventory inv);

    /**
     * Get the 'container item' which is the item
     * left in the crafting area after an item is used
     * in a crafting recipe.
     * <p>
     * Returns null if nothing/air is the container item.
     */
    default ItemStack getContainerItem(ItemStack input) {
        var remainingItem = input.getType().getCraftingRemainingItem();
        if (remainingItem == null) return null;
        return new ItemStack(remainingItem, input.getAmount());
    }
}
