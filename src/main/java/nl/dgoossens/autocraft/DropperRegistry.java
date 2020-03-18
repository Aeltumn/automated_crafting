package nl.dgoossens.autocraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import nl.dgoossens.autocraft.api.CrafterRegistry;
import nl.dgoossens.autocraft.api.CraftingRecipe;
import nl.dgoossens.autocraft.events.AutocrafterCreateEvent;
import nl.dgoossens.autocraft.events.AutocrafterDestroyEvent;
import nl.dgoossens.autocraft.api.BlockPos;
import nl.dgoossens.autocraft.helpers.ReflectionHelper;
import nl.dgoossens.autocraft.helpers.SerializedItem;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

public class DropperRegistry extends CrafterRegistry {
    public static final int VERSION = 1;
    public DropperRegistry(JavaPlugin jp) {
        //TODO new MainDropperTick(this, recipeLoader).runTaskTimer(jp, 27, 27);
    }

    @Override
    public void checkBlock(final Location location, final Player player) {
        final Block block = location.getBlock();
        final BlockPos pos = new BlockPos(block);
        final ItemStack m = getAutocrafterMap(location.getWorld()).entrySet().parallelStream().filter(f -> f.getKey().equals(pos)).findAny().map(Map.Entry::getValue).orElse(null);
        if ((!(block.getState() instanceof Container)))
            return;

        final Container container = (Container) block.getState();
        if (m == null) return;
        if (container.isLocked() || block.getBlockPower() > 0) {
            if (container.isLocked()) player.spigot().sendMessage(ChatMessageType.ACTION_BAR, getText("&7Autocrafter is locked"));
            else player.spigot().sendMessage(ChatMessageType.ACTION_BAR, getText("&7Autocrafter has redstone signal blocking it"));
            return;
        }
        final Set<CraftingRecipe> recipes = recipeLoader.getRecipesFor(m);
        if (recipes == null || recipes.size() == 0) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, getText("&7Autocrafter can't craft this item"));
            return;
        }

        //Inform the player how many recipes are being accepted right now
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, getText("&7Autocrafter is accepting "+recipes.size()+" recipe(s)"));
    }

    private BaseComponent[] getText(final String text) {
        return TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', text));
    }

    @Override
    public boolean create(final Location l, final Player p, final ItemStack type) {
        AutocrafterCreateEvent e = new AutocrafterCreateEvent(l, p, type);
        Bukkit.getPluginManager().callEvent(e);
        if(!p.hasPermission("automatedcrafting.makeautocrafters") || e.isCancelled()) {
            return false;
        }
        BlockPos el = new BlockPos(l.getBlockX(), l.getBlockY(), l.getBlockZ());
        getAutocrafterMap(l.getWorld()).keySet().removeIf(f -> f.equals(el));
        if(type!=null)
            getAutocrafterMap(l.getWorld()).put(el, type);
        save();
        return true;
    }

    @Override
    public void destroy(final Location l) {
        BlockPos el = new BlockPos(l.getBlockX(), l.getBlockY(), l.getBlockZ());
        getAutocrafterMap(l.getWorld()).keySet().removeIf(el::equals);
        for (BlockPos p : new HashSet<>(getAutocrafterMap(l.getWorld()).keySet())) {
            if (el.equals(p)) {
                AutocrafterDestroyEvent e = new AutocrafterDestroyEvent(l, getAutocrafterMap(l.getWorld()).get(p));
                Bukkit.getPluginManager().callEvent(e);
                getAutocrafterMap(l.getWorld()).remove(p);
            }
        }
        save();
    }

    @Override
    public void load() {
        if (!AutomatedCrafting.INSTANCE.getDataFolder().exists()) AutomatedCrafting.INSTANCE.getDataFolder().mkdirs();
        File legacyFile = new File(AutomatedCrafting.INSTANCE.getDataFolder(), "droppers.json");
        //Legacyload
        if(legacyFile.exists()) {
            try {
                FileReader fr = new FileReader(legacyFile);
                JsonReader jr = new JsonReader(fr);
                jr.beginObject();
                Gson g = new GsonBuilder().create();
                while (jr.hasNext()) {
                    String n = jr.nextName();
                    LegacyBlockPos lbp = g.fromJson(jr, LegacyBlockPos.class);
                    ItemStack it = AutomatedCrafting.GSON.fromJson(n, LegacySerializedItem.class).getItem();
                    getAutocrafterMap(lbp.world).put(new BlockPos(lbp.x, lbp.y, lbp.z), it);
                }
                jr.endObject();
                jr.close();
                fr.close();
            } catch (Exception x) {
                AutomatedCrafting.INSTANCE.warning("An error occurred whilst legacy loading autocrafters from an old configuration file. Please rebuild all autocrafters!");
            }

            legacyFile.delete(); //Remove old file
        }

        //Load modern file
        if (file.exists()) {
            try {
                FileReader fr = new FileReader(file);
                JsonReader jr = new JsonReader(fr);
                if (!jr.hasNext()) return;
                if (jr.peek() != JsonToken.BEGIN_OBJECT) return;
                jr.beginObject();
                String s = jr.nextName();
                int version = jr.nextInt();
                if(version != VERSION) {
                    //TODO Add compatibility for older versions of configuration when new configuration versions are added!
                    AutomatedCrafting.INSTANCE.warning("You were running an old version of AutomatedCrafting (file version "+VERSION+") and all exsisting autocrafters have been invalidated, sorry! (every autocrafter will need to be rebuilt)");
                    return;
                }
                Gson g = new GsonBuilder().create();
                while (jr.hasNext()) {
                    String world = jr.nextName();
                    Map<BlockPos, ItemStack> m = getAutocrafterMap(world);
                    jr.beginObject();
                    while(jr.hasNext()) {
                        String n = jr.nextName();
                        //Update method of saving items to json
                        m.put(BlockPos.fromLong(jr.nextLong()), AutomatedCrafting.GSON.fromJson(n, SerializedItem.class).getItem());
                    }
                    jr.endObject();
                }
                jr.endObject();
                jr.close();
                fr.close();
            } catch (Exception x) {
                AutomatedCrafting.INSTANCE.warning("An error occurred whilst reading autocrafters from the configuration file. Please rebuild all autocrafters!");
            }
        }
    }

    @Override
    public void save() {
        try {
            if (!file.exists()) file.createNewFile();
            FileWriter fw = new FileWriter(file);
            JsonWriter jw = new JsonWriter(fw);
            jw.beginObject();
            jw.name("version");
            jw.value(VERSION);

            for(String s : getWorldsRegistered()) {
                jw.name(s);
                jw.beginObject();
                Map<BlockPos, ItemStack> m = getAutocrafterMap(s);
                for (BlockPos d : m.keySet()) {
                    if (m.get(d) == null) continue;
                    jw.name(new SerializedItem(m.get(d)).toString());
                    jw.value(d.toLong());
                }
                jw.endObject();
            }
            jw.endObject();
            jw.flush();
            jw.close();
            fw.close();
        } catch(Exception x) { x.printStackTrace(); }
    }

    //Used for legacy loading of old data files.
    public static class LegacyBlockPos {
        private int x, y, z;
        private String world;
    }

    //Used for legacy loading of old data files.
    public static class LegacySerializedItem {
        private static final Class<?> mojangsonParser = ReflectionHelper.getNMSClass("MojangsonParser");
        private static final Class<?> craftItemStack = ReflectionHelper.getBukkitClass("inventory.CraftItemStack");
        private static final Class<?> nbtTagCompound = ReflectionHelper.getNMSClass("NBTTagCompound");
        private static final Class<?> itemStack = ReflectionHelper.getNMSClass("ItemStack");

        private Map<String, Object> item;
        private Map<String, Object> meta;
        private String nbt;

        public LegacySerializedItem(ItemStack item) {
            build(item);
        }

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
    }
}
