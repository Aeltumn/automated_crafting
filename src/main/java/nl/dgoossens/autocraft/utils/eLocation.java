package nl.dgoossens.autocraft.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class eLocation {
    private int x, y, z;
    private String world;

    public eLocation(Block l) {
        x=l.getX(); y=l.getY(); z=l.getZ();
        world=l.getWorld().getName();
    }

    public Location getLocation() {
        if(Bukkit.getWorld(world)==null) return null;
        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof eLocation) {
            eLocation el = (eLocation) obj;
            return el.x==x && el.y==y && el.z==z && el.world.equalsIgnoreCase(world);
        }
        return super.equals(obj);
    }
}
