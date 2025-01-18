package com.aeltumn.autocraft.impl;

import com.aeltumn.autocraft.AutomatedCrafting;
import com.aeltumn.autocraft.ConfigFile;
import com.aeltumn.autocraft.api.Autocrafter;
import com.aeltumn.autocraft.api.AutocrafterPositions;
import com.aeltumn.autocraft.api.BlockPos;
import com.aeltumn.autocraft.api.ChunkIdentifier;
import com.aeltumn.autocraft.api.CrafterRegistry;
import com.aeltumn.autocraft.api.CraftingRecipe;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class CrafterRegistryImpl extends CrafterRegistry {
    public static final int VERSION = 3;
    private final BukkitTask mainTick;

    public CrafterRegistryImpl(JavaPlugin jp) {
        super();

        if (!ConfigFile.craftOnRedstonePulse()) {
            var speed = ConfigFile.ticksPerCraft();
            mainTick = new MainCrafterTick(this).runTaskTimer(jp, speed, speed);
        } else {
            mainTick = null;
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();

        if (mainTick != null) {
            mainTick.cancel();
        }
    }

    @Override
    public boolean isAutocrafter(Block block) {
        var bp = new BlockPos(block);
        return getAutocrafters(block.getWorld()).map(f -> f.get(bp) != null).orElse(false);
    }

    @Override
    public void tick(Block block) {
        var bp = new BlockPos(block);
        var crafter = getAutocrafters(block.getWorld()).map(f -> f.get(bp)).orElse(null);
        if (crafter == null) return;
        crafter.tick(block.getChunk());
    }

    @Override
    public boolean checkBlock(final Location location, final Player player) {
        final Block block = location.getBlock();
        final BlockPos pos = new BlockPos(block);
        final Autocrafter m = getAutocrafters(location.getWorld()).map(f -> f.get(pos)).orElse(null);
        if ((!(block.getState() instanceof Container)))
            return false;

        final Container container = (Container) block.getState();
        if (m == null) return false;
        if (container.isLocked() || block.getBlockPower() > 0) {
            if (container.isLocked())
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, getText("Autocrafter is locked"));
            else
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, getText("Autocrafter has redstone signal blocking it"));
            return true;
        }

        if (!ConfigFile.isMaterialAllowed(m.getItem(block.getChunk()).getType())) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, getText("Crafting this item is disabled"));
            return true;
        }

        final Set<CraftingRecipe> recipes = recipeLoader.getRecipesFor(m.getItem(block.getChunk()));
        if (recipes == null || recipes.size() == 0) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, getText("Autocrafter can't craft this item"));
            return false;
        }

        //Inform the player how many recipes are being accepted right now
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, getText("Autocrafter is accepting " + recipes.size() + " recipe(s)"));
        return true;
    }

    private BaseComponent[] getText(final String text) {
        return TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', text));
    }

    @Override
    public boolean create(final Location l, final Player p, final ItemStack type) {
        if (!p.hasPermission("automatedcrafting.makeautocrafters") || type == null)
            return false;
        BlockPos el = new BlockPos(l.getBlockX(), l.getBlockY(), l.getBlockZ());
        AutocrafterPositions am = getOrCreateAutocrafters(l.getWorld());
        am.destroy(el); //Destroy old ones
        am.add(el, type); //Add the new one
        markDirty();
        return true;
    }

    @Override
    public void destroy(final Location l) {
        BlockPos el = new BlockPos(l.getBlockX(), l.getBlockY(), l.getBlockZ());
        Optional<AutocrafterPositions> m = getAutocrafters(l.getWorld());
        m.ifPresent(am -> am.destroy(el));
        markDirty();
    }

    @Override
    public void load() {
        if (!AutomatedCrafting.INSTANCE.getDataFolder().exists()) AutomatedCrafting.INSTANCE.getDataFolder().mkdirs();
        File legacyFile = new File(AutomatedCrafting.INSTANCE.getDataFolder(), "droppers.json");
        boolean legacyLoaded = false;

        //Reset stored crafter information
        crafters = new ConcurrentHashMap<>();

        //Legacyload
        if (legacyFile.exists()) {
            AutomatedCrafting.INSTANCE.warning("Found a legacy configuration file `droppers.json`. Please run an older version of Automated Crafting v2.6.2 or older to load from this file.");
        }

        //Load modern file
        boolean converted = false;
        if (file.exists()) {
            try {
                FileReader fr = new FileReader(file);
                JsonReader jr = new JsonReader(fr);
                if (!jr.hasNext()) return;
                if (jr.peek() != JsonToken.BEGIN_OBJECT) return;
                jr.beginObject();
                jr.nextName();
                int version = jr.nextInt();
                while (jr.hasNext()) {
                    String world = jr.nextName();
                    AutocrafterPositions m = new AutocrafterPositions(world);
                    jr.beginObject();
                    while (jr.hasNext()) {
                        String n = jr.nextName(); //Chunk identifier code
                        ChunkIdentifier ci;
                        try {
                            ci = new ChunkIdentifier(Long.parseLong(n));
                        } catch (NumberFormatException ignored) {
                            //Skip through the data for this chunk
                            jr.beginObject();
                            while (jr.hasNext()) {
                                jr.nextName();
                                jr.skipValue();
                            }
                            jr.endObject();
                            continue;
                        }
                        if (version < 3) {
                            jr.beginObject();
                            while (jr.hasNext()) {
                                String n2 = jr.nextName(); //Chunk identifier code
                                jr.skipValue();

                                long l;
                                try {
                                    l = Long.parseLong(n2);
                                } catch (NumberFormatException ignored) {
                                    continue;
                                }
                                //Update method of saving items to json
                                m.add(ci, l);
                            }
                            jr.endObject();
                        } else {
                            jr.beginArray();
                            while (jr.hasNext()) {
                                m.add(ci, jr.nextLong());
                            }
                            jr.endArray();
                        }
                    }
                    jr.endObject();

                    //If this world has data we add it to the full list
                    if (!m.isEmpty())
                        crafters.put(world, m);
                }
                jr.endObject();
                jr.close();
                fr.close();
            } catch (Exception x) {
                x.printStackTrace();
                AutomatedCrafting.INSTANCE.warning("An error occurred whilst reading autocrafters from the configuration file. Please rebuild all autocrafters!");
            }
        }

        if (legacyLoaded || converted) {
            forceSave();
        }
    }

    @Override
    public void forceSave() {
        saveTime = Long.MAX_VALUE;

        try {
            if (!file.exists()) file.createNewFile();
            FileWriter fw = new FileWriter(file);
            JsonWriter jw = new JsonWriter(fw);
            jw.setIndent("  ");
            jw.beginObject();
            jw.name("version");
            jw.value(VERSION);

            for (String s : getWorldsRegistered()) {
                Optional<AutocrafterPositions> m = getAutocrafters(s);
                if (!m.isPresent()) continue;
                if (m.get().isEmpty()) continue;

                jw.name(s);
                jw.beginObject();
                for (ChunkIdentifier ci : m.get().listChunks()) {
                    jw.name(String.valueOf(ci.toLong()));
                    ArrayList<Autocrafter> positions = m.get().getInChunk(ci);
                    jw.beginArray();
                    for (Autocrafter a : positions) {
                        if (a.isBroken()) continue; //Don't save broken ones.
                        jw.value(a.getPositionAsLong());
                    }
                    jw.endArray();
                }
                jw.endObject();
            }
            jw.endObject();
            jw.flush();
            jw.close();
            fw.close();
        } catch (Exception x) {
            x.printStackTrace();
        }

        saveTime = Long.MAX_VALUE; //Save again at the end of time.
    }
}
