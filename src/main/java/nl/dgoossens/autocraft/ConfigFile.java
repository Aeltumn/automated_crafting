package nl.dgoossens.autocraft;

public class ConfigFile {
    public static boolean allowDispensers() {
        return AutomatedCrafting.INSTANCE.getConfig().getBoolean("alternate-blocks.allowDispensers");
    }

    public static boolean allowHoppers() {
        return AutomatedCrafting.INSTANCE.getConfig().getBoolean("alternate-blocks.allowHoppers");
    }
}
