package nl.dgoossens.autocraft.helpers;

//A helper class to determine the MinecraftVersion because we need some backwards support for 1.12.
//Do note that most of the features for 1.14+ use try/catch statements that fail if the classes don't exist, why you ask? I'm lazy and bukkit introduced some features halfway through 1.13/1.14 so you can have a MinecraftVersion of FOURTEEN and not have those classes either.
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
