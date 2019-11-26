package nl.dgoossens.autocraft;

import com.google.gson.JsonElement;
import nl.dgoossens.autocraft.events.AutoPostCraftItemEvent;
import nl.dgoossens.autocraft.events.AutoPreCraftItemEvent;
import nl.dgoossens.autocraft.helpers.JsonItem;
import nl.dgoossens.autocraft.helpers.MinecraftVersion;
import nl.dgoossens.autocraft.helpers.Recipe;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dropper;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dispenser;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

public class MainDropperTick extends BukkitRunnable {
    private final DropperRegistry dr;
    private final RecipeLoader rl;

    private Class<?> tagClass = null;
    private Method tagMethod = null;

    public MainDropperTick(final DropperRegistry dropperRegistry,
            final RecipeLoader recipeLoader) {
        this.dr=dropperRegistry; this.rl=recipeLoader;
        try {
            tagClass = Class.forName("org.bukkit.Tag");
            tagMethod = tagClass.getMethod("isTagged", Object.class);
        } catch(Exception x) { } //Tags don't exist before 1.14.
    }
    
    @Override
    public void run() {
        int i = dr.droppers.size();
        dr.droppers.keySet().removeIf(d -> {
            final Block block = d.getLocation().getBlock();
            if(block.getType() != Material.DROPPER) return true;
            final ItemStack m = dr.droppers.get(d);
            final Dropper dropper = (Dropper) block.getState();
            if(m==null || dropper.isLocked() || block.getBlockPower()>0) return false;
            final Set<Recipe> recipes = rl.getRecipesFor(m);
            if(recipes==null) return false;
            for(Recipe r : recipes) {
                if(r==null) continue; //Continue to the next recipe.
                final List<ItemStack> ingredients = rl.getIngredients(r);
                if(ingredients==null) continue; //Continue to the next recipe.

                AutoPreCraftItemEvent event = new AutoPreCraftItemEvent(r, block, m);
                Bukkit.getPluginManager().callEvent(event);
                if(event.isCancelled()) continue; //Continue to the next recipe.

                final Map<ItemStack, Integer> removed = new HashMap<>();
                boolean failed = false;
                for(ItemStack ite : ingredients) {
                    if(ite==null) continue;
                    //Does this dropper have this ingredient?
                    //We ignore data values in 112 so we don't need the specific data value to get removed.
                    ItemStack found = inventoryContains(dropper.getInventory(), ite);
                    if(found==null) {
                        //If we don't have the
                        removed.forEach((k, v) -> repopulate(dropper.getInventory(), k, v));
                        failed = true;
                        break; //If we can't afford this item we should cancel this recipe. (so go out of this for loop and continue on the other!)
                    }

                    //If we've got the ingredient we remove it and remember we've removed it.
                    int s = ite.getAmount();
                    if(removed.containsKey(found)) s += removed.get(found);
                    removed.put(found, s); //If we have to revert this we need to give back ite.getAmount() because we already repopulate the rest.

                    //Set the total to be minus the amount we want. The function will automatically add the current amount to this.
                    repopulate(dropper.getInventory(), found, -ite.getAmount());
                }
                if(failed) continue; //Try the next recipe!
                AutoPostCraftItemEvent postEvent = new AutoPostCraftItemEvent(r, removed, block, m);
                Bukkit.getPluginManager().callEvent(postEvent);
                if(postEvent.isCancelled()) {
                    removed.forEach((k, v) -> repopulate(dropper.getInventory(), k, v)); //Put everything back nicely.
                    continue; //Continue to the next recipe.
                }
                //If we pass this we'll always complete the craft, items are already removed from the dropper.

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
                if(loc.getWorld()!=null) loc.getWorld().dropItem(loc.clone().add(0.5, 0.25, 0.5), r.getResult());
                return false; //If one recipe got completed, stop the crafting.
            }
            return false;
        });
        if(i!=dr.droppers.size()) dr.save();
    }

    /**
     * Repopulates the dropper inventory and modified the current total of copies of
     * base with total. If total is negative the inventory will end up with total less
     * items after this method is done.
     *
     * This method also properly re-uses existing items with varying data values in 1.12.
     * (data values are not taken into account in 1.12)
     */
    private void repopulate(final Inventory dropper, final ItemStack base, int total) {
        //Calculate how much is currently present. 1.12 ignores data values.
        int present = Stream.of(dropper.getStorageContents()).filter(base::isSimilar).mapToInt(ItemStack::getAmount).sum();
        total += present;
        inventoryRemove(dropper, base);

        if(total <= 0) return; //If the total is 0 or lower we don't need to put anything back.

        //Add full stacks for every full stack within the total.
        for(int l = 0; l < total / base.getMaxStackSize(); l++) {
            ItemStack is = base.clone();
            is.setAmount(is.getMaxStackSize());
            dropper.addItem(is);
        }
        //Add remainder
        int remainder = total % base.getMaxStackSize();
        if(remainder > 0) {
            ItemStack is = base.clone();
            is.setAmount(remainder);
            dropper.addItem(is);
        }
    }

    /**
     * Checks if the inventory contains any of the json items specified
     * by the json element. Will return the json item that was
     * found.
     */
    private ItemStack inventoryContains(final Inventory inv, final ItemStack ji) {
        if(ji == null) return null;
        else {
            Map<ItemStack, Integer> amounts = new HashMap<>();
            if(ji.getAmount() > 0) amounts.put(ji, ji.getAmount());
            if(amounts.isEmpty()) return null;

            ItemStack[] var3;
            int var4 = (var3 = inv.getStorageContents()).length;

            for(ItemStack it : amounts.keySet()) {
                Object tag = null;
                //We're gonna scrap this tag stuff for now until we no longer just straight up use ItemStacks (for which tags are not useful)
                /*if(MinecraftVersion.get().atLeast(MinecraftVersion.THIRTEEN) && tagClass!=null) {
                    JsonItem jsonItem = new JsonItem(it);
                    if(jsonItem.getTag()!=null) {
                        for(Field f : tagClass.getDeclaredFields()) {
                            if(f.getType().equals(tagClass)) {
                                if(jsonItem.getTag().equalsIgnoreCase("#minecraft:"+f.getName().toLowerCase())) {
                                    try {
                                        tag = f.get(null);
                                        break;
                                    } catch(Exception x) { x.printStackTrace(); }
                                }
                            }
                        }
                    }
                }*/
                //if(item.getTag()!=null) System.out.println("[DEBUG] Found tag: "+tag);

                int amount = amounts.get(it);
                for(int var5 = 0; var5 < var4; ++var5) {
                    ItemStack i = var3[var5];
                    boolean tagSuccess = false;
                    try {
                        tagSuccess = tag!=null && (boolean) tagMethod.invoke(tag, i.getType());
                    } catch(Exception x) { x.printStackTrace(); }
                    if(tagSuccess || it.isSimilar(i)) { //This is equals() in the normal method, we need isSimilar because we don't care about amount.
                        amount -= i.getAmount(); //More logical usage of the amount for our implementation.
                        if(amount <= 0) {
                            return it;
                        }
                    }
                }
            }
            return null;
        }
    }

    /**
     * A variant of Inventory#remove(ItemStack) which ignores the amount
     * of the item stack. Removes any item similar to the base.
     */
    private void inventoryRemove(final Inventory inv, final ItemStack base) {
        ItemStack[] items = inv.getStorageContents();

        for(int i = 0; i < items.length; ++i) {
            if (items[i] != null && base.isSimilar(items[i])) //We don't care about the amount, so we need isSimilar not equals.
                inv.clear(i);
        }
    }
}
