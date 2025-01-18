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
        addIngredient(Material.DANDELION, PotionEffectType.SATURATION, 0.35f);
        addIngredient(Material.TORCHFLOWER, PotionEffectType.NIGHT_VISION, 5.0f);
        addIngredient(Material.POPPY, PotionEffectType.NIGHT_VISION, 5.0f);
        addIngredient(Material.BLUE_ORCHID, PotionEffectType.SATURATION, 0.35f);
        addIngredient(Material.ALLIUM, PotionEffectType.FIRE_RESISTANCE, 3.0f);
        addIngredient(Material.AZURE_BLUET, PotionEffectType.BLINDNESS, 11.0f);
        addIngredient(Material.RED_TULIP, PotionEffectType.WEAKNESS, 7.0f);
        addIngredient(Material.ORANGE_TULIP, PotionEffectType.WEAKNESS, 7.0f);
        addIngredient(Material.WHITE_TULIP, PotionEffectType.WEAKNESS, 7.0f);
        addIngredient(Material.PINK_TULIP, PotionEffectType.WEAKNESS, 7.0f);
        addIngredient(Material.OXEYE_DAISY, PotionEffectType.REGENERATION, 7.0f);
        addIngredient(Material.CORNFLOWER, PotionEffectType.JUMP_BOOST, 5.0f);
        addIngredient(Material.WITHER_ROSE, PotionEffectType.WITHER, 7.0f);
        addIngredient(Material.LILY_OF_THE_VALLEY, PotionEffectType.POISON, 11.0f);
        addIngredient(Material.OPEN_EYEBLOSSOM, PotionEffectType.BLINDNESS, 11.0f);
        addIngredient(Material.CLOSED_EYEBLOSSOM, PotionEffectType.NAUSEA, 7.0f);
    }

    private final Material ingredient;

    public SuspicousStewRecipe(NamespacedKey key, Material ingredient) {
        super(key);
        this.ingredient = ingredient;
    }

    /**
     * Adds a new suspcious stew type.
     */
    public static void addIngredient(Material material, PotionEffectType type, float effectLength) {
        INGREDIENTS.put(material, new PotionEffect(type, (int) Math.floor(effectLength * 20), 0, false, true));
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
