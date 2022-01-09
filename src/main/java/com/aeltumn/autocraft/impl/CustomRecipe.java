package com.aeltumn.autocraft.impl;

import com.aeltumn.autocraft.api.CraftSolution;
import com.aeltumn.autocraft.api.CraftingRecipe;
import com.aeltumn.autocraft.api.RecipeType;
import com.aeltumn.autocraft.helpers.Utils;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * For custom recipes that vanilla handles through custom code. These are mostly
 * for crafting items with NBT data.
 */
public abstract class CustomRecipe implements CraftingRecipe, CraftSolution {
    private final NamespacedKey key;

    public CustomRecipe(NamespacedKey key) {
        this.key = key;
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }

    @Override
    public RecipeType getType() {
        return RecipeType.CUSTOM;
    }

    @Override
    public CraftSolution findSolution(Inventory inv) {
        return this;
    }

    @Override
    public boolean containsRequirements(Inventory inv) {
        for (var item : getItems()) {
            if (!inv.containsAtLeast(item, item.getAmount())) return false;
        }
        return true;
    }

    @Override
    public void applyTo(Inventory inv) {
        var contents = inv.getStorageContents();
        for (var item : getItems()) {
            Utils.takeItem(contents, item);
        }
        inv.setStorageContents(contents);
    }

    @Override
    public List<ItemStack> getContainerItems() {
        return List.of();
    }

    /**
     * The items that make up this recipe.
     */
    public abstract List<ItemStack> getItems();
}
