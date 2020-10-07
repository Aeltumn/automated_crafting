package nl.dgoossens.autocraft.helpers;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * The ReflectionHelper is a utility to aid in using reflection to interact
 * with internal Minecraft classes. It is however preferred to write version
 * specific code as the performance impact of using reflection often is quite
 * substantial.
 */
public final class ReflectionHelper {
    private static String version = null; //`v1_12_R1`
    private static String nmsPackage = ""; //`net.minecraft.server.v1_12_R1.`
    private static String cbPackage = ""; //`org.bukkit.craftbukkit.v1_12_R1.`

    private static final String NMS_STRING = "net.minecraft.server.";
    private static final String CB_STRING = "org.bukkit.craftbukkit.";

    private static final Class<?> packetClass = getNMSClass("Packet").orElse(null);
    private static final Class<?> entityPlayerClass = getNMSClass("EntityPlayer").orElse(null);
    private static final Class<?> playerConnectionClass = getNMSClass("PlayerConnection").orElse(null);
    private static final Class<?> craftPlayerClass = getCraftBukkitClass("entity.CraftPlayer").orElse(null);
    private static final Field playerConnectionField = getField(entityPlayerClass, "playerConnection").orElse(null);
    private static final Method sendPacketMethod = getMethod(playerConnectionClass, "sendPacket", packetClass).orElse(null);
    private static final Method getHandleMethod = getMethod(craftPlayerClass, "getHandle").orElse(null);
    private static final Class<?> chatComponentTextClass = getNMSClass("ChatComponentText").orElse(null);
    private static final Constructor<?> chatComponentTextConstructor = getConstructor(chatComponentTextClass, String.class).orElse(null);

    /**
     * Loads the version this utility looks at to determine the class
     * locations of NMS/CB classes.
     */
    private static void loadVersion(final String full) {
        StringBuilder sb = new StringBuilder();
        int s = 0;
        if (full.startsWith(NMS_STRING)) s = NMS_STRING.length();
        if (full.startsWith(CB_STRING)) s = CB_STRING.length();
        if (s == 0) throw new UnsupportedOperationException("Tried to load version of NMS/CB package without passing a usable package.");

        for (int c = s; c < full.length(); c++) {
            final char ch = full.charAt(c);
            if (ch == '.') break;
            sb.append(ch);
        }
        version = sb.toString();
        nmsPackage = NMS_STRING + (version.length() > 0 ? version + "." : "");
        cbPackage = CB_STRING + (version.length() > 0 ? version + "." : "");
    }

    /**
     * Returns the Minecraft version being used.
     */
    public static String getVersion() {
        if (version == null) loadVersion(Bukkit.getServer().getClass().getPackage().getName());
        return version;
    }

    /**
     * Gets NMS Class (eg. net.minecraft.server.v1_12_R1.ParticleEffect)
     */
    public static Optional<Class<?>> getNMSClass(String name) {
        try {
            if (nmsPackage.equals("")) loadVersion(Bukkit.getServer().getClass().getPackage().getName());
            return Optional.of(Class.forName(nmsPackage + name));
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * Gets CraftBukkit class (eg. org.bukkit.craftbukkit.v1_12_R1.CraftPlayer)
     */
    public static Optional<Class<?>> getCraftBukkitClass(String name) {
        try {
            if (cbPackage.equals("")) loadVersion(Bukkit.getServer().getClass().getPackage().getName());
            return Optional.of(Class.forName(cbPackage + name));
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * Gets a method in a certain class.
     * Gets the declared method so this will work with private methods.
     */
    public static Optional<Method> getMethod(@Nullable Class<?> klass, String methodName, Class<?>... parameters) {
        if (klass == null) return Optional.empty();
        try {
            Method m = klass.getDeclaredMethod(methodName, parameters);
            m.setAccessible(true);
            return Optional.of(m);
        } catch (Exception ignored) {}
        return Optional.empty();
    }

    /**
     * Gets a constructor in a certain class.
     * Gets the declared constructor so this will work with private constructor.
     */
    public static <T> Optional<Constructor<T>> getConstructor(@Nullable Class<T> klass, Class<?>... parameters) {
        if (klass == null) return Optional.empty();
        try {
            Constructor<T> c = klass.getDeclaredConstructor(parameters);
            c.setAccessible(true);
            return Optional.of(c);
        } catch (Exception ignored) {}
        return Optional.empty();
    }

    /**
     * Gets a field in a certain class.
     * Gets the declared field so this will work with private fields.
     */
    public static Optional<Field> getField(@Nullable Class<?> klass, String fieldName) {
        if (klass == null) return Optional.empty();
        try {
            Field f = klass.getDeclaredField(fieldName);
            f.setAccessible(true);
            return Optional.of(f);
        } catch (Exception ignored) {}
        return Optional.empty();
    }

    /**
     * Returns the value stored in a given field in a given object.
     * Optional is empty if the field wasn't found.
     */
    public static Optional<Object> getFieldValue(Object object, Field field) {
        try {
            return Optional.ofNullable(field.get(object));
        } catch (Exception ignored) {}
        return Optional.empty();
    }

    /**
     * Sets a given field of a given object to a given value.
     */
    public static void setField(Object object, Field field, Object value) {
        try {
            field.set(object, value);
        } catch (Exception ignored) {}
    }

    /**
     * Sends a packet to player, not version-dependent.
     */
    public static void sendPacket(@Nullable Object packet, @Nullable Player player) throws Exception {
        if (packet == null || player == null) return;
        Object entityPlayer = getHandleMethod.invoke(player);
        Object playerConnection = playerConnectionField.get(entityPlayer);
        sendPacketMethod.invoke(playerConnection, packet);
    }

    /**
     * Get the IChatBaseComponent from a string of text.
     */
    public static Object getChatBaseComponent(String text) {
        try {
            return chatComponentTextConstructor.newInstance(ChatColor.translateAlternateColorCodes('&', text));
        } catch (Exception ignored) {}
        return null;
    }
}
