package com.aeltumn.autocraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.aeltumn.autocraft.api.CrafterRegistry;
import com.aeltumn.autocraft.impl.CrafterRegistryImpl;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

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

        registry.shutdown();
        INSTANCE = null;
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
