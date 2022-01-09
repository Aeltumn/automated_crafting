package com.aeltumn.autocraft.impl;

import com.aeltumn.autocraft.api.Autocrafter;
import com.aeltumn.autocraft.api.ChunkIdentifier;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

/**
 * Ticks all loaded autocrafters in the world.
 */
public class MainCrafterTick extends BukkitRunnable {
    private final CrafterRegistryImpl cr;

    public MainCrafterTick(final CrafterRegistryImpl crafterRegistry) {
        this.cr = crafterRegistry;
    }

    @Override
    public void run() {
        for (String s : cr.getWorldsRegistered()) {
            World w = Bukkit.getWorld(s);
            //We skip unloaded worlds.
            if (w == null) continue;

            cr.getAutocrafters(s).ifPresent(m -> {
                for (ChunkIdentifier ci : m.listChunks()) {
                    //If the chunk isn't loaded we skip it.
                    if (!w.isChunkLoaded(ci.getX(), ci.getZ())) continue;

                    Chunk chunk = w.getChunkAt(ci.getX(), ci.getZ());
                    for (Autocrafter a : Objects.requireNonNull(m.getInChunk(ci))) {
                        a.tick(chunk);
                    }
                }
            });
        }
    }
}
