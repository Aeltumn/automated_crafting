package nl.dgoossens.autocraft.api;

import java.util.Objects;

/**
 * An identifier that represents a specific chunk in the world.
 */
public class ChunkIdentifier {
    private int x, z;

    /**
     * Build a chunk identifier from a long.
     */
    public ChunkIdentifier(long l) {
        BlockPos p = BlockPos.fromLong(l);
        this.x = p.getX();
        this.z = p.getZ();
    }

    ChunkIdentifier(BlockPos position) {
        this.x = position.getX() >> 16; //Needs 21 bits
        this.z = position.getZ() >> 16; //Needs 21 bits
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    /**
     * Get a long with all information in it to recreate this chunk identifier.
     */
    public long toLong() {
        return new BlockPos(x, 0, z).toLong();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkIdentifier that = (ChunkIdentifier) o;
        return x == that.x &&
                z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }
}
