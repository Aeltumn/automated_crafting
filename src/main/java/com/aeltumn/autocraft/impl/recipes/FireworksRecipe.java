package com.aeltumn.autocraft.impl.recipes;

import com.aeltumn.autocraft.impl.CustomRecipe;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.List;

/**
 * Implements recipes for flight duration fireworks.
 */
public class FireworksRecipe extends CustomRecipe {

    private final int flightDuration;

    public FireworksRecipe(NamespacedKey key, int flightDuration) {
        super(key);
        this.flightDuration = flightDuration;
    }

    @Override
    public boolean creates(ItemStack stack) {
        if (stack.getType() == Material.FIREWORK_ROCKET && stack.hasItemMeta() && stack.getItemMeta() instanceof FireworkMeta meta) {
            return meta.getPower() == flightDuration;
        }
        return false;
    }

    @Override
    public ItemStack getResultDrop() {
        var item = new ItemStack(Material.FIREWORK_ROCKET, 3);
        var meta = (FireworkMeta) item.getItemMeta();
        meta.setPower(flightDuration);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public List<ItemStack> getItems() {
        return List.of(new ItemStack(Material.PAPER), new ItemStack(Material.GUNPOWDER, flightDuration));
    }
}
