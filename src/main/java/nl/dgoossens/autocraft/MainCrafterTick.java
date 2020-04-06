package nl.dgoossens.autocraft;

import nl.dgoossens.autocraft.api.Autocrafter;
import nl.dgoossens.autocraft.api.ChunkIdentifier;
import nl.dgoossens.autocraft.api.CraftingRecipe;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.DirectionalContainer;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MainCrafterTick extends BukkitRunnable {
    private final CrafterRegistryImpl cr;
    private final RecipeLoader rl;

    public MainCrafterTick(final CrafterRegistryImpl crafterRegistry, final RecipeLoader recipeLoader) {
        this.cr = crafterRegistry;
        this.rl = recipeLoader;
    }

    @Override
    public void run() {
        for(String s : cr.getWorldsRegistered()) {
            World w = Bukkit.getWorld(s);
            if(w == null) continue; //We skip unloaded worlds.M

            cr.getAutocrafters(s).ifPresent(m -> {
                for(ChunkIdentifier ci : m.listChunks()) {
                    if(w.isChunkLoaded(ci.getX(), ci.getZ())) return; //If the chunk isn't loaded we skip it.
                    Chunk ch = w.getChunkAt(ci.getX(), ci.getZ());

                    //Get all autocrafters in this chunk
                    for(Autocrafter a : m.getInChunk(ci)) {
                        if(a.isBroken()) continue; //Ignore broken ones.
                        Block bl = ch.getBlock(a.getX(), a.getY(), a.getZ());
                        ItemStack item = a.getItem();

                        //If the block was broken we mark it broken and no longer save it next time.
                        if(!CreationListener.isValidBlock(bl, false) || !(bl.getState() instanceof Container)) {
                            a.markBroken();
                            return;
                        }

                        final Container container = (Container) bl.getState();
                        if (container.isLocked() || bl.getBlockPower() > 0)
                            return; //If locked or powered we don't craft.

                        for(CraftingRecipe recipe : rl.getRecipesFor(item)) {
                            if(recipe == null) continue;

                            //TODO attempt to craft and take ingredients

                            //Check if there's a container nearby that wants the data.
                            final DirectionalContainer dispenser = (DirectionalContainer) bl.getState().getData();
                            final Location loc = bl.getLocation().getBlock().getRelative(dispenser.getFacing()).getLocation();
                            if (loc.getBlock().getState() instanceof InventoryHolder) {
                                InventoryHolder c = (InventoryHolder) loc.getBlock().getState();
                                ItemStack i = recipe.getResultDrop();
                                if(i.getType() != Material.AIR) {
                                    HashMap<Integer, ItemStack> couldntFit = c.getInventory().addItem(i);
                                    //Drop what couldn't fit
                                    if(!couldntFit.isEmpty() && loc.getWorld() != null) {
                                        couldntFit.forEach((k, v) -> {
                                            if(v == null || v.getType() == Material.AIR) return; //Can't drop this
                                            v.setAmount(k);
                                            loc.getWorld().dropItem(loc.clone().add(0.5, 0.25, 0.5), v);
                                        });
                                    }
                                }
                            }
                            //Drop the item if no container wants it.
                            if (loc.getWorld() != null) {
                                ItemStack i = recipe.getResultDrop();
                                if(i.getType() != Material.AIR)
                                    loc.getWorld().dropItem(loc.clone().add(0.5, 0.25, 0.5), i);
                            }
                            return; //If one recipe got completed, stop the crafting.
                        }
                        return;
                    }
                }
            });
        }
    }
}
