package nl.dgoossens.autocraft.events;

import nl.dgoossens.autocraft.helpers.Recipe;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class AutoPreCraftItemEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private final Recipe recipe;
    private final Block block;
    private final ItemStack target;

    public AutoPreCraftItemEvent(final Recipe recipe, final Block block, final ItemStack target) {
        this.recipe=recipe; this.block=block; this.target=target;
    }

    public Block getDropper() {
        return block;
    }

    public Recipe getRecipe() {
        return recipe;
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
