package nl.dgoossens.autocraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import nl.dgoossens.autocraft.events.AutoPreCraftItemEvent;
import nl.dgoossens.autocraft.events.AutocrafterCreateEvent;
import nl.dgoossens.autocraft.events.AutocrafterDestroyEvent;
import nl.dgoossens.autocraft.helpers.BlockPos;
import nl.dgoossens.autocraft.helpers.Recipe;
import nl.dgoossens.autocraft.helpers.SerializedItem;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Dropper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DropperRegistry {
    protected ConcurrentHashMap<BlockPos, ItemStack> droppers = new ConcurrentHashMap<>();
    private File file;
    private final RecipeLoader recipeLoader;

    /**
     * Checks the validity of the item in the item frame, notifies the player in chat.
     */
    public void checkDropper(final Location location, final Player player) {
        final Block block = location.getBlock();
        final BlockPos pos = new BlockPos(block);
        if(!(block.getState() instanceof Dropper)) return; //Not a dropper, no message. This is probably an edge-case or an error.
        final ItemStack m = droppers.entrySet().parallelStream().filter(f -> f.getKey().equals(pos)).findAny().map(Map.Entry::getValue).orElse(null);
        final Dropper dropper = (Dropper) block.getState();
        if(m==null) return;
        if(dropper.isLocked() || block.getBlockPower()>0) {
            if(dropper.isLocked()) player.spigot().sendMessage(ChatMessageType.ACTION_BAR, getText("&7Autocrafter is locked"));
            else player.spigot().sendMessage(ChatMessageType.ACTION_BAR, getText("&7Autocrafter has redstone signal blocking it"));
            return;
        }
        final Set<Recipe> recipes = recipeLoader.getRecipesFor(m);
        if(recipes==null || recipes.size()==0) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, getText("&7Autocrafter can't craft this item"));
            return;
        }

        int size = recipes.size();
        recipes.removeIf(f -> {
            if(f==null) return true;
            AutoPreCraftItemEvent event = new AutoPreCraftItemEvent(f, block, m);
            Bukkit.getPluginManager().callEvent(event);
            return event.isCancelled();
        });

        if(recipes.size()==0) { //If all recipes are blocked we can assume this item is being blocked.
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, getText("&7Crafting this item has been disabled"));
            return;
        }

        //No recipes got removed
        if(recipes.size()==size) player.spigot().sendMessage(ChatMessageType.ACTION_BAR, getText("&7Autocrafter is accepting "+recipes.size()+" recipe(s)"));
        else player.spigot().sendMessage(ChatMessageType.ACTION_BAR, getText("&7Autocrafter is accepting "+recipes.size()+" recipe(s), "+(size-recipes.size())+" were disabled")); //also tell how many of the recipes got blocked
    }

    /**
     * Parses and colours a piece of text.
     */
    private BaseComponent[] getText(final String text) {
        return TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', text));
    }

    public DropperRegistry(final AutomatedCrafting instance) {
        load(instance);
        recipeLoader = instance.getRecipeLoader();

        new MainDropperTick(this, recipeLoader).runTaskTimer(instance, 27, 27);
    }

    /**
     * Creates a new dropper at a given location with
     * a given type that the dropper crafts.
     * This will overwrite an existing dropper at the location.
     * @return Was the creation successfull?
     */
    public boolean create(final Location l, final Player p, final ItemStack type) {
        AutocrafterCreateEvent e = new AutocrafterCreateEvent(l, p, type);
        Bukkit.getPluginManager().callEvent(e);
        if(!p.hasPermission("automatedcrafting.makeautocrafters") || e.isCancelled()) {
            return false;
        }
        BlockPos el = new BlockPos(l.getBlock());
        droppers.keySet().removeIf(f -> f.equals(el));
        if(type!=null) droppers.put(el, type);
        save();
        return true;
    }

    /**
     * Destroys the dropper at a given location.
     */
    public void destroy(final Location l) {
        BlockPos el = new BlockPos(l.getBlock());
        droppers.keySet().removeIf(el::equals);
        for(BlockPos p : new HashSet<>(droppers.keySet())) {
            if(el.equals(p)) {
                AutocrafterDestroyEvent e = new AutocrafterDestroyEvent(l, droppers.get(p));
                Bukkit.getPluginManager().callEvent(e);
                droppers.remove(p);
            }
        }
        save();
    }

    /**
     * Loads all autocrafters from the saved configuration file.
     */
    private void load(final AutomatedCrafting instance) {
        if(!instance.getDataFolder().exists()) instance.getDataFolder().mkdirs();
        file = new File(instance.getDataFolder(), "droppers.json");
        if(file.exists()) {
            try {
                FileReader fr = new FileReader(file);
                JsonReader jr = new JsonReader(fr);
                if(!jr.hasNext()) return;
                if(jr.peek()!=JsonToken.BEGIN_OBJECT) return;
                jr.beginObject();
                Gson g = new GsonBuilder().create();
                while(jr.hasNext()) {
                    String n = jr.nextName();
                    droppers.put(g.fromJson(jr, BlockPos.class), AutomatedCrafting.GSON.fromJson(n, SerializedItem.class).getItem());
                }
                jr.endObject();
                jr.close();
                fr.close();
            } catch(Exception x) {
                instance.getLogger().warning("An error occurred whilst reading autocrafters from the configuration file. Please rebuild all autocrafters!");
            }
        }
    }

    /**
     * Saves all autocrafters to the configuration.
     */
    protected void save() {
        try {
            if(!file.exists()) file.createNewFile();
            FileWriter fw = new FileWriter(file);
            JsonWriter jw = new JsonWriter(fw);
            jw.beginObject();
            for(BlockPos d : droppers.keySet()) { jw.name(droppers.get(d)==null ? "null" : new SerializedItem(droppers.get(d)).toString()); jw.jsonValue(new GsonBuilder().create().toJson(d)); }
            jw.endObject();
            jw.flush();
            jw.close();
            fw.close();
        } catch(Exception x) { x.printStackTrace(); }
    }
}
