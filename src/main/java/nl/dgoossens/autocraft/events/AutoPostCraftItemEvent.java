package nl.dgoossens.autocraft.events;

import nl.dgoossens.autocraft.helpers.Recipe;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Set;

public class AutoPostCraftItemEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private final Recipe recipe;
    private final Map<ItemStack, Integer> takenItemstacks;
    private final Block block;
    private final ItemStack target;

    public AutoPostCraftItemEvent(final Recipe recipe, final Map<ItemStack, Integer> takenItemstacks, final Block block, final ItemStack target) {
        this.recipe=recipe; this.block=block; this.target=target; this.takenItemstacks=takenItemstacks;
    }

    public Block getDropper() {
        return block;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    /**
     * A map of which itemstacks were taken from the inventory.
     * The value specifies how many of the itemstack was taken.
     * (can be more than 64)
     */
    public Map<ItemStack, Integer> getTakenItemstacks() { return takenItemstacks; }

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
