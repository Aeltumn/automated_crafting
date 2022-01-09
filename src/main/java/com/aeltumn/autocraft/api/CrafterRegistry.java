package com.aeltumn.autocraft.api;

import com.aeltumn.autocraft.AutomatedCrafting;
import com.aeltumn.autocraft.RecipeLoader;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the existance of all autocrafters.
 */
public abstract class CrafterRegistry {
    private static final long SAVE_DELAY = (150) * 1000; //Wait 2.5 minutes = 150 seconds
    protected final RecipeLoader recipeLoader;
    protected final File file;
    private final BukkitTask saveTask;
    protected ConcurrentHashMap<String, AutocrafterPositions> crafters = new ConcurrentHashMap<>();
    protected long saveTime = Long.MAX_VALUE;

    public CrafterRegistry() {
        recipeLoader = AutomatedCrafting.INSTANCE.getRecipeLoader();
        file = new File(AutomatedCrafting.INSTANCE.getDataFolder(), "autocrafters.json");

        load();

        // periodically try to save if the data is marked as dirty
        saveTask = Bukkit.getScheduler().runTaskTimer(AutomatedCrafting.INSTANCE, () -> {
            if (System.currentTimeMillis() > saveTime) {
                forceSave();
            }
        }, 40, 40);
    }

    /**
     * Shuts down the crafter registry and ends any pending tasks.
     */
    public void shutdown() {
        saveTask.cancel();
    }

    /**
     * Returns whether any crafter caused the registry to be marked as dirty.
     */
    public boolean isDirty() {
        return saveTime != Long.MAX_VALUE;
    }

    /**
     * Get the list of worlds for which we have an autocrafter map.
     */
    public Set<String> getWorldsRegistered() {
        return crafters.keySet();
    }

    /**
     * Get a map of all autocrafters that exist in the world.
     */
    public Optional<AutocrafterPositions> getAutocrafters(String world) {
        return Optional.ofNullable(crafters.get(world));
    }

    /**
     * Get a map of all autocrafters that exist in the world.
     */
    public Optional<AutocrafterPositions> getAutocrafters(World world) {
        return Optional.ofNullable(crafters.get(world.getName()));
    }

    /**
     * Get a map of all autocrafters in the world.
     * Create one if none exist.
     */
    public AutocrafterPositions getOrCreateAutocrafters(World world) {
        return crafters.computeIfAbsent(world.getName(), (w) -> new AutocrafterPositions());
    }

    /**
     * Checks the validity of the item in the item frame, notifies the player in chat.
     */
    public abstract boolean checkBlock(final Location location, final Player player);

    /**
     * Creates a new autocrafter at a given location with
     * a given type that the autocrafter crafts.
     * This will overwrite an existing autocrafter at the location.
     *
     * @return Was the creation successful?
     */
    public abstract boolean create(final Location l, final Player p, final ItemStack type);

    /**
     * Destroys the auto crafter at a given location.
     */
    public abstract void destroy(final Location l);

    /**
     * Ticks the auto crafter on the given block.
     */
    public abstract void tick(final Block block);

    /**
     * Returns whether there is an autocrafter at the given block.
     */
    public abstract boolean isAutocrafter(final Block block);

    /**
     * Loads all autocrafters from the saved configuration file.
     */
    public abstract void load();

    /**
     * Saves all autocrafters to the configuration.
     * Don't use this method!
     */
    public abstract void forceSave();

    /**
     * After marking the crafter registry dirty it will be saved soon.
     */
    public void markDirty() {
        //We save either when we previously wanted to save or in five minutes.
        saveTime = Math.min(saveTime, System.currentTimeMillis() + SAVE_DELAY);
    }
}
