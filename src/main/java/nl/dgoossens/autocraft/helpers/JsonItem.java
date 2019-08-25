package nl.dgoossens.autocraft.helpers;

import com.google.gson.annotations.Expose;
import nl.dgoossens.autocraft.AutomatedCrafting;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonItem {
    @Expose private String item = "minecraft:cobblestone";
    @Expose private String tag;
    @Expose private int count = 1; //Ignored if this is not the result.
    @Expose private short data = 0;
    @Expose private String displayName;
    @Expose private List<String> lore = new ArrayList<>();
    @Expose private Map<String, Integer> enchantments = new HashMap<>();

    private ItemStack stackCache;

    public int getAmount() { return count; }
    public String getTag() { return tag; }
    public ItemStack getStack() {
        if(stackCache==null) { //This is only meant for the resulting items, not the ingredients!
            Material mat = Material.getMaterial(item.substring("minecraft:".length()).toUpperCase());
            if(mat==null) {
                stackCache = new ItemStack(Material.COBBLESTONE);
                return stackCache;
            }
            ItemStack its = new ItemStack(mat, count, data);
            ItemMeta meta = its.getItemMeta();
            if(displayName!=null) meta.setDisplayName(displayName);
            if(!lore.isEmpty()) meta.setLore(lore);
            enchantments.forEach((k, v) -> {
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
    public JsonItem(Material m) { item = "minecraft:"+m.name().toLowerCase(); }
    public JsonItem(ItemStack stack) {
        item = "minecraft:"+stack.getType().name().toLowerCase();
        count = stack.getAmount();
        data = stack.getDurability();
        if(stack.hasItemMeta()) {
            ItemMeta meta = stack.getItemMeta();
            if(meta==null) return;
            if(meta.hasDisplayName()) displayName = meta.getDisplayName();
            if(meta.hasLore()) lore = meta.getLore();
            if(meta.hasEnchants())
                meta.getEnchants().forEach((k, v) -> enchantments.put(k.getName(), v));
        }
    }
    @Override
    public String toString() {
        return AutomatedCrafting.GSON_ITEM.toJson(this);
    }
}