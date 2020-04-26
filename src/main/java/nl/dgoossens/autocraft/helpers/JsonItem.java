package nl.dgoossens.autocraft.helpers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

public class JsonItem {
    private String item;
    private int count;
    private String tag;

    // cached value
    private transient Material material;

    private static transient Class<?> tagClass = null;
    private static transient Method tagMethod = null, getTagMethod = null;
    static {
        try {
            tagClass = Class.forName("org.bukkit.Tag");
            getTagMethod = Bukkit.class.getMethod("getTag", String.class, NamespacedKey.class, Class.class);
            tagMethod = tagClass.getMethod("isTagged", Object.class);
        } catch(Exception ignored) {}
    }

    public int getCount() {
        return count;
    }

    /**
     * Returns true if this stack is similar to this json item.
     * Ignores amount.
     */
    public boolean isSimilar(ItemStack it) {
        //Tag takes priority
        if(tag != null) {
            String[] parts = tag.split(":", 2);
            try {
                Object t = getTagMethod.invoke(null, "items", new NamespacedKey(parts[0], parts[1]), Material.class);
                if((boolean) tagMethod.invoke(t, it.getType()))
                    return true;
            } catch(Exception x) {
                x.printStackTrace();
            }
            return false;
        } else {
            //Check material
            Material mat = getMaterial();
            return it.getType().equals(mat);
        }
    }

    /**
     * Get bukkit material from namespaced key.
     */
    private Material getMaterial() {
        if(material != null) return material;
        material = Material.getMaterial((item.startsWith("minecraft:") ? item.substring("minecraft:".length()) : item).toUpperCase());
        return material;
    }

    /**
     * Get the itemstack created if this item is dropped.
     */
    public ItemStack toStack() {
        //We ignore the tag here as tags can't have materials.
        return new ItemStack(getMaterial(), count);
    }
}
