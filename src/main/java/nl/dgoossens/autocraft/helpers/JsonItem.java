package nl.dgoossens.autocraft.helpers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

public class JsonItem {
    private String item;
    private transient Material material;
    private int count;

    private int data; //1.12 only
    private String tag; //1.14+ only

    //1.12 only
    private static final Class<?> block = ReflectionHelper.getNMSClass("Block");
    private static final Class<?> itemClass = ReflectionHelper.getNMSClass("Item");

    //1.13+ only
    private static transient Class<?> tagClass = null;
    private static transient Method tagMethod = null, getTagMethod = null;
    static {
        try {
            tagClass = Class.forName("org.bukkit.Tag");
            getTagMethod = Bukkit.class.getMethod("getTag", String.class, NamespacedKey.class, Class.class);
            tagMethod = tagClass.getMethod("isTagged", Object.class);
        } catch(Exception x) { } //Tags don't exist before 1.14.
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
            //See if data is correct but only on 1.12 as durability is weird on 1.13+
            if(MinecraftVersion.get() == MinecraftVersion.TWELVE && it.getDurability() != data) return false;

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
        if (MinecraftVersion.get().atLeast(MinecraftVersion.THIRTEEN))
            material = Material.getMaterial(item.substring("minecraft:".length()).toUpperCase());
        else {
            try {
                Method m = Material.class.getMethod("getMaterial", int.class);
                m.setAccessible(true);
                try {
                    material = (Material) m.invoke(null, (int) block.getMethod("getId", block).invoke(null, block.getMethod("getByName", String.class).invoke(null, item)));
                } catch (Exception ignored) {
                }
                if (material == null || material == Material.AIR) {
                    try {
                        material = (Material) m.invoke(null, (int) itemClass.getMethod("getId", itemClass).invoke(null, itemClass.getMethod("b", String.class).invoke(null, item)));
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return material;
    }
}
