package nl.dgoossens.autocraft.helpers;

import java.lang.reflect.Method;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;

/**
 * A serializable representation of an bukkit itemstack where nbt is properly
 * saved and restored during deserialization.
 */
public final class SerializedItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Object NULL_OBJECT = null;
    private static final Class<?> craftItemStack = ReflectionHelper.getCraftBukkitClass("inventory.CraftItemStack").orElse(null);
    private static final Class<?> mojangsonParser = ReflectionHelper.getNMSClass("MojangsonParser").orElse(null);
    private static final Class<?> nbtTagCompound = ReflectionHelper.getNMSClass("NBTTagCompound").orElse(null);
    private static final Class<?> itemStack = ReflectionHelper.getNMSClass("ItemStack").orElse(null);
    private static final Method parseMethod = ReflectionHelper.getMethod(mojangsonParser, "parse", String.class).orElse(null);
    private static final Method asNMSCopyMethod = ReflectionHelper.getMethod(craftItemStack, "asNMSCopy", ItemStack.class).orElse(null);
    private static final Method hasTagMethod = ReflectionHelper.getMethod(itemStack, "hasTag").orElse(null);
    private static final Method getTagMethod = ReflectionHelper.getMethod(itemStack, "getTag").orElse(null);
    private static final Method setTagMethod = ReflectionHelper.getMethod(itemStack, "setTag", nbtTagCompound).orElse(null);
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
        if (material == null) return new ItemStack(Material.AIR);
        if (materialCache == null) {
            materialCache = Material.getMaterial(material);
        }
        ItemStack ret = new ItemStack(materialCache, amount, durability);
        try {
            Object tag = parseMethod.invoke(null, nbt);
            Object nmsStack = asNMSCopyMethod.invoke(null, ret);
            if (!tag.toString().equalsIgnoreCase("{}")) setTagMethod.invoke(nmsStack, tag);
            else setTagMethod.invoke(nmsStack, NULL_OBJECT);
            ret = (ItemStack) asCraftMirrorMethod.invoke(null, nmsStack);
        } catch (Exception x) {
            x.printStackTrace();
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
            Object tag = nbtTagCompound.newInstance();
            if ((boolean) hasTagMethod.invoke(nmsStack))
                tag = getTagMethod.invoke(nmsStack);
            nbt = tag.toString();
        } catch (Exception x) {
            x.printStackTrace();
        }
    }
}
