package nl.dgoossens.autocraft.helpers;

import nl.dgoossens.autocraft.AutomatedCrafting;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public class SerializedItem {
    public static final Class<?> mojangsonParser = ReflectionHelper.getNMSClass("MojangsonParser");
    public static final Class<?> craftItemStack = ReflectionHelper.getBukkitClass("inventory.CraftItemStack");
    public static final Class<?> nbtTagCompound = ReflectionHelper.getNMSClass("NBTTagCompound");
    public static final Class<?> itemStack = ReflectionHelper.getNMSClass("ItemStack");

    private Map<String, Object> item;
    private Map<String, Object> meta;
    private String nbt;

    public SerializedItem(ItemStack item) { build(item); }
    public ItemStack getItem() {
        if(this.item==null) return null;
        ItemStack ret = ItemStack.deserialize(this.item);
        if(meta!=null) ret.setItemMeta((ItemMeta) ConfigurationSerialization.deserializeObject(meta, ConfigurationSerialization.getClassByAlias("ItemMeta")));
        try {
            Object tag = mojangsonParser.getMethod("parse", String.class).invoke(null, nbt);
            Object nullObject = null;
            Object nmsStack = craftItemStack.getMethod("asNMSCopy", ItemStack.class).invoke(null, ret);
            if(!tag.toString().equalsIgnoreCase("{}")) nmsStack.getClass().getMethod("setTag", nbtTagCompound).invoke(nmsStack, tag);
            else nmsStack.getClass().getMethod("setTag", nbtTagCompound).invoke(nmsStack, nullObject);
            ret = (ItemStack) craftItemStack.getMethod("asCraftMirror", itemStack).invoke(null, nmsStack);
        } catch(Exception x) { x.printStackTrace(); }
        return ret;
    }
    private void build(ItemStack item) {
        if(item==null) return;
        if(item.hasItemMeta()) meta = item.getItemMeta().serialize();
        ItemStack copy = item.clone();
        copy.setItemMeta(null);
        this.item = copy.serialize();
        try {
            Object nmsStack = craftItemStack.getMethod("asNMSCopy", ItemStack.class).invoke(null, item);
            Object tag = nbtTagCompound.newInstance();
            if((boolean) nmsStack.getClass().getMethod("hasTag").invoke(nmsStack)) tag = nmsStack.getClass().getMethod("getTag").invoke(nmsStack);
            nbt = tag.toString();
        } catch(Exception x) { x.printStackTrace(); }
    }


    @Override
    public String toString() {
        return AutomatedCrafting.GSON.toJson(this);
    }
}
