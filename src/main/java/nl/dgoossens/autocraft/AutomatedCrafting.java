package nl.dgoossens.autocraft;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class AutomatedCrafting extends JavaPlugin {
    private DropperRegistry registry;

    public void onEnable() {
        registry = new DropperRegistry(this);
        Bukkit.getPluginManager().registerEvents(new CreationListener(this), this);
    }

    public DropperRegistry getDropperRegistry() { return registry; }
}
