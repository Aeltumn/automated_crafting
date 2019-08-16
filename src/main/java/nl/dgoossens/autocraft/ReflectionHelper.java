package nl.dgoossens.autocraft;

import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Copied from Nucleus at https://github.com/daniel-goossens/nucleus/blob/develop/src/main/java/nl/dgoossens/nucleus/utils/spigot/nms/ReflectionHelper.java
 * 16/08/2019 15:00
 */
public class ReflectionHelper {
    private static String version = null;

    /**
     * Returns the minecraft version being used.
     */
    public static String getVersion() { return version; }

    /**
     * Gets NMS Class (eq. net.minecraft.server.v1.8.R3.ParticleEffect)
     * @param name eq. ParticleEffect
     * @return Class<?>
     */
    public static Class<?> getNMSClass(String name) {
        if(version==null) {
            try {
                version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            } catch(Exception x) { version = ""; }
        }
        if(version.equals("")) {
            try {
                return Class.forName("net.minecraft.server." + name);
            } catch(ClassNotFoundException e2) {
                e2.printStackTrace();
                return null;
            }
        }
        try {
            return Class.forName("net.minecraft.server." + version + "." + name);
        } catch(ClassNotFoundException e) {
            try {
                return Class.forName("net.minecraft.server." + name);
            } catch(ClassNotFoundException e2) {
                e2.printStackTrace();
                return null;
            }
        }
    }

    /**
     * Gets NMS Class (eq. net.minecraft.server.v1.8.R3.ParticleEffect)
     * @param name eq. ParticleEffect
     * @return Class<?>
     */
    public static Class<?> getExceptionNMSClass(String name) throws Exception {
        if(version==null) {
            try {
                version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            } catch(Exception x) { version = ""; }
        }
        if(version.equals("")) return Class.forName("net.minecraft.server." + name);
        try {
            return Class.forName("net.minecraft.server." + version + "." + name);
        } catch(ClassNotFoundException e) {
            return Class.forName("net.minecraft.server." + name);
        }
    }

    /**
     * Gets Autlib Class (eq. com.mojang.autlib.GameProfile)
     * @param name eq. GameProfile
     * @return Class<?>
     */
    public static Class<?> getAuthlibClass(String name) {
        try {
            return Class.forName("com.mojang.authlib." + name);
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets CraftBukkit Class (eq. org.bukkit.craftbukkit.v1.8.R3.CraftPlayer)
     * @param name eq. CraftPlayer
     * @return Class<?>
     */
    public static Class<?> getBukkitClass(String name) {
        if(version==null) {
            try {
                version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            } catch(Exception x) { version = ""; }
        }
        if(version.equals("")) {
            try {
                return Class.forName("org.bukkit.craftbukkit." + name);
            } catch(ClassNotFoundException e2) {
                e2.printStackTrace();
                return null;
            }
        }
        try {
            return Class.forName("org.bukkit.craftbukkit." + version + "." + name);
        } catch(ClassNotFoundException e) {
            try {
                return Class.forName("org.bukkit.craftbukkit." + name);
            } catch(ClassNotFoundException e2) {
                e2.printStackTrace();
                return null;
            }
        }
    }

    /**
     * Sends the actual packet to player, not version-dependend
     * @param packet extends Packet
     * @param p PlayerConnection Target
     * @throws Exception Will probably never throw exception, otherwise blame Mojang/md_5!
     */
    public static void sendPacket(Object packet, Player p) throws Exception {
        if(p==null) return;
        Object entityPlayer = p.getClass().getMethod("getHandle").invoke(p);
        Object playerConnection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
        playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
    }

    /**
     * Gets a field.
     * @param klass Target class.
     * @param object Target object.
     * @param fieldName Target field.
     */
    public static Object getField(Class<?> klass, Object object, String fieldName) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field f = klass.getDeclaredField(fieldName);
        f.setAccessible(true);
        return f.get(object);
    }
    public static Object getField(Object object, String fieldName) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        if(object==null) return null;
        return getField(object.getClass(), object, fieldName);
    }

    /**
     * Sets a field to a given value.
     * @param klass Target class.
     * @param object Target object.
     * @param fieldName Target field.
     * @param fieldValue Value to be set.
     */
    public static void setField(Class<?> klass, Object object, String fieldName, Object fieldValue) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        Field f = klass.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(object, fieldValue);
    }
    public static void setField(Object object, String fieldName, Object fieldValue) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        setField(object.getClass(), object, fieldName, fieldValue);
    }
}
