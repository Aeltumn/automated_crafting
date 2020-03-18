package nl.dgoossens.autocraft.api;

import org.bukkit.inventory.ItemStack;

public interface CraftingRecipe {
    /**
     * The type of this recipe. Should be 'crafting_shaped' or
     * 'crafting_shapeless' without 'minecraft:' prefix.
     */
    String getType();

    /**
     * Return true if this recipe can create this item stack.
     * NBT of input item matters!
     */
    boolean creates(ItemStack stack);

    /**
     * Get the item dropped if the crafting was successfull.
     */
    ItemStack getResultDrop();
}
