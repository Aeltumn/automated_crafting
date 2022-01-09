package com.aeltumn.autocraft.api;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * A solution to a crafting recipe based on the contents of an autocrafter.
 */
public interface CraftSolution {

    /**
     * Applies this solution to the given inventory.
     */
    void applyTo(Inventory inv);

    /**
     * Returns the list of container items created by this solution.
     */
    List<ItemStack> getContainerItems();
}
