package nl.dgoossens.autocraft.helpers;

public enum MinecraftVersion {
    TWELVE,
    THIRTEEN,
    FOURTEEN,
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
        if(ver.startsWith("v1_13")) return MinecraftVersion.THIRTEEN;
        return MinecraftVersion.FOURTEEN; //Default to 1.14 so there's support for future versions. (1.8 will completely break anyways)
    }
}
