package nl.dgoossens.autocraft;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dropper;
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
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class CreationListener implements Listener {
    private AutomatedCrafting instance;
    public CreationListener(AutomatedCrafting instance) { this.instance=instance; }

    private boolean isDropper(final Block dropper) {
        final BlockPos bp = new BlockPos(dropper);
        return dropper.getState() instanceof Dropper && instance.getDropperRegistry().droppers.keySet().parallelStream().anyMatch(bp::equals);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDispense(BlockDispenseEvent e) {
        Block dropper = e.getBlock();
        if(isDropper(dropper))
            e.setCancelled(true); //Autocrafters can't drop items normally. This is to avoid dispensing ingredients when powered.
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCreate(HangingPlaceEvent e) {
        Block dropper = e.getEntity().getLocation().getBlock().getRelative(e.getEntity().getAttachedFace());
        if(dropper.getState() instanceof Dropper) {
            Dropper d = (Dropper) dropper.getState();
            d.setCustomName("Autocrafter"); //Rename it to autocrafter to make this clear to the player.
            d.update();
            instance.getDropperRegistry().create(d.getLocation(), null);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        breakDropper(e.getBlock()); //Destroying the item frame break the autocrafter.
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDestroy(HangingBreakEvent e) {
        destroyDropper(e.getEntity()); //Destroying the item frame break the autocrafter.
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStealItem(EntityDamageByEntityEvent e) {
        destroyDropper(e.getEntity()); //Stealing the item from the item frame destroys the autocrafter.
    }

    private void destroyDropper(final Entity itemFrame) {
        if(!(itemFrame instanceof ItemFrame)) return;
        final Block dropper = itemFrame.getLocation().getBlock().getRelative(((ItemFrame) itemFrame).getAttachedFace());
        breakDropper(dropper);
    }
    private void breakDropper(final Block dropper) {
        if(dropper.getState() instanceof Dropper) {
            instance.getDropperRegistry().destroy(dropper.getLocation());
            Dropper d = (Dropper) dropper.getState();
            d.setCustomName("Dropper"); //Set the name back to the default dropper.
            d.update();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onClickItemFrame(PlayerInteractEntityEvent e) {
        if(e.getRightClicked().getType().equals(EntityType.ITEM_FRAME)) {
            Block dropper = e.getRightClicked().getLocation().getBlock().getRelative(((ItemFrame) e.getRightClicked()).getAttachedFace());
            if(dropper.getState() instanceof Dropper) {
                Dropper d = (Dropper) dropper.getState();
                if(((ItemFrame) e.getRightClicked()).getItem().getType()!=Material.AIR) { //If there's already something in the item frame, cancel!
                    e.setCancelled(true);
                    return;
                }
                new BukkitRunnable() {
                    public void run() {
                        ItemStack item = ((ItemFrame) e.getRightClicked()).getItem();
                        Dropper d = (Dropper) dropper.getState();
                        d.setCustomName("Autocrafter"); //Rename it to autocrafter to make this clear to the player.
                        d.update();
                        instance.getDropperRegistry().create(d.getLocation(), item);
                        instance.getDropperRegistry().checkDropper(d.getLocation(), e.getPlayer());
                    }
                }.runTaskLater(instance, 1); //Wait a second for the item to be put into the frame.
            }
        }
    }
}
