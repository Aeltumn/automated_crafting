package nl.dgoossens.autocraft.api;

import org.bukkit.inventory.ItemStack;

/**
 * Stores all the data we have on an individual autocrafter.
 */
public class Autocrafter {
    private short x, y, z;
    private ItemStack item;
    private boolean broken;

    Autocrafter(BlockPos position, ItemStack item) {
        this.x = (short) (position.getX() & 0xF);
        this.y = (short) (position.getY() & 0xF);
        this.z = (short) (position.getZ() & 0xF);
        this.item = item;
    }

    Autocrafter(long l, ItemStack item) {
        this.z = (short) (l >>> 32);
        this.y = (short) ((l >>> 16) & 0xFFFF);
        this.x = (short) (l & 0xFFFF);
        this.item = item;
    }

    public boolean isBroken() {
        return broken;
    }

    //Broken autocrafters will not be saved next time.
    public void markBroken() {
        broken = true;
    }

    public short getX() {
        return x;
    }

    public short getY() {
        return y;
    }

    public short getZ() {
        return z;
    }

    /**
     * Get the position of this autocrafter as a long.
     */
    public long getPositionAsLong() {
        return x + y << 16 + z << 32;
    }

    /**
     * Get the position long for a generic block position.
     */
    public static long getPositionLong(BlockPos position) {
        short x = (short) (position.getX() & 0xF);
        short y = (short) (position.getY() & 0xF);
        short z = (short) (position.getZ() & 0xF);
        return x + y << 16 + z << 32;
    }

    /**
     * Returns whether or not the in-chunk coordinates of this autocrafter equal
     * the input numbers.
     */
    boolean equals(short x, short y, short z) {
        return this.x == x && this.y == y && this.z == z;
    }

    public ItemStack getItem() {
        return item;
    }
}
