package nl.dgoossens.autocraft.api;

import nl.dgoossens.autocraft.AutomatedCrafting;
import nl.dgoossens.autocraft.RecipeLoader;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class CrafterRegistry {
    private ConcurrentHashMap<String, ConcurrentHashMap<BlockPos, ItemStack>> crafters = new ConcurrentHashMap<>();
    protected final RecipeLoader recipeLoader;
    protected final File file;

    public CrafterRegistry() {
        recipeLoader = AutomatedCrafting.INSTANCE.getRecipeLoader();
        file = new File(AutomatedCrafting.INSTANCE.getDataFolder(), "autocrafters.json");

        load();
    }

    /**
     * Get the list of worlds for which we have an autocrafter map.
     */
    protected Set<String> getWorldsRegistered() {
        return crafters.keySet().parallelStream().filter(f -> !crafters.getOrDefault(f, new ConcurrentHashMap<>()).isEmpty()).collect(Collectors.toSet());
    }

    /**
     * Get a map of all autocrafters that exist in the world.
     */
    public Map<BlockPos, ItemStack> getAutocrafterMap(String world) {
        return crafters.computeIfAbsent(world, (w) -> new ConcurrentHashMap<>());
    }

    /**
     * Get a map of all autocrafters that exist in the world.
     */
    public Map<BlockPos, ItemStack> getAutocrafterMap(World world) {
        return crafters.computeIfAbsent(world.getName(), (w) -> new ConcurrentHashMap<>());
    }

    /**
     * Checks the validity of the item in the item frame, notifies the player in chat.
     */
    public abstract void checkBlock(final Location location, final Player player);

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
     * Loads all autocrafters from the saved configuration file.
     */
    public abstract void load();

    /**
     * Saves all autocrafters to the configuration.
     */
    public abstract void save();
}
