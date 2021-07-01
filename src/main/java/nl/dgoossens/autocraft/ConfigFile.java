package nl.dgoossens.autocraft;

import org.bukkit.Material;

public class ConfigFile {
    public static boolean allowDispensers() {
        return AutomatedCrafting.INSTANCE.getConfig().getBoolean("alternate-blocks.allowDispensers");
    }

    public static int ticksPerCraft() {
        return AutomatedCrafting.INSTANCE.getConfig().getInt("ticks-per-craft");
    }

    public static boolean isMaterialAllowed(Material material) {
        return !AutomatedCrafting.INSTANCE.getConfig().getStringList("blocked-materials").contains(material.name());
    }
}
