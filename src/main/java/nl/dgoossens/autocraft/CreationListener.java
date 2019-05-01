package nl.dgoossens.autocraft;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dropper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class CreationListener implements Listener {
    private AutomatedCrafting instance;
    public CreationListener(AutomatedCrafting instance) { this.instance=instance; }

    @EventHandler
    public void onCreate(HangingPlaceEvent e) {
        Block dropper = e.getEntity().getLocation().getBlock().getRelative(e.getEntity().getAttachedFace());
        if(dropper.getType().equals(Material.DROPPER)) {
            Dropper d = (Dropper) dropper.getState();
            d.setCustomName("Autocrafter");
            d.update();
            instance.getDropperRegistry().create(d.getLocation(), null);
        }
    }

    @EventHandler
    public void onDestroy(HangingBreakEvent e) {
        Block dropper = e.getEntity().getLocation().getBlock().getRelative(e.getEntity().getAttachedFace());
        instance.getDropperRegistry().destroy(dropper.getLocation());
        if(dropper.getType().equals(Material.DROPPER)) {
            Dropper d = (Dropper) dropper.getState();
            d.setCustomName("Dropper");
            d.update();
        }
    }

    @EventHandler
    public void onClickItemFrame(PlayerInteractEntityEvent e) {
        if(e.getRightClicked().getType().equals(EntityType.ITEM_FRAME)) {
            Block dropper = e.getRightClicked().getLocation().getBlock().getRelative(((ItemFrame) e.getRightClicked()).getAttachedFace());
            if(dropper.getType()== Material.DROPPER) {
                new BukkitRunnable() {
                    public void run() {
                        Dropper d = (Dropper) dropper.getState();
                        ItemStack item = ((ItemFrame) e.getRightClicked()).getItem();
                        instance.getDropperRegistry().create(d.getLocation(), item.getType());
                    }
                }.runTaskLater(instance, 1);
            }
        }
    }
}
