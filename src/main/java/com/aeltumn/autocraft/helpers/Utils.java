package com.aeltumn.autocraft.helpers;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Provides various utility functions.
 */
public class Utils {
    /**
     * Adds the given item to the given inventory. Returns whether all items were added.
     */
    public static boolean addItem(ItemStack[] storage, ItemStack it) {
        if (it == null || it.getType() == Material.AIR) return true;

        int amountToAdd = it.getAmount();
        for (var i = 0; i < storage.length; i++) {
            var itemStack = storage[i];
            if (itemStack == null) {
                // If the slot is empty we can put it here immediately
                it.setAmount(amountToAdd);
                storage[i] = it;
                return true;
            } else if (itemStack.isSimilar(it)) {
                // If there is an item we attempt to fill up the stack
                var toAdd = Math.min(itemStack.getMaxStackSize() - itemStack.getAmount(), amountToAdd);
                itemStack.setAmount(itemStack.getAmount() + toAdd);
                amountToAdd -= toAdd;

                // We can stop when we have added enough
                if (amountToAdd <= 0) return true;
            }
        }
        return false;
    }

    /**
     * Takes the given item from the given inventory.  Returns whether all items were taken.
     */
    public static boolean takeItem(ItemStack[] storage, ItemStack it) {
        if (it == null || it.getType() == Material.AIR) return true;

        int amountToTake = it.getAmount();
        for (var i = 0; i < storage.length; i++) {
            var itemStack = storage[i];
            if (itemStack == null) continue;

            if (itemStack.isSimilar(it)) {
                var toTake = Math.min(itemStack.getAmount(), amountToTake);
                if (toTake >= itemStack.getAmount()) {
                    // If we take the whole stack just remove it
                    storage[i] = null;
                } else {
                    // Take away the given amount of items
                    itemStack.setAmount(itemStack.getAmount() - toTake);
                }
                amountToTake -= toTake;

                // We can stop when we have taken enough
                if (amountToTake <= 0) return true;
            }
        }
        return false;
    }

    /**
     * Custom isSimilar implementation that supports ingredients with a
     * durability of -1.
     */
    public static boolean isSimilar(ItemStack a, ItemStack b) {
        // Documentation is a bit vague but it appears ingredients with -1 mean
        // the metadata isn't important and it should accept any type. We always
        // pass the ingredient as a so if a has a durability of -1 we only compare
        // materials. (Bukkit changes -1 to Short.MAX_VALUE)
        if (a != null && b != null && a.getDurability() == Short.MAX_VALUE) {
            return a.getType() == b.getType();
        }
        return a != null && a.isSimilar(b);
    }
}
