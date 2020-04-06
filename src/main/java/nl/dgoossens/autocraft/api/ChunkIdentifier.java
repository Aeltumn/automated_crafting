package nl.dgoossens.autocraft.api;

import java.util.Objects;

/**
 * An identifier that represents a specific chunk in the world.
 */
public class ChunkIdentifier {
    private int x, z;
    private short y;

    /**
     * Build a chunk identifier from a long.
     */
    public ChunkIdentifier(long l) {
        BlockPos p = BlockPos.fromLong(l);
        this.x = p.getX();
        this.y = (short) p.getY();
        this.z = p.getZ();
    }

    ChunkIdentifier(BlockPos position) {
        this.x = position.getX() >> 16; //Needs 21 bits
        this.y = (short) (position.getY() >> 16); //At most 4 bits
        this.z = position.getZ() >> 16; //Needs 21 bits
    }

    public int getX() {
        return x;
    }

    public short getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    /**
     * Get a long with all information in it to recreate this chunk identifier.
     */
    public long toLong() {
        return new BlockPos(x, y, z).toLong();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkIdentifier that = (ChunkIdentifier) o;
        return x == that.x &&
                y == that.y &&
                z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}
