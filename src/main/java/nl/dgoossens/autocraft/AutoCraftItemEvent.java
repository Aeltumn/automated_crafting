package nl.dgoossens.autocraft;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.List;

public class AutoCraftItemEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private final Recipe recipe;
    private final List<ItemStack> ingredients;
    private final Block block;
    private final ItemStack target;

    public AutoCraftItemEvent(final Recipe recipe, final List<ItemStack> ingredients, final Block block, final ItemStack target) {
        this.recipe=recipe; this.ingredients=ingredients; this.block=block; this.target=target;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public List<ItemStack> getIngredients() {
        return ingredients;
    }

    /**
     * Get the itemstack that is being crafted by this autocrafter.
     */
    public ItemStack getCraftedItem() {
        return target;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
