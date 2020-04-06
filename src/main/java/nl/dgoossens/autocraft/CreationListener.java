package nl.dgoossens.autocraft;

import nl.dgoossens.autocraft.api.BlockPos;
import org.bukkit.Material;
import org.bukkit.Nameable;
import org.bukkit.block.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class CreationListener implements Listener {
    /**
     * Returns true if this block is a valid block and optionally if it's seen as an autocrafter.
     *
     * @param existing Only return true if this block is also already an autocrafter.
     */
    public static boolean isValidBlock(final Block bl, boolean existing) {
        final BlockPos bp = new BlockPos(bl);
        if(existing && AutomatedCrafting.INSTANCE.getCrafterRegistry().getAutocrafters(bl.getWorld()).map(f -> f.get(bp) != null).orElse(false)) return false;
        if(ConfigFile.allowDispensers() && bl.getState() instanceof Dispenser) return true;
        if(ConfigFile.allowHoppers() && bl.getState() instanceof Hopper) return true;
        return bl.getState() instanceof Dropper;
    }

    //This method specifically is needed because when droppers put the item directly into the neighbouring container the BlockDispenseEvent is not fired.
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDispense(final InventoryMoveItemEvent e) {
        if (e.getSource().getHolder() instanceof Container) {
            Block bl = ((Container) e.getSource().getHolder()).getBlock();
            if (isValidBlock(bl, true))
                e.setCancelled(true); //Autocrafters can't drop items normally. This is to avoid dispensing ingredients when powered.
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDispense(final BlockDispenseEvent e) {
        Block bl = e.getBlock();
        if (isValidBlock(bl, true))
            e.setCancelled(true); //Autocrafters can't drop items normally. This is to avoid dispensing ingredients when powered.
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCreate(final HangingPlaceEvent e) {
        Block bl = e.getEntity().getLocation().getBlock().getRelative(e.getEntity().getAttachedFace());
        if (isValidBlock(bl, false))
            AutomatedCrafting.INSTANCE.getCrafterRegistry().create(bl.getLocation(), e.getPlayer(), null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(final BlockBreakEvent e) {
        //Due to bukkit being annoying I can't rename the autocrafter before you break it.
        breakCrafter(e.getBlock(), true); //Destroying the item frame break the autocrafter.
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDestroy(final HangingBreakEvent e) {
        destroyCrafter(e.getEntity(), false); //Destroying the item frame break the autocrafter.
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStealItem(final EntityDamageByEntityEvent e) {
        destroyCrafter(e.getEntity(), true); //Stealing the item from the item frame destroys the autocrafter.
    }

    private void destroyCrafter(final Entity itemFrame, final boolean clean) {
        if (!(itemFrame instanceof ItemFrame)) return;
        final Block bl = itemFrame.getLocation().getBlock().getRelative(((ItemFrame) itemFrame).getAttachedFace());
        breakCrafter(bl, clean);
    }

    private void breakCrafter(final Block bl, final boolean clean) {
        if (isValidBlock(bl, true)) {
            AutomatedCrafting.INSTANCE.getCrafterRegistry().destroy(bl.getLocation());

            if (clean) { //Clean should be true when the item is removed from the item frame. (can actually be true at all times but we don't need to update droppers randomly if you're placing down item frames, could break redstone)
                ((Nameable) bl.getState()).setCustomName(null); //Set the name back to default.
                bl.getState().update();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onClickItemFrame(final PlayerInteractEntityEvent e) {
        if (e.getRightClicked().getType().equals(EntityType.ITEM_FRAME)) {
            Block bl = e.getRightClicked().getLocation().getBlock().getRelative(((ItemFrame) e.getRightClicked()).getAttachedFace());
            if (isValidBlock(bl, false)) {
                if (((ItemFrame) e.getRightClicked()).getItem().getType() != Material.AIR) { //If there's already something in the item frame, cancel!
                    e.setCancelled(true);
                    return;
                }
                new BukkitRunnable() {
                    public void run() {
                        ItemStack item = ((ItemFrame) e.getRightClicked()).getItem();
                        AutomatedCrafting.INSTANCE.getCrafterRegistry().create(bl.getLocation(), e.getPlayer(), item);

                        //Only rename if we have a valid item that we can craft in there.
                        if(AutomatedCrafting.INSTANCE.getCrafterRegistry().checkBlock(bl.getLocation(), e.getPlayer())) {
                            //The dropper is named autocrafter is it has an item frame AND there's an item in the item frame. If the item frame is empty the name should be Dropper.
                            ((Nameable) bl.getState()).setCustomName("Autocrafter"); //Rename it to autocrafter to make this clear to the player.
                            bl.getState().update();
                        }
                    }
                }.runTaskLater(AutomatedCrafting.INSTANCE, 1); //Wait a second for the item to be put into the frame.
            }
        }
    }
}
