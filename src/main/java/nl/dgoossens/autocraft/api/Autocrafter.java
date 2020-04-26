package nl.dgoossens.autocraft.api;

import org.bukkit.inventory.ItemStack;

/**
 * Stores all the data we have on an individual autocrafter.
 */
public class Autocrafter {
    private BlockPos position;
    private ItemStack item;
    private boolean broken;

    Autocrafter(BlockPos position, ItemStack item) {
        this.position = position;
        this.item = item;
    }

    Autocrafter(long l, ItemStack item) {
        this(BlockPos.fromLong(l), item);
    }

    public boolean isBroken() {
        return broken;
    }

    //Broken autocrafters will not be saved next time.
    public void markBroken() {
        broken = true;
    }

    /**
     * The relative position of this autocrafter in the chunk.
     */
    public BlockPos getPosition() {
        return position;
    }

    /**
     * Get the position of this autocrafter as a long.
     */
    public long getPositionAsLong() {
        return position.toLong();
    }

    /**
     * Returns whether or not the in-chunk coordinates of this autocrafter equal
     * the input numbers.
     */
    boolean equals(BlockPos pos) {
        return this.position.equals(pos);
    }

    public ItemStack getItem() {
        return item;
    }

    @Override
    public String toString() {
        return "Autocrafter{" +
                "position=" + getPosition() +
                ", positionLong=" + getPositionAsLong() +
                ", item=" + item +
                ", broken=" + broken +
                '}';
    }
}
