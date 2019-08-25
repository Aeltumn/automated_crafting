package nl.dgoossens.autocraft.helpers;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Copied from Nucleus at https://github.com/daniel-goossens/nucleus/blob/develop/src/main/java/nl/dgoossens/nucleus/utils/spigot/nms/ReflectionHelper.java
 * 25/08/2019 13:00
 */
public class ReflectionHelper {
    private static String version = null; //`v1_12_R1`
    private static String nms_package = ""; //`net.minecraft.server.v1_12_R1.`
    private static String cb_package = ""; //`org.bukkit.craftbukkit.v1_12_R1.`

    private static final String NMS_STRING = "net.minecraft.server.";
    private static final String CB_STRING = "org.bukkit.craftbukkit.";
    private static final String AUTH_STRING = "com.mojang.authlib.";

    /**
     * Loads the version this helper looks to to determine the class
     * locations of NMS classes from the NMS main package.
     */
    public static void loadVersion(final String full) {
        StringBuilder sb = new StringBuilder();
        int s = 0;
        if(full.startsWith(NMS_STRING)) s = NMS_STRING.length();
        if(full.startsWith(CB_STRING)) s = CB_STRING.length();
        if(s==0) throw new UnsupportedOperationException("Tried to load version of NMS/CB package without passing a usable package.");
        for(int c = s; c < full.length(); c++) {
            final char ch = full.charAt(c);
            if(ch=='.') break;
            sb.append(ch);
        }
        version = sb.toString();
        if(version.length()>0) {
            //This will not be true if there's a custom implementation where the version number is removed from the package.
            nms_package = NMS_STRING + version + ".";
            cb_package = CB_STRING + version + ".";
            return;
        }
        nms_package = NMS_STRING;
        cb_package = CB_STRING;
    }

    /**
     * Returns the minecraft version being used.
     */
    public static String getVersion() {
        if(version==null) loadVersion(Bukkit.getServer().getClass().getPackage().getName());
        return version;
    }

    /**
     * Gets NMS Class (eg. net.minecraft.server.v1.8.R3.ParticleEffect)
     * @param name eg. ParticleEffect
     * @return Class<?>
     */
    public static Class<?> getNMSClass(String name) {
        try {
            return Class.forName(nms_package + name);
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets NMS Class (eg. net.minecraft.server.v1.8.R3.ParticleEffect)
     * If the class doesn't exist the optional will be empty.
     */
    public static Optional<Class<?>> getOptionalNMSClass(String name) {
        try {
            return Optional.of(Class.forName(nms_package + name));
        } catch(ClassNotFoundException e) { return Optional.empty(); }
    }

    /**
     * Gets NMS Class (eg. net.minecraft.server.v1.8.R3.ParticleEffect)
     * @deprecated Use {@link #getOptionalNMSClass(String)} instead, it is better than a try/catch statement.
     */
    @Deprecated
    public static Class<?> getExceptionNMSClass(String name) throws Exception {
        return Class.forName(nms_package + name);
    }

    /**
     * Gets Autlib Class (eg. com.mojang.autlib.GameProfile)
     * @param name eg. GameProfile
     * @return Class<?>
     */
    public static Class<?> getAuthlibClass(String name) {
        try {
            return Class.forName(AUTH_STRING + name);
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets CraftBukkit Class (eg. org.bukkit.craftbukkit.v1.8.R3.CraftPlayer)
     * @param name eg. CraftPlayer
     * @return Class<?>
     */
    public static Class<?> getBukkitClass(String name) {
        try {
            return Class.forName(cb_package + name);
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Sends the actual packet to player, not version-dependent.
     * @param packet extends Packet
     * @param p PlayerConnection Target
     * @throws Exception Will probably never throw exception, otherwise blame Mojang!
     */
    public static void sendPacket(Object packet, Player p) throws Exception {
        if(p==null) return;
        Object entityPlayer = p.getClass().getMethod("getHandle").invoke(p);
        Object playerConnection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
        playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
    }

    /**
     * Gets a method in a certain class.
     * Gets the declared method so this will work with private methods.
     * (private's just a suggestion anyways, not?)
     */
    public static Method getMethod(Class<?> klass, String methodName, Class<?>... parameters) throws NoSuchMethodException {
        Method f = klass.getDeclaredMethod(methodName, parameters);
        f.setAccessible(true);
        return f;
    }

    /**
     * Gets a constructor in a ceratin class.
     * Gets the declared method so this will work with private constructor.
     * (private's just a suggestion anyways, not?)
     */
    public static <T> Constructor<T> getConstructor(Class<T> klass, Class<?>... parameters) throws NoSuchMethodException {
        Constructor<T> f = klass.getDeclaredConstructor(parameters);
        f.setAccessible(true);
        return f;
    }

    /**
     * Gets a field in a certain class from a given object instance.
     * This is handy when the object extends or implements the class in
     * which case {@link #getField(Object, String)} wouldn't work because the
     * object's class doesn't contain the field.
     */
    public static Object getField(Class<?> klass, Object object, String fieldName) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field f = klass.getDeclaredField(fieldName);
        f.setAccessible(true);
        return f.get(object);
    }

    /**
     * Gets a field from an object's class.
     */
    public static Object getField(Object object, String fieldName) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        if(object==null) return null;
        return getField(object.getClass(), object, fieldName);
    }

    /**
     * Sets a field in a certain class from a given object instance.
     * This is handy when the object extends or implements the class in
     * which case {@link #setField(Object, String, Object)} wouldn't work because the
     * object's class doesn't contain the field.
     */
    public static void setField(Class<?> klass, Object object, String fieldName, Object fieldValue) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field f = klass.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(object, fieldValue);
    }

    /**
     * Set a field's value in the object's class.
     */
    public static void setField(Object object, String fieldName, Object fieldValue) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        setField(object.getClass(), object, fieldName, fieldValue);
    }
}
