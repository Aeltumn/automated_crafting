package nl.dgoossens.autocraft.helpers;

import com.google.gson.annotations.Expose;
import nl.dgoossens.autocraft.AutomatedCrafting;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A currently messy implementation of a json-parsable item, might get implemented in the Nucleus utilities in the future.
 */
public class JsonItem {
    @Expose private String item = "minecraft:cobblestone";
    @Expose private String tag;
    @Expose private int count = 1; //Ignored if this is not the result.
    @Expose private short data = 0;
    @Expose private String displayName;
    @Expose private List<String> lore;
    @Expose private Map<String, Integer> enchantments;

    private ItemStack stackCache;

    private static final Class<?> block = ReflectionHelper.getNMSClass("Block");
    private static final Class<?> itemClass = ReflectionHelper.getNMSClass("Item");

    public int getAmount() { return count; }
    public String getTag() { return tag; }
    public ItemStack getStack() {
        if(stackCache==null) { //This is only meant for the resulting items, not the ingredients!
            Material mat = null;
            if(MinecraftVersion.get().atLeast(MinecraftVersion.THIRTEEN))
                mat = Material.getMaterial(item.substring("minecraft:".length()).toUpperCase());
            else {
                try {
                    Method m = Material.class.getMethod("getMaterial", int.class);
                    m.setAccessible(true);
                    try {
                        mat = (Material) m.invoke(null, (int) block.getMethod("getId", block).invoke(null, block.getMethod("getByName", String.class).invoke(null, item)));
                    } catch(Exception x) { x.printStackTrace(); }
                    if(mat==null || mat==Material.AIR) {
                        try {
                            mat = (Material) m.invoke(null, (int) itemClass.getMethod("getId", itemClass).invoke(null, itemClass.getMethod("b", String.class).invoke(null, item)));
                        } catch(Exception x) { x.printStackTrace(); }
                    }
                } catch(Exception x) { x.printStackTrace(); }
            }
            if(mat==null) {
                stackCache = new ItemStack(Material.COBBLESTONE);
                return stackCache;
            }
            ItemStack its = new ItemStack(mat, Math.max(count, 1), data);
            ItemMeta meta = its.getItemMeta();
            if(displayName!=null) meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
            if(lore!=null && !lore.isEmpty()) {
                List<String> newlore = new ArrayList<>();
                for(String s : lore) newlore.add(ChatColor.translateAlternateColorCodes('&', s));
                meta.setLore(newlore);
            }
            if(enchantments!=null) enchantments.forEach((k, v) -> {
                Enchantment e = Enchantment.getByName(k);
                if(e==null) {
                    AutomatedCrafting.getInstance().getLogger().severe("Couldn't find enchantment with name "+k+"!");
                    return;
                }
                meta.addEnchant(e, v, true);
            });
            its.setItemMeta(meta);
            stackCache = its;
        }
        return stackCache;
    }

    public JsonItem() {}
    public JsonItem(Material m) {
        item = "minecraft:"+m.name().toLowerCase();
    }
    public JsonItem(ItemStack stack) {
        if(stack==null) return;
        item = "minecraft:"+stack.getType().name().toLowerCase();
        count = stack.getAmount();
        if(MinecraftVersion.get().equals(MinecraftVersion.TWELVE)) data = stack.getData().getData(); //We don't do damage values in 1.13+ because they are completely broken.
        if(stack.hasItemMeta()) {
            ItemMeta meta = stack.getItemMeta();
            if(meta==null) return;
            if(meta.hasDisplayName()) displayName = meta.getDisplayName();
            if(meta.hasLore()) lore = meta.getLore();
            if(meta.hasEnchants()) {
                enchantments = new HashMap<>();
                meta.getEnchants().forEach((k, v) -> enchantments.put(k.getName(), v));
            }
        }
    }
    @Override
    public String toString() {
        return AutomatedCrafting.GSON_ITEM.toJson(this);
    }
}