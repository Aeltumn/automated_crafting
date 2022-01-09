package com.aeltumn.autocraft.api;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public interface CraftingRecipe {
    /**
     * Returns the key of this recipe.
     */
    NamespacedKey getKey();

    /**
     * Returns the type of this recipe.
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
     * Returns the solution for this crafting recipe based on the given inventory.
     */
    CraftSolution findSolution(Inventory inv);
}
