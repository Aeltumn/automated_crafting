package nl.dgoossens.autocraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import nl.dgoossens.autocraft.api.CrafterRegistry;
import nl.dgoossens.autocraft.helpers.ReflectionHelper;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class AutomatedCrafting extends JavaPlugin {
    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    public static AutomatedCrafting INSTANCE;
    private CrafterRegistry registry;
    private RecipeLoader recipeLoader;

    /**
     * Returns the instance of this plugin, will be null if the plugin hasn't been loaded
     * yet.
     */
    @Deprecated
    public static AutomatedCrafting getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

        //A little reward for the folks that don't bother to include the supported versions.
        if (ReflectionHelper.getVersion().startsWith("v1_8")) {
            getLogger().severe("This plugin doesn't support 1.8 and never will, update to 1.12+ or find another plugin.");
            getPluginLoader().disablePlugin(this);
            Bukkit.shutdown();
            return;
        }

        //Setup config
        saveDefaultConfig();
        reloadConfig();

        recipeLoader = new RecipeLoader(this); //The recipe loader keeps track of all the recipes the autocrafters support.
        registry = new CrafterRegistryImpl(this); //The registry tracks all autocrafters and ticks them to craft every second.
        Bukkit.getPluginManager().registerEvents(new CreationListener(), this);
    }

    @Override
    public void onDisable() {
        // Save the crafter registry if it's dirty
        if (registry.isDirty()) {
            registry.forceSave();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("reloadrecipes"))
            getRecipeLoader().reload(sender);
        return false;
    }


    public CrafterRegistry getCrafterRegistry() {
        return registry;
    }

    public RecipeLoader getRecipeLoader() {
        return recipeLoader;
    }

    public void info(String text) {
        getLogger().info(text);
    }

    public void warning(String text) {
        getLogger().warning(text);
    }
}
