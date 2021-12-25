package nl.dgoossens.autocraft.impl;

import nl.dgoossens.autocraft.api.CraftSolution;
import nl.dgoossens.autocraft.api.CraftingRecipe;
import nl.dgoossens.autocraft.api.RecipeType;
import nl.dgoossens.autocraft.helpers.Utils;
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
        for (var item : getItems()) {
            Utils.takeItem(inv, item);
        }
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
