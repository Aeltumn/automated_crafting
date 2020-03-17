package nl.dgoossens.autocraft.helpers;

/**
 * A helper class to determine if we need to use legacy support features for 1.12 or can use the modern code.
 */
public enum MinecraftVersion {
    TWELVE,
    THIRTEEN,
    ;

    /**
     * For example:
     * MinecraftVersion.THIRTEEN.atLeast(MinecraftVersion.TWELVE);
     *      => true
     *
     * MinecraftVersion.THIRTEEN.atLeast(MinecraftVersion.FOURTEEN);
     *      => false
     */
    public boolean atLeast(MinecraftVersion mc) {
        return ordinal() >= mc.ordinal();
    }

    public static MinecraftVersion get() {
        final String ver = ReflectionHelper.getVersion();
        if(ver.startsWith("v1_12")) return MinecraftVersion.TWELVE;
        return MinecraftVersion.THIRTEEN; //Anything newer than 1.12 works the same
    }
}
