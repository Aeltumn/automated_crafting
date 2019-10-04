package nl.dgoossens.autocraft.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * An event called whenever an autocrafter is destroyed.
 */
public class AutocrafterDestroyEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Location location;
    private final ItemStack item;

    public AutocrafterDestroyEvent(final Location location, final ItemStack item) {
        this.item=item; this.location=location;
    }

    /**
     * Get the location where the autocrafter will be created.
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Get the item that was in the item frame.
     */
    public ItemStack getItem() {
        return item;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
