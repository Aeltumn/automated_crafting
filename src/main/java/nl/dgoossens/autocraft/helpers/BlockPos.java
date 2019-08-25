package nl.dgoossens.autocraft.helpers;

import nl.dgoossens.autocraft.AutomatedCrafting;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

/**
 * A representation of a block's
 * position in the world.
 *
 */
public class BlockPos {
    private int x, y, z;
    private String world;

    public BlockPos(Block l) {
        x=l.getX(); y=l.getY(); z=l.getZ();
        world=l.getWorld().getName();
    }

    /**
     * Get the bukkit location corresponding to the
     * block position.
     */
    public Location getLocation() {
        if(Bukkit.getWorld(world)==null) return null;
        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof BlockPos) {
            BlockPos el = (BlockPos) obj;
            return el.x==x && el.y==y && el.z==z && (el.world==null || el.world.equals(world));
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return AutomatedCrafting.GSON.toJson(this);
    }
}
