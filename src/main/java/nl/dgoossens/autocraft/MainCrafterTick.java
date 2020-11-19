package nl.dgoossens.autocraft;

import java.util.ArrayList;
import nl.dgoossens.autocraft.api.Autocrafter;
import nl.dgoossens.autocraft.api.BlockPos;
import nl.dgoossens.autocraft.api.ChunkIdentifier;
import nl.dgoossens.autocraft.api.CraftingRecipe;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.DirectionalContainer;
import org.bukkit.scheduler.BukkitRunnable;

public class MainCrafterTick extends BukkitRunnable {
    private final CrafterRegistryImpl cr;
    private final RecipeLoader rl;

    public MainCrafterTick(final CrafterRegistryImpl crafterRegistry, final RecipeLoader recipeLoader) {
        this.cr = crafterRegistry;
        this.rl = recipeLoader;
    }

    @Override
    public void run() {
        for (String s : cr.getWorldsRegistered()) {
            World w = Bukkit.getWorld(s);
            if (w == null) continue; //We skip unloaded worlds.

            cr.getAutocrafters(s).ifPresent(m -> {
                for (ChunkIdentifier ci : m.listChunks()) {
                    if (!w.isChunkLoaded(ci.getX(), ci.getZ())) continue; //If the chunk isn't loaded we skip it.
                    Chunk ch = w.getChunkAt(ci.getX(), ci.getZ());

                    for (Autocrafter a : m.getInChunk(ci)) {
                        if (a.isBroken()) continue; //Ignore broken ones.
                        BlockPos position = a.getPosition();
                        Block bl = ch.getBlock(position.getX(), position.getY(), position.getZ());
                        ItemStack item = a.getItem();

                        //If the block was broken we mark it broken and no longer save it next time.
                        if (!CreationListener.isValidBlock(bl, false) || !(bl.getState() instanceof Container)) {
                            a.markBroken();
                            continue;
                        }

                        final Container container = (Container) bl.getState();
                        if (container.isLocked() || bl.getBlockPower() > 0)
                            continue; //If locked or powered we don't craft.

                        for (CraftingRecipe recipe : rl.getRecipesFor(item)) {
                            if (recipe == null) continue;

                            //Check if the dropper contains all required items to craft the result.
                            if (!recipe.containsRequirements(container.getInventory()))
                                continue;

                            final DirectionalContainer dispenser = (DirectionalContainer) bl.getState().getData();
                            final Location loc = bl.getLocation().getBlock().getRelative(dispenser.getFacing()).getLocation();

                            //Check if there's a container being output into and if it can fit the
                            //items to put in.
                            if (loc.getBlock().getState() instanceof InventoryHolder) {
                                InventoryHolder c = (InventoryHolder) loc.getBlock().getState();
                                ItemStack i = recipe.getResultDrop();
                                if (i.getType() != Material.AIR && !canInventorySupport(c.getInventory(), i))
                                    continue;
                            }

                            //Take the materials we need for the craft from the crafter
                            ArrayList<ItemStack> leftovers = recipe.takeMaterials(container.getInventory());

                            //Put leftovers back into the inventory, we'll assume it always fits
                            container.getInventory().addItem(leftovers.toArray(new ItemStack[0]));

                            //Check if there's a container nearby that wants the output.
                            //This could never trigger if we didn't have enough space in the holder
                            //as that would have caused this recipe to be discarded earlier.
                            if (loc.getBlock().getState() instanceof InventoryHolder) {
                                InventoryHolder c = (InventoryHolder) loc.getBlock().getState();
                                ItemStack i = recipe.getResultDrop();
                                if (i.getType() != Material.AIR) {
                                    c.getInventory().addItem(i);
                                }
                                break;
                            }

                            //Drop the item if no container wants it.
                            if (loc.getWorld() != null) {
                                ItemStack i = recipe.getResultDrop();
                                if (i != null && i.getType() != Material.AIR)
                                    loc.getWorld().dropItem(loc.clone().add(0.5, 0.25, 0.5), i);
                            }
                            break; //If one recipe got completed, stop the crafting.
                        }
                    }
                }
            });
        }
    }

    /**
     * Returns whether we can still fit itemstack it into an inventory.
     */
    private boolean canInventorySupport(Inventory inv, ItemStack it) {
        if (it == null || it.getType() == Material.AIR) return true;

        ItemStack[] storage = inv.getStorageContents();
        int amountToDeposit = it.getAmount();
        for (int i = 0; i < storage.length; i++) {
            if (storage[i] == null) return true;

            ItemStack ite = storage[i];
            if (ite.isSimilar(it)) {
                // Remove how much we can still fit on stack from the amount to
                // deposit
                amountToDeposit -= (ite.getMaxStackSize() - ite.getAmount());

                // If we deposited everything we fit it in.
                if (amountToDeposit <= 0) return true;
            }
        }
        return false;
    }
}
