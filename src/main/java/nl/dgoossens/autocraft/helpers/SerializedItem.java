package nl.dgoossens.autocraft.helpers;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;

/**
 * A serializable representation of an bukkit itemstack where nbt is properly
 * saved and restored during deserialization.
 */
public final class SerializedItem implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final Class<?> craftItemStack = ReflectionHelper.getOptionalBukkitClass("inventory.CraftItemStack").orElse(null);
    public static final Class<?> mojangsonParser = ReflectionHelper.getOptionalNMSClass("MojangsonParser").orElse(null);
    public static final Class<?> nbtTagCompound = ReflectionHelper.getOptionalNMSClass("NBTTagCompound").orElse(null);
    public static final Class<?> itemStack = ReflectionHelper.getOptionalNMSClass("ItemStack").orElse(null);

    //All other properties of an item are stored in NBT but not material, durability or amount.
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
        ItemStack ret = new ItemStack(Material.getMaterial(material), amount, durability);
        try {
            Object tag = mojangsonParser.getMethod("parse", String.class).invoke(null, nbt);
            Object nullObject = null;
            Object nmsStack = craftItemStack.getMethod("asNMSCopy", ItemStack.class).invoke(null, ret);
            if (!tag.toString().equalsIgnoreCase("{}")) nmsStack.getClass().getMethod("setTag", nbtTagCompound).invoke(nmsStack, tag);
            else nmsStack.getClass().getMethod("setTag", nbtTagCompound).invoke(nmsStack, nullObject);
            ret = (ItemStack) craftItemStack.getMethod("asCraftMirror", itemStack).invoke(null, nmsStack);
        } catch (Exception x) { x.printStackTrace(); }
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
            Object nmsStack = craftItemStack.getMethod("asNMSCopy", ItemStack.class).invoke(null, item);
            Object tag = nbtTagCompound.newInstance();
            if ((boolean) nmsStack.getClass().getMethod("hasTag").invoke(nmsStack)) tag = nmsStack.getClass().getMethod("getTag").invoke(nmsStack);
            nbt = tag.toString();
        } catch (Exception x) { x.printStackTrace(); }
    }
}
