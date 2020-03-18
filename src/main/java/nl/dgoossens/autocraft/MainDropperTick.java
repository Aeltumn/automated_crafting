package nl.dgoossens.autocraft;

import nl.dgoossens.autocraft.api.BlockPos;
import nl.dgoossens.autocraft.api.CraftingRecipe;
import nl.dgoossens.autocraft.helpers.BukkitRecipe;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Dropper;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.DirectionalContainer;
import org.bukkit.material.Dispenser;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Stream;

public class MainDropperTick extends BukkitRunnable {
    private final CrafterRegistryImpl dr;
    private final RecipeLoader rl;

    public MainDropperTick(final CrafterRegistryImpl dropperRegistry, final RecipeLoader recipeLoader) {
        this.dr = dropperRegistry;
        this.rl = recipeLoader;
    }

    @Override
    public void run() {
        boolean save = false;
        for(String s : dr.getWorldsRegistered()) {
            World w = Bukkit.getWorld(s);
            if(w == null) continue; //We skip unloaded worlds.

            dr.getAutocrafterMap(s).entrySet().removeIf(entry -> {
                BlockPos block = entry.getKey();
                ItemStack item = entry.getValue();

                final Block bl = w.getBlockAt(block.getX(), block.getY(), block.getZ());
                if(!CreationListener.isValidBlock(bl, false)) return true; //Destroy if it's been removed
                if(!(bl.getState() instanceof Container)) return true; //Destroy if it's not a container
                final Container container = (Container) bl.getState();
                if (container.isLocked() || bl.getBlockPower() > 0) return false; //If locked or powered we don't craft.

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
                    return false; //If one recipe got completed, stop the crafting.
                }
                return false;
            });

        }
        if(save)
            dr.save();
    }
}
