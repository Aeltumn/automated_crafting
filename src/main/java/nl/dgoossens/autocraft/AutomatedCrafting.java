package nl.dgoossens.autocraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import nl.dgoossens.autocraft.helpers.ReflectionHelper;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;

public class AutomatedCrafting extends JavaPlugin {
    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    public static final Gson GSON_ITEM = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
    public static final JsonParser JSON_PARSER = new JsonParser();

    /**
     * Returns the instance of this plugin, will be null if the plugin hasn't been loaded
     * yet.
     */
    @Nullable
    public static AutomatedCrafting getInstance() {
        return (AutomatedCrafting) Bukkit.getPluginManager().getPlugin("AutomatedCrafting");
    }

    @Override
    public void onEnable() {
        //A little reward for those special snowflakes and reposting websites that don't bother to include the supported versions.
        if(ReflectionHelper.getVersion().startsWith("v1_8")) {
            getLogger().severe("My plugins don't support 1.8 and never will, update to 1.12+ or find another plugin.");
            getPluginLoader().disablePlugin(this);
            Bukkit.shutdown();
            return;
        }

        recipeLoader = new RecipeLoader(this); //The recipe loader keeps track of all the recipes the autocrafters support.
        registry = new DropperRegistry(this); //The dropper regstiry tracks all autocrafters and ticks them to craft every second.
        Bukkit.getPluginManager().registerEvents(new CreationListener(this), this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("reloadrecipes"))
            new BukkitRunnable() {
                public void run() {
                    getRecipeLoader().reload(null);
                }
            }.runTaskAsynchronously(this); //Run async for better performance!
        return false;
    }

    private DropperRegistry registry;
    public DropperRegistry getDropperRegistry() { return registry; }

    private RecipeLoader recipeLoader;
    public RecipeLoader getRecipeLoader() { return recipeLoader; }
}
