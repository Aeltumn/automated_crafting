package com.aeltumn.autocraft.api;

import org.bukkit.block.Block;

/**
 * A representation of a block's position.
 */
public class BlockPos {
    private static final int NUM_X_BITS = 26;
    private static final int NUM_Z_BITS = 26;
    private static final int NUM_Y_BITS = 12;
    private static final long X_MASK = (1L << NUM_X_BITS) - 1L;
    private static final long Y_MASK = (1L << NUM_Y_BITS) - 1L;
    private static final long Z_MASK = (1L << NUM_Z_BITS) - 1L;
    private static final int NUM_YZ_BITS = 38;

    private final int x;
    private final int y;
    private final int z;

    public BlockPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockPos(Block l) {
        x = l.getX();
        y = l.getY();
        z = l.getZ();
    }

    public static BlockPos fromLong(long l) {
        return new BlockPos(unpackX(l), unpackY(l), unpackZ(l));
    }

    private static int unpackX(long v) {
        return (int) (v << 64 - NUM_YZ_BITS - NUM_X_BITS >> 64 - NUM_X_BITS);
    }

    private static int unpackY(long v) {
        return (int) (v << 64 - NUM_Y_BITS >> 64 - NUM_Y_BITS);
    }

    private static int unpackZ(long v) {
        return (int) (v << 64 - NUM_Y_BITS - NUM_Z_BITS >> 64 - NUM_Z_BITS);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public BlockPos subtract(BlockPos other) {
        return new BlockPos(x - other.x, y - other.y, z - other.z);
    }

    public long toLong() {
        long i = 0L;
        i = i | ((long) x & X_MASK) << NUM_YZ_BITS;
        i = i | ((long) y & Y_MASK);
        i = i | ((long) z & Z_MASK) << NUM_Y_BITS;
        return i;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockPos blockPos = (BlockPos) o;
        return x == blockPos.x &&
                y == blockPos.y &&
                z == blockPos.z;
    }

    @Override
    public String toString() {
        return "BlockPos{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
