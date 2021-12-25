package nl.dgoossens.autocraft.helpers;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

/**
 * Provides various utility functions.
 */
public class Utils {
    /**
     * Takes the given item from the given inventory.
     */
    public static void takeItem(Inventory inv, ItemStack it) {
        if (it == null || it.getType() == Material.AIR) return;

        ItemStack[] storage = inv.getStorageContents();
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
                if (amountToTake <= 0) break;
            }
        }
        inv.setStorageContents(storage);
    }

    /**
     * Returns whether we can still fit a given item into an inventory.
     */
    public static boolean canInventorySupport(Inventory inv, ItemStack it) {
        if (it == null || it.getType() == Material.AIR) return true;

        ItemStack[] storage = inv.getStorageContents();
        int amountToDeposit = it.getAmount();
        for (ItemStack itemStack : storage) {
            if (itemStack == null) return true;
            if (itemStack.isSimilar(it)) {
                // Remove how much we can still fit on stack from the amount to
                // deposit
                amountToDeposit -= (itemStack.getMaxStackSize() - itemStack.getAmount());

                // If we deposited everything we fit it in.
                if (amountToDeposit <= 0) return true;
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
