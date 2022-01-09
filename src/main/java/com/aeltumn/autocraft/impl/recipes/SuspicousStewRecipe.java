package com.aeltumn.autocraft.impl.recipes;

import com.aeltumn.autocraft.impl.CustomRecipe;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements recipes for suspicious stews.
 */
public class SuspicousStewRecipe extends CustomRecipe {

    public static final Map<Material, PotionEffect> INGREDIENTS = new HashMap<>();

    static {
        addIngredient(Material.DANDELION, PotionEffectType.SATURATION, 7);
        addIngredient(Material.POPPY, PotionEffectType.NIGHT_VISION, 5);
        addIngredient(Material.BLUE_ORCHID, PotionEffectType.SATURATION, 7);
        addIngredient(Material.ALLIUM, PotionEffectType.FIRE_RESISTANCE, 4);
        addIngredient(Material.RED_TULIP, PotionEffectType.WEAKNESS, 9);
        addIngredient(Material.ORANGE_TULIP, PotionEffectType.WEAKNESS, 9);
        addIngredient(Material.WHITE_TULIP, PotionEffectType.WEAKNESS, 9);
        addIngredient(Material.PINK_TULIP, PotionEffectType.WEAKNESS, 9);
        addIngredient(Material.OXEYE_DAISY, PotionEffectType.REGENERATION, 8);
        addIngredient(Material.CORNFLOWER, PotionEffectType.JUMP, 6);
        addIngredient(Material.WITHER_ROSE, PotionEffectType.WITHER, 8);
        addIngredient(Material.LILY_OF_THE_VALLEY, PotionEffectType.POISON, 12);
        addIngredient(Material.AZURE_BLUET, PotionEffectType.BLINDNESS, 8);
    }

    private final Material ingredient;

    public SuspicousStewRecipe(NamespacedKey key, Material ingredient) {
        super(key);
        this.ingredient = ingredient;
    }

    /**
     * Adds a new suspcious stew type.
     */
    public static void addIngredient(Material material, PotionEffectType type, int duration) {
        INGREDIENTS.put(material, new PotionEffect(type, type.isInstant() ? duration : duration * 20, 0, false, true));
    }

    @Override
    public boolean creates(ItemStack stack) {
        if (stack.getType() == Material.SUSPICIOUS_STEW && stack.hasItemMeta() && stack.getItemMeta() instanceof SuspiciousStewMeta meta) {
            var type = INGREDIENTS.get(ingredient).getType();
            return meta.hasCustomEffect(type);
        }
        return false;
    }

    @Override
    public ItemStack getResultDrop() {
        var item = new ItemStack(Material.SUSPICIOUS_STEW);
        var meta = (SuspiciousStewMeta) item.getItemMeta();
        meta.addCustomEffect(INGREDIENTS.get(ingredient), true);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public List<ItemStack> getItems() {
        return List.of(new ItemStack(Material.BOWL), new ItemStack(Material.RED_MUSHROOM), new ItemStack(Material.BROWN_MUSHROOM), new ItemStack(ingredient));
    }
}
