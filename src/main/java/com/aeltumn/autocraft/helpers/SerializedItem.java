package com.aeltumn.autocraft.helpers;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * A serializable representation of an bukkit itemstack where nbt is properly
 * saved and restored during deserialization.
 */
public final class SerializedItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Object NULL_OBJECT = null;
    private static final Class<?> craftItemStack = ReflectionHelper.getCraftBukkitClass("inventory.CraftItemStack").orElse(null);
    private static final Class<?> mojangsonParser = ReflectionHelper.getNMSClass("nbt.MojangsonParser").orElse(null);
    private static final Class<?> nbtTagCompound = ReflectionHelper.getNMSClass("nbt.NBTTagCompound").orElse(null);
    private static final Class<?> itemStack = ReflectionHelper.getNMSClass("world.item.ItemStack").orElse(null);
    private static final Method parseMethod = ReflectionHelper.getMethod(mojangsonParser, "a", String.class).orElse(null);
    private static final Method asNMSCopyMethod = ReflectionHelper.getMethod(craftItemStack, "asNMSCopy", ItemStack.class).orElse(null);
    private static final Method getTagMethod = ReflectionHelper.getMethod(itemStack, "getTagClone").orElse(null);
    private static final Method setTagMethod = ReflectionHelper.getMethod(itemStack, "setTagClone", nbtTagCompound).orElse(null);
    private static final Method asCraftMirrorMethod = ReflectionHelper.getMethod(craftItemStack, "asCraftMirror", itemStack).orElse(null);

    //All other properties of an item are stored in NBT but not material, durability or amount.
    private transient Material materialCache;
    private String material;
    private short durability;
    private int amount;
    private String nbt;

    //For any newInstance() calls in serializers.
    protected SerializedItem() {
    }

    /**
     * Create a new serialized item from an ItemStack.
     */
    public SerializedItem(ItemStack item) {
        build(item);
    }

    /**
     * Create an item stack from this serialized item.
     * Has the same properties, metadata and NBT as the
     * original item.
     */
    public ItemStack getItem() {
        return getItem(false);
    }

    /**
     * Create an item stack from this serialized item.
     * Has the same properties, metadata and NBT as the
     * original item.
     *
     * @param legacyMaterial True if the material should be converted
     *                       from the 1.12 names to the modern
     *                       names. Useful for loading old configuration
     *                       files.
     */
    public ItemStack getItem(boolean legacyMaterial) {
        if (material == null) return new ItemStack(Material.AIR);
        if (materialCache == null) {
            materialCache = Material.getMaterial(material, legacyMaterial);
        }

        ItemStack ret = new ItemStack(materialCache, amount, durability);
        if (nbt != null) {
            try {
                Object tag = parseMethod.invoke(null, nbt);
                Object nmsStack = asNMSCopyMethod.invoke(null, ret);
                if (!tag.toString().equalsIgnoreCase("{}")) setTagMethod.invoke(nmsStack, tag);
                else setTagMethod.invoke(nmsStack, NULL_OBJECT);
                ret = (ItemStack) asCraftMirrorMethod.invoke(null, nmsStack);
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * Builds this object's data from the ItemStack
     * provided.
     */
    private void build(ItemStack item) {
        if (item == null) return;
        ItemStack copy = item.clone();
        material = copy.getType().name();
        amount = copy.getAmount();
        durability = copy.getDurability();
        try {
            Object nmsStack = asNMSCopyMethod.invoke(null, item);
            Object tag = getTagMethod.invoke(nmsStack);
            if (tag != null) {
                nbt = tag.toString();
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
    }
}
