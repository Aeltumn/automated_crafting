package nl.dgoossens.autocraft.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * An event called whenever a player attempts to create an autocrafter.
 */
public class AutocrafterCreateEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private final Location location;
    private final Player player;
    private final ItemStack item;

    public AutocrafterCreateEvent(final Location location, Player p, final ItemStack item) {
        this.item=item; this.location=location; this.player=p;
    }

    /**
     * Get the location where the autocrafter will be created.
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Get the player creating the autocrafter.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Get the item being put in the item frame.
     */
    public ItemStack getItem() {
        return item;
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
