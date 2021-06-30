package nl.dgoossens.autocraft.api;

import java.util.List;
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
    List<ItemStack> takeMaterials(Inventory inv);
}
