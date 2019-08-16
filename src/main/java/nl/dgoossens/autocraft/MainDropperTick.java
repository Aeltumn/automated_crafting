package nl.dgoossens.autocraft;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dropper;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.material.Dispenser;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class MainDropperTick extends BukkitRunnable {
    private final DropperRegistry dr;
    public MainDropperTick(final DropperRegistry dropperRegistry) { this.dr=dropperRegistry; }
    
    @Override
    public void run() {
        int i = dr.droppers.size();
        dr.droppers.keySet().removeIf(d -> {
            final Block block = d.getLocation().getBlock();
            if(block.getType() != Material.DROPPER) return true;
            final ItemStack m = dr.droppers.get(d);
            final Dropper dropper = (Dropper) block.getState();
            if(m==null || dropper.isLocked() || block.getBlockPower()>0) return false;
            final Set<Recipe> recipes = dr.getRecipe(m);
            if(recipes==null) return false;
            for(Recipe r : recipes) {
                if(r==null) continue; //Continue to the next recipe.
                final List<ItemStack> ingredients = dr.getIngredients(r);
                if(ingredients==null) continue; //Continue to the next recipe.

                AutoCraftItemEvent event = new AutoCraftItemEvent(r, ingredients, block, m);
                Bukkit.getPluginManager().callEvent(event);
                if(event.isCancelled()) continue; //Continue to the next recipe.

                final List<ItemStack> removed = new ArrayList<>();
                boolean failed = false;
                for(ItemStack ite : ingredients) {
                    if(ite==null) continue;
                    //Does this dropper have this ingredient?
                    if(!dropper.getInventory().contains(ite.getType(), ite.getAmount())) {
                        removed.forEach(r2 -> dropper.getInventory().addItem(r2));
                        failed = true;
                        break; //If we can't afford this item we should cancel this recipe. (so go out of this for loop and continue on the other!)
                    }
                    //If we've got the ingredient we remove it and remember we've removed it.
                    removed.add(ite);
                    //Take all similar items.
                    int ico = Stream.of(dropper.getInventory().getContents()).filter(ite::isSimilar).mapToInt(ItemStack::getAmount).sum();
                    ico -= ite.getAmount();
                    dropper.getInventory().remove(ite.getType());
                    if(ico > 0) {
                        dropper.getInventory().addItem(new ItemStack(ite.getType(), ico % ite.getMaxStackSize()));
                        for(int j = 0; j < ico / ite.getMaxStackSize(); j++) dropper.getInventory().addItem(new ItemStack(ite.getType(), ite.getMaxStackSize()));
                    }
                }
                if(failed) continue; //Try the next recipe!
                //If we pass this we'll always conplete the craft, items are already removed from the dropper.

                //Check if there's a container nearby that wants the data.
                final Dispenser dispenser = (Dispenser) dropper.getData(); //Dropper is a dispenser too!
                final Location loc = dropper.getLocation().getBlock().getRelative(dispenser.getFacing()).getLocation();
                if(loc.getBlock().getState() instanceof InventoryHolder) {
                    InventoryHolder c = (InventoryHolder) loc.getBlock().getState();
                    if(c.getInventory().firstEmpty() != -1 || Stream.of(c.getInventory().getContents()).anyMatch(f -> r.getResult().isSimilar(f) && f.getAmount() <= f.getMaxStackSize()-r.getResult().getAmount())) {
                        c.getInventory().addItem(r.getResult());
                        return false; //If one recipe got completed, stop the crafting.
                    }
                }
                //Drop the item if no container wants it.
                if(loc.getWorld()!=null) loc.getWorld().dropItemNaturally(loc, r.getResult());
                return false; //If one recipe got completed, stop the crafting.
            }
            return false;
        });
        if(i!=dr.droppers.size()) dr.save();
    }
}
