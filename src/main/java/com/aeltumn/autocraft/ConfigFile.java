package com.aeltumn.autocraft;

import org.bukkit.Material;

public class ConfigFile {
    public static boolean allowDispensers() {
        return AutomatedCrafting.INSTANCE.getConfig().getBoolean("alternate-blocks.allowDispensers");
    }

    public static boolean allowChests() {
        return AutomatedCrafting.INSTANCE.getConfig().getBoolean("alternate-blocks.allowChests");
    }

    public static int ticksPerCraft() {
        return AutomatedCrafting.INSTANCE.getConfig().getInt("ticks-per-craft");
    }

    public static boolean craftOnRedstonePulse() {
        return AutomatedCrafting.INSTANCE.getConfig().getBoolean("craft-on-redstone-pulse");
    }

    public static boolean isMaterialAllowed(Material material) {
        return !AutomatedCrafting.INSTANCE.getConfig().getStringList("blocked-materials").contains(material.name());
    }
}
