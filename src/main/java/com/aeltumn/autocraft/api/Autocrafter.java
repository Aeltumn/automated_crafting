package com.aeltumn.autocraft.api;

import com.aeltumn.autocraft.AutomatedCrafting;
import com.aeltumn.autocraft.ConfigFile;
import com.aeltumn.autocraft.CreationListener;
import com.aeltumn.autocraft.helpers.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Stores all the data we have on an individual autocrafter.
 */
public class Autocrafter {
    private final String world;
    private final BlockPos position;
    @Nullable
    private ItemStack item;
    private boolean broken;

    Autocrafter(String world, BlockPos position, @Nullable ItemStack item) {
        this.world = world;
        this.position = position;
        this.item = item;
    }

    Autocrafter(String world, long l) {
        this(world, BlockPos.fromLong(l), null);
    }

    /**
     * Ticks this auto-crafter, making it craft an item.
     */
    public void tick(Chunk chunk) {
        // Don't tick if broken
        if (isBroken()) return;

        Block bl = chunk.getBlock(position.getX(), position.getY(), position.getZ());

        //If the block was broken we mark it broken and no longer save it next time.
        if (!CreationListener.isValidBlock(bl, false) || !(bl.getState() instanceof final Container container)) {
            markBroken();
            return;
        }

        // If locked we don't craft.
        if (container.isLocked())
            return;

        // If powered and not crafting based on signal we don't craft.
        if (!ConfigFile.craftOnRedstonePulse()) {
            if (bl.getBlockPower() > 0) return;
        }

        // Get the item, if we can't find it ignore
        var itemType = getItem(chunk);
        if (itemType.isEmpty()) return;

        // Never craft a material that is banned
        if (!ConfigFile.isMaterialAllowed(itemType.getType())) return;

        outer:
        for (CraftingRecipe recipe : AutomatedCrafting.INSTANCE.getRecipeLoader().getRecipesFor(itemType)) {
            if (recipe == null) continue;

            // Check if the dropper contains all required items to craft the result.
            if (!recipe.containsRequirements(container.getInventory()))
                continue;

            final Directional autocrafter = (Directional) bl.getBlockData();
            final Location loc = bl.getLocation().getBlock().getRelative(autocrafter.getFacing()).getLocation();

            // Determine what craft we can perform
            var solution = recipe.findSolution(container.getInventory());
            ArrayList<ItemStack> output = new ArrayList<>(solution.getContainerItems());
            output.add(recipe.getResultDrop());

            // Remove null items
            output.removeIf(f -> f == null || f.getType().isAir());

            // Check if there's a container being output into and if it can fit the
            // items to put in. If there is a container we only craft if we can fit it.
            if (loc.getBlock().getState() instanceof InventoryHolder c) {
                // Try to add all items to a copy of the inventory in order so we know whether all
                // output items can fit when given together.
                var inventoryCopy = new ItemStack[c.getInventory().getStorageContents().length];
                for (var i = 0; i < inventoryCopy.length; i++) {
                    var it = c.getInventory().getStorageContents()[i];
                    if (it != null) {
                        inventoryCopy[i] = it.clone();
                    }
                }
                for (var item : output) {
                    if (!Utils.addItem(inventoryCopy, item.clone())) {
                        continue outer;
                    }
                }
            }

            // Perform the actual crafting
            solution.applyTo(container.getInventory());

            if (loc.getBlock().getState() instanceof InventoryHolder c) {
                // Put the output in the output container
                for (var item : output) {
                    c.getInventory().addItem(item);
                }
            } else {
                // Drop the items if no container wants them
                if (loc.getWorld() != null) {
                    for (var item : output) {
                        loc.getWorld().dropItem(loc.clone().add(0.5, 0.25, 0.5), item);
                    }
                }
            }

            // If a recipe was found we stop here, never craft two things
            break;
        }
    }

    /**
     * Returns whether this autocrafter is broken.
     */
    public boolean isBroken() {
        return broken;
    }

    /**
     * Marks this autocrafter as broken. A broken autocrafter is not saved to disk.
     */
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

    @NotNull
    public ItemStack getItem(Chunk baseChunk) {
        if (item != null) {
            return item;
        }

        // Try to get an item frame attached to this auto crafter,
        // always set the item to empty to indicate we are done trying!
        var world = Bukkit.getWorld(this.world);
        if (world != null) {
            var chunks = new HashSet<Chunk>();
            var entities = new HashSet<Entity>();
            for (var direction : List.of(BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.UP, BlockFace.DOWN)) {
                var chunk = world.getChunkAt(
                    new Location(
                        world,
                        (baseChunk.getX() << 4) + position.getX() + direction.getModX(),
                        position.getY() + direction.getModY(),
                        (baseChunk.getZ() << 4) + position.getZ() + direction.getModZ()
                    )
                );

                // Test each chunk once
                if (chunks.contains(chunk)) continue;
                chunks.add(chunk);

                // Ignore until entities are loaded!
                if (!chunk.isEntitiesLoaded()) return ItemStack.empty();
                entities.addAll(List.of(chunk.getEntities()));
            }

            var autocrafterBlock = baseChunk.getBlock(position.getX(), position.getY(), position.getZ());
            for (var entity : entities) {
                if (entity instanceof ItemFrame itemFrame) {
                    var entityBlock = itemFrame.getLocation().getBlock();
                    var attachedFace = itemFrame.getAttachedFace();
                    var attachedBlock = entityBlock.getRelative(attachedFace);
                    if (attachedBlock.equals(autocrafterBlock)) {
                        item = itemFrame.getItem();
                        return item;
                    }
                }
            }
            item = ItemStack.empty();
        }
        return ItemStack.empty();
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
