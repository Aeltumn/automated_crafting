package nl.dgoossens.autocraft.api;

import org.bukkit.inventory.ItemStack;

/**
 * Stores all the data we have on an individual autocrafter.
 */
public class Autocrafter {
    private short x, z;
    private int y;
    private ItemStack item;
    private boolean broken;

    Autocrafter(BlockPos position, ItemStack item) {
        this.x = (short) (position.getX() & 0xF);
        this.y = position.getY();
        this.z = (short) (position.getZ() & 0xF);
        this.item = item;
    }

    Autocrafter(long l, ItemStack item) {
        this.y = (short) (l >>> 32);
        this.z = (short) ((l >>> 16) & 0xFFFF);
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

    public int getY() {
        return y;
    }

    public short getZ() {
        return z;
    }

    /**
     * Get the position of this autocrafter as a long.
     */
    public long getPositionAsLong() {
        return x + z << 16 + y << 32;
    }

    /**
     * Get the position long for a generic block position.
     */
    public static long getPositionLong(BlockPos position) {
        short x = (short) (position.getX() & 0xF);
        int y = position.getY();
        short z = (short) (position.getZ() & 0xF);
        return x + y << 32 + z << 16;
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
