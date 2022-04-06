package com.aeltumn.autocraft;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Nameable;
import org.bukkit.block.*;
import org.bukkit.entity.Entity;
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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class CreationListener implements Listener {
    /**
     * Returns true if this block is a valid block and optionally if it's seen as an autocrafter.
     *
     * @param existing Only return true if this block is also already an autocrafter.
     */
    public static boolean isValidBlock(final Block bl, boolean existing) {
        //If the block is not any of the allowed states.
        if ((!ConfigFile.allowDispensers() || !(bl.getState() instanceof Dispenser)) &&
                (!ConfigFile.allowChests() || !(bl.getState() instanceof Chest)) &&
                !(bl.getState() instanceof Dropper))
            return false;

        //Test if we can find an autocrafter on this block if applicable.
        return !existing || AutomatedCrafting.INSTANCE.getCrafterRegistry().isAutocrafter(bl);
    }

    //This method specifically is needed because when droppers put the item directly into the neighbouring container the BlockDispenseEvent is not fired.
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDispense(final InventoryMoveItemEvent e) {
        //Autocrafters can't drop items normally. This is to avoid dispensing ingredients when powered.
        if (e.getSource().getHolder() instanceof Container) {
            Block bl = ((Container) e.getSource().getHolder()).getBlock();
            if (isValidBlock(bl, true)) {
                e.setCancelled(true);
                if (ConfigFile.craftOnRedstonePulse()) {
                    AutomatedCrafting.INSTANCE.getCrafterRegistry().tick(bl);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDispense(final BlockDispenseEvent e) {
        //Autocrafters can't drop items normally. This is to avoid dispensing ingredients when powered.
        Block bl = e.getBlock();
        if (isValidBlock(bl, true)) {
            e.setCancelled(true);
            if (ConfigFile.craftOnRedstonePulse()) {
                Bukkit.getScheduler().runTask(AutomatedCrafting.INSTANCE, () -> AutomatedCrafting.INSTANCE.getCrafterRegistry().tick(bl));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCreate(final HangingPlaceEvent e) {
        Block bl = e.getEntity().getLocation().getBlock().getRelative(e.getEntity().getAttachedFace());
        if (isValidBlock(bl, false))
            AutomatedCrafting.INSTANCE.getCrafterRegistry().create(bl.getLocation(), e.getPlayer(), null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(final BlockBreakEvent e) {
        //Destroying the item frame break the autocrafter.
        breakCrafter(e.getBlock(), true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDestroy(final HangingBreakEvent e) {
        //Destroying the item frame break the autocrafter.
        destroyCrafter(e.getEntity(), false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStealItem(final EntityDamageByEntityEvent e) {
        //Stealing the item from the item frame destroys the autocrafter.
        destroyCrafter(e.getEntity(), true);
    }

    private void destroyCrafter(final Entity itemFrame, final boolean clean) {
        if (!(itemFrame instanceof ItemFrame)) return;
        final Block bl = itemFrame.getLocation().getBlock().getRelative(((ItemFrame) itemFrame).getAttachedFace());
        breakCrafter(bl, clean);
    }

    private void breakCrafter(final Block bl, final boolean clean) {
        if (isValidBlock(bl, true)) {
            AutomatedCrafting.INSTANCE.getCrafterRegistry().destroy(bl.getLocation());

            //Clean should be true when the item is removed from the item frame. (can actually be true at all times but we don't need to update droppers randomly if you're placing down item frames, could break redstone)
            if (clean) {
                BlockState state = bl.getState();
                ((Nameable) state).setCustomName(null);
                state.update();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onClickItemFrame(final PlayerInteractEntityEvent e) {
        // ignore clicking non-item frames
        if (!(e.getRightClicked() instanceof ItemFrame)) {
            return;
        }

        // ignore clicking whilst holding nothing
        ItemStack heldItem = e.getHand() == EquipmentSlot.HAND ? e.getPlayer().getInventory().getItemInMainHand() :
                e.getPlayer().getInventory().getItemInOffHand();
        if (heldItem == null || heldItem.getType() == Material.AIR) {
            return;
        }

        Block bl = e.getRightClicked().getLocation().getBlock().getRelative(((ItemFrame) e.getRightClicked()).getAttachedFace());
        if (isValidBlock(bl, false)) {
            //If there's already something in the item frame, cancel!
            //This prevents rotating the item in the item frame.
            if (((ItemFrame) e.getRightClicked()).getItem().getType() != Material.AIR) {
                e.setCancelled(true);
                return;
            }
            //Wait a second for the item to be put into the frame.
            new BukkitRunnable() {
                public void run() {
                    ItemStack item = ((ItemFrame) e.getRightClicked()).getItem();
                    AutomatedCrafting.INSTANCE.getCrafterRegistry().create(bl.getLocation(), e.getPlayer(), item);

                    //Only rename if we have a valid item that we can craft in there.
                    if (AutomatedCrafting.INSTANCE.getCrafterRegistry().checkBlock(bl.getLocation(), e.getPlayer())) {
                        //The block is named autocrafter is it has an item frame AND there's an item in the item frame. If the item frame is empty the name should be reset.
                        //Rename it to autocrafter to make this clear to the player.
                        BlockState state = bl.getState();
                        ((Nameable) state).setCustomName("Autocrafter");
                        state.update();
                    }
                }
            }.runTaskLater(AutomatedCrafting.INSTANCE, 1);
        }
    }
}
