package nl.dgoossens.autocraft.helpers;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static nl.dgoossens.autocraft.helpers.ReflectionHelper.getBukkitClass;
import static nl.dgoossens.autocraft.helpers.ReflectionHelper.getNMSClass;

/**
 * Copied from Nucleus at https://github.com/daniel-goossens/nucleus/blob/develop/src/main/java/nl/dgoossens/nucleus/utils/spigot/item/NBTUtils.java
 * 25/08/2019 13:00
 */
public class NBTUtils {
    public static final Class<?> craftItemStack = getBukkitClass("inventory.CraftItemStack");
    public static final Class<?> nbtTagCompound = getNMSClass("NBTTagCompound");
    public static final Class<?> itemStack = getNMSClass("ItemStack");

    /**
     * Returns the length of the item's NBT data.
     */
    public static int getNBTLength(ItemStack it) {
        try {
            return getTag(it).toString().length();
        } catch(Exception x) { x.printStackTrace(); }
        return -1;
    }

    /**
     * Return a list of all nbt values stored in this item.
     */
    public static Map<String, Object> listNBT(ItemStack it) {
        try {
            Object tag = getTag(it);
            Map<String, Object> content = new HashMap<>();
            //Booleans are stored as false = byte0, true = byte1
            //UUIDs are stored as two longs
            for(String keys : (Set<String>) tag.getClass().getMethod("getKeys").invoke(tag))
                content.put(keys, getNBTData(it, keys, getMethod(tag.getClass().getMethod("get", String.class).invoke(tag, keys).getClass())));
            return content;
        } catch(Exception x) { x.printStackTrace(); }
        return new HashMap<>();
    }

    /**
     * Removes a NBT key from the item.
     */
    public static ItemStack removeNBT(ItemStack it, String key) {
        try {
            Object tag = getTag(it);
            nbtTagCompound.getMethod("remove", String.class).invoke(tag, "nbt_"+key);

            //Determine/set/update nucleus code
            String nucleusCode = "";
            if((boolean) tag.getClass().getMethod("hasKey", String.class).invoke(tag, "nucleus")) nucleusCode = (String) tag.getClass().getMethod("getString", String.class).invoke(tag, "nucleus");
            nucleusCode = nucleusCode.replaceAll("\\(nbt_"+key+"\\)", "");
            if(!nucleusCode.equalsIgnoreCase("")) nbtTagCompound.getMethod("setString", String.class, String.class).invoke(tag, "nucleus", nucleusCode);
            else nbtTagCompound.getMethod("remove", String.class).invoke(tag, "nucleus");

            return setTag(it, tag);
        } catch(Exception x) { x.printStackTrace(); }
        return it;
    }

    /**
     * Sets the NBT of an item. The key is automatically prefixed with
     * nbt_, so for {@link #getRegularNBT(ItemStack, String)} you would need to
     * fetch "nbt_key", or use {@link #getNucleusNBT(ItemStack, String)}
     * where you can use the regular "key"
     * @return The modified item
     */
    public static ItemStack setNBT(ItemStack it, String key, Object value) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("nbt_"+key, value);
            return setMultipleNBT(it, data);
        } catch(Exception x) { x.printStackTrace(); }
        return it;
    }

    /**
     * Internal method used for mainly setting ID's in {@link Item}
     */
    protected static ItemStack setMultipleNBT(ItemStack it, Map<String, Object> values) {
        try {
            Object tag = getTag(it);
            String nucleusCode = "";
            if((boolean) tag.getClass().getMethod("hasKey", String.class).invoke(tag, "nucleus")) nucleusCode = (String) tag.getClass().getMethod("getString", String.class).invoke(tag, "nucleus");
            for(String f : values.keySet()) nucleusCode+="("+f+")";
            nbtTagCompound.getMethod("setString", String.class, String.class).invoke(tag, "nucleus", nucleusCode);
            for(String s : values.keySet()) {
                final Object o = values.get(s);
                if(byte.class.isAssignableFrom(o.getClass()))
                    nbtTagCompound.getMethod("setByte", String.class, byte.class).invoke(tag, s, o);
                else if(short.class.isAssignableFrom(o.getClass()))
                    nbtTagCompound.getMethod("setShort", String.class, short.class).invoke(tag, s, o);
                else if(int.class.isAssignableFrom(o.getClass()))
                    nbtTagCompound.getMethod("setInt", String.class, int.class).invoke(tag, s, o);
                else if(long.class.isAssignableFrom(o.getClass()))
                    nbtTagCompound.getMethod("setLong", String.class, long.class).invoke(tag, s, o);
                else if(float.class.isAssignableFrom(o.getClass()))
                    nbtTagCompound.getMethod("setFloat", String.class, float.class).invoke(tag, s, o);
                else if(double.class.isAssignableFrom(o.getClass()))
                    nbtTagCompound.getMethod("setDouble", String.class, double.class).invoke(tag, s, o);
                else if(UUID.class.isAssignableFrom(o.getClass()))
                    nbtTagCompound.getMethod("a", String.class, UUID.class).invoke(tag, s, o); //setUniqueId, it's first so it'll always be a()
                else if(String.class.isAssignableFrom(o.getClass()))
                    nbtTagCompound.getMethod("setString", String.class, String.class).invoke(tag, s, o);
                else if(byte[].class.isAssignableFrom(o.getClass()))
                    nbtTagCompound.getMethod("setByteArray", String.class, byte[].class).invoke(tag, s, o);
                else if(int[].class.isAssignableFrom(o.getClass()))
                    nbtTagCompound.getMethod("setIntArray", String.class, int[].class).invoke(tag, s, o);
                else if(long[].class.isAssignableFrom(o.getClass()))
                    nbtTagCompound.getMethod("a", String.class, long[].class).invoke(tag, s, o);
                else if(boolean.class.isAssignableFrom(o.getClass()))
                    nbtTagCompound.getMethod("setBoolean", String.class, boolean.class).invoke(tag, s, o);
            }
            return setTag(it, tag);
        } catch(Exception x) { x.printStackTrace(); }
        return it;
    }

    /**
     * Gets the NBT of a nucleus NBT entry, similar to a regular
     * entry but automatically prefixed with nbt_.
     */
    @Nullable
    public static String getNucleusNBT(ItemStack it, String key) {
        return (String) getNBTData(it, "nbt_"+key, "getString");
    }

    /**
     * Gets the NBT of a regular NBT entry.
     */
    @Nullable
    public static String getRegularNBT(ItemStack it, String rawName) {
        return (String) getNBTData(it, rawName, "getString");
    }

    /**
     * Gets the Nucleus ID of the itemstack, or null if none is found.
     * The Nucleus ID is the ID from the {@link Item#Item(String, Material)}
     * constructor.
     */
    @Nullable
    public static String getNucleusId(ItemStack it) {
        return (String) getNBTData(it, "nbt__id", "getString");
    }

    /**
     * Get the name to use to get this data's type.
     */
    private static String getMethod(Class<?> klass) {
        switch(klass.getName()) {
            case "NBTTagString": return "getString";
            case "NBTTagShort": return "getShort";
            case "NBTTagLongArray": return "getLongArray";
            case "NBTTagLong": return "getLong";
            case "NBTTagIntArray": return "getIntArray";
            case "NBTTagInt": return "getInt";
            case "NBTTagFloat": return "getFloat";
            case "NBTTagDouble": return "getDouble";
            case "NBTTagByteArray": return "getByteArray";
            case "NBTTagByte": return "getByte";
        }
        return "get";
    }

    /**
     * Gets NBT data from an item stack at a given key and parses
     * it into a given data type.
     */
    @Nullable
    public static <T> T getNBTData(ItemStack stack, Class<T> type, String key) {
        if(byte.class.isAssignableFrom(type))
            return (T) getNBTData(stack, key, "getByte");
        else if(short.class.isAssignableFrom(type))
            return (T) getNBTData(stack, key, "getShort");
        else if(int.class.isAssignableFrom(type))
            return (T) getNBTData(stack, key, "getInt");
        else if(long.class.isAssignableFrom(type))
            return (T) getNBTData(stack, key, "getLong");
        else if(float.class.isAssignableFrom(type))
            return (T) getNBTData(stack, key, "getFloat");
        else if(double.class.isAssignableFrom(type))
            return (T) getNBTData(stack, key, "getDouble");
        else if(UUID.class.isAssignableFrom(type))
            return (T) getNBTData(stack, key, "a"); //getUniqueId, it's first so it'll always be a()
        else if(String.class.isAssignableFrom(type))
            return (T) getNBTData(stack, key, "getString");
        else if(byte[].class.isAssignableFrom(type))
            return (T) getNBTData(stack, key, "getByteArray");
        else if(int[].class.isAssignableFrom(type))
            return (T) getNBTData(stack, key, "getIntArray");
        else if(long[].class.isAssignableFrom(type))
            return (T) getNBTData(stack, key, "getLongArray");
        else if(boolean.class.isAssignableFrom(type))
            return (T) getNBTData(stack, key, "getBoolean");

        return (T) getNBTData(stack, key, "get");
    }

    /**
     * Internal method for getting NBT data from an item stack.
     */
    private static Object getNBTData(ItemStack it, String key, String method) {
        Object nbt = null;
        try {
            Object tag = getTag(it);
            if((boolean) tag.getClass().getMethod("hasKey", String.class).invoke(tag, key)) {
                nbt = tag.getClass().getMethod(method, String.class).invoke(tag, key);
            }
        } catch(Exception x) { x.printStackTrace(); }
        return nbt;
    }

    private static ItemStack setTag(ItemStack it, Object tag) throws Exception {
        Object nullObject = null;
        Object nmsStack = craftItemStack.getMethod("asNMSCopy", ItemStack.class).invoke(null, it);
        if(!tag.toString().equalsIgnoreCase("{}")) nmsStack.getClass().getMethod("setTag", nbtTagCompound).invoke(nmsStack, tag);
        else nmsStack.getClass().getMethod("setTag", nbtTagCompound).invoke(nmsStack, nullObject);
        return (ItemStack) craftItemStack.getMethod("asCraftMirror", itemStack).invoke(null, nmsStack);
    }

    /**
     * Returns the NBTTagCompound of the item.
     */
    private static Object getTag(ItemStack item) throws Exception {
        Object nmsStack = craftItemStack.getMethod("asNMSCopy", ItemStack.class).invoke(null, item);
        Object tag = nbtTagCompound.newInstance();
        if((boolean) nmsStack.getClass().getMethod("hasTag").invoke(nmsStack)) tag = nmsStack.getClass().getMethod("getTag").invoke(nmsStack);
        return tag;
    }
}
