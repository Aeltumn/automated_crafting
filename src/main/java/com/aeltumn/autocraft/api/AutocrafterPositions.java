package com.aeltumn.autocraft.api;

import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * An object storing information on all autocrafters in a single world.
 */
public class AutocrafterPositions {
    private final HashMap<ChunkIdentifier, ArrayList<Autocrafter>> data;

    public AutocrafterPositions() {
        data = new HashMap<>();
    }

    /**
     * We don't store autocrafter position objects with empty
     * data.
     */
    public boolean isEmpty() {
        return data.isEmpty();
    }

    /**
     * Get a set of all chunks in this world that contain autocrafters.
     */
    public Set<ChunkIdentifier> listChunks() {
        return data.keySet();
    }

    /**
     * Get a list of all autocrafters in a chunk, this list should not be
     * used to perform operations on!
     */
    @Nullable
    public ArrayList<Autocrafter> getInChunk(ChunkIdentifier ci) {
        return data.get(ci);
    }

    /**
     * Get an autocrafter at a position.
     */
    @Nullable
    public Autocrafter get(BlockPos position) {
        ChunkIdentifier ci = new ChunkIdentifier(position);
        long l = position.subtract(ci.getPosition()).toLong();
        if (data.containsKey(ci)) {
            //Find the autocrafter that has this position long as its position
            for (Autocrafter a : data.get(ci)) {
                if (a.getPositionAsLong() == l)
                    return a;
            }
        }
        return null;
    }

    /**
     * Adds a new autocrafter to this data object.
     */
    public void add(BlockPos position, ItemStack item) {
        ChunkIdentifier ci = new ChunkIdentifier(position);
        data.computeIfAbsent(ci, (a) -> new ArrayList<>()).add(new Autocrafter(position.subtract(ci.getPosition()), item));
    }

    /**
     * Adds a new autocrafter to this data object in a given chunk
     * with a long created by the {@link Autocrafter#getPositionAsLong()} method in the chunk.
     */
    public void add(ChunkIdentifier chunk, long l, ItemStack item) {
        data.computeIfAbsent(chunk, (a) -> new ArrayList<>()).add(new Autocrafter(l, item));
    }

    /**
     * Removes any autocrafters on a given position from this
     * data object.
     */
    public void destroy(BlockPos position) {
        ChunkIdentifier ci = new ChunkIdentifier(position);
        if (!data.containsKey(ci)) return; //No data on this chunk means we can't destroy it to begin with
        ArrayList<Autocrafter> crafters = new ArrayList<>(data.get(ci));
        long l = position.subtract(ci.getPosition()).toLong();

        //Remove existing autocrafters on this block from the list
        crafters.removeIf(a -> a.getPositionAsLong() == l);

        //Put data back into crafters map
        if (crafters.isEmpty()) data.remove(ci);
        else data.put(ci, crafters);
    }
}
