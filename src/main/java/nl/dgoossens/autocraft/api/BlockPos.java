package nl.dgoossens.autocraft.api;

import org.bukkit.block.Block;

/**
 * A representation of a block's position.
 */
public class BlockPos {
    private int x, y, z;

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

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
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

    private static final int NUM_X_BITS = 1 + log2(smallestEncompassingPowerOfTwo(30000000));
    private static final int NUM_Z_BITS = NUM_X_BITS;
    private static final int NUM_Y_BITS = 64 - NUM_X_BITS - NUM_Z_BITS;
    private static final long X_MASK = (1L << NUM_X_BITS) - 1L;
    private static final long Y_MASK = (1L << NUM_Y_BITS) - 1L;
    private static final long Z_MASK = (1L << NUM_Z_BITS) - 1L;
    private static final int NUM_YZ_BITS = NUM_Y_BITS + NUM_Z_BITS;
    private static final int[] MULTIPLY_DE_BRUIJN_BIT_POSITION = new int[]{0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9};


    public static BlockPos fromLong(long l) {
        return new BlockPos(unpackX(l), unpackY(l), unpackZ(l));
    }

    private static int unpackX(long p_218290_0_) {
        return (int)(p_218290_0_ << 64 - NUM_YZ_BITS - NUM_X_BITS >> 64 - NUM_X_BITS);
    }

    private static int unpackY(long p_218274_0_) {
        return (int)(p_218274_0_ << 64 - NUM_Y_BITS >> 64 - NUM_Y_BITS);
    }

    private static int unpackZ(long p_218282_0_) {
        return (int)(p_218282_0_ << 64 - NUM_Y_BITS - NUM_Z_BITS >> 64 - NUM_Z_BITS);
    }

    public long toLong() {
        System.out.println("[DEBUG] Replace 'NUM_X_BITS' with '"+NUM_X_BITS+"'");
        long i = 0L;
        i = i | ((long) x & X_MASK) << NUM_YZ_BITS;
        i = i | ((long) y & Y_MASK);
        i = i | ((long) z & Z_MASK) << NUM_Y_BITS;
        return i;
    }

    /**
     * Efficiently calculates the floor of the base-2 log of an integer value.  This is effectively the index of the
     * highest bit that is set.  For example, if the number in binary is 0...100101, this will return 5.
     */
    private static int log2(int value) {
        return log2DeBruijn(value) - (isPowerOfTwo(value) ? 0 : 1);
    }

    /**
     * Is the given value a power of two?  (1, 2, 4, 8, 16, ...)
     */
    private static boolean isPowerOfTwo(int value) {
        return value != 0 && (value & value - 1) == 0;
    }

    /**
     * Uses a B(2, 5) De Bruijn sequence and a lookup table to efficiently calculate the log-base-two of the given value.
     * Optimized for cases where the input value is a power-of-two. If the input value is not a power-of-two, then
     * subtract 1 from the return value.
     */
    private static int log2DeBruijn(int value) {
        value = isPowerOfTwo(value) ? value : smallestEncompassingPowerOfTwo(value);
        return MULTIPLY_DE_BRUIJN_BIT_POSITION[(int)((long)value * 125613361L >> 27) & 31];
    }

    /**
     * Returns the input value rounded up to the next highest power of two.
     */
    private static int smallestEncompassingPowerOfTwo(int value) {
        int i = value - 1;
        i = i | i >> 1;
        i = i | i >> 2;
        i = i | i >> 4;
        i = i | i >> 8;
        i = i | i >> 16;
        return i + 1;
    }
}
