package nl.dgoossens.autocraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import nl.dgoossens.autocraft.utils.eLocation;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Dropper;
import org.bukkit.material.Dispenser;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DropperRegistry {
    private HashMap<eLocation, Material> droppers = new HashMap<>();
    private File file;

    public DropperRegistry(AutomatedCrafting instance) {
        if(!instance.getDataFolder().exists()) instance.getDataFolder().mkdirs();
        file = new File(instance.getDataFolder(), "droppers.json");
        if(file.exists()) {
            try {
                FileReader fr = new FileReader(file);
                JsonReader jr = new JsonReader(fr);
                jr.beginObject();
                Gson g = new GsonBuilder().create();
                while(jr.hasNext()) {
                    String n = jr.nextName();
                    Material mat = n.equals("null") ? null : Material.matchMaterial(n);
                    droppers.put(g.fromJson(jr, eLocation.class), mat);
                }
                jr.endObject();
                jr.close();
                fr.close();
             } catch(Exception x) { x.printStackTrace(); }
        }

        new BukkitRunnable() {
            public void run() {
                int i = droppers.size();
                droppers.keySet().removeIf(d -> {
                    Block block = d.getLocation().getBlock();
                    if(block.getType() != Material.DROPPER) return true;
                    Dropper dropper = (Dropper) block.getState();
                    Material m = droppers.get(d);
                    if(m==null || dropper==null) return false;
                    //System.out.println("Found m was "+m);
                    Iterator<Recipe> recipes = Bukkit.recipeIterator();
                    while(recipes.hasNext()) {
                        Recipe r = recipes.next();
                        if(!r.getResult().getType().equals(m)) continue;
                        Set<ItemStack> items = Stream.of(dropper.getInventory().getContents()).collect(Collectors.toSet());
                        List<ItemStack> ingredients = null;
                        if(r instanceof ShapelessRecipe) ingredients = new ArrayList<>(((ShapelessRecipe) r).getIngredientList());
                        else if(r instanceof ShapedRecipe) ingredients = new ArrayList<>(((ShapedRecipe) r).getIngredientMap().values());
                        if(ingredients==null) continue;
                        List<ItemStack> removed = new ArrayList<>();
                        for(ItemStack ite : ingredients) {
                            if(ite==null) continue;
                            //System.out.println("Found ingredient "+ite.getType()+", amount "+ite.getAmount());
                            if(!dropper.getInventory().contains(ite.getType(), ite.getAmount())) {
                                removed.forEach(r2 -> dropper.getInventory().addItem(r2));
                                return false;
                            }
                            removed.add(ite);
                            int ico = Stream.of(dropper.getInventory().getContents()).filter(f -> f!=null && f.getType().equals(ite.getType())).mapToInt(ItemStack::getAmount).sum();
                            ico -= ite.getAmount();
                            dropper.getInventory().remove(ite.getType());
                            if(ico > 0) {
                                dropper.getInventory().addItem(new ItemStack(ite.getType(), ico % ite.getMaxStackSize()));
                                for(int j = 0; j < ico / 64; j++) dropper.getInventory().addItem(new ItemStack(ite.getType(), ite.getMaxStackSize()));
                            }
                        }
                        Dispenser dispenser = (Dispenser) dropper.getData();
                        Location loc = dropper.getLocation().getBlock().getRelative(dispenser.getFacing()).getLocation();
                        if(loc.getBlock().getState() instanceof Container) {
                            Container c = (Container) loc.getBlock().getState();
                            if(c.getInventory().firstEmpty() != -1 ||
                            Stream.of(c.getInventory().getContents()).anyMatch(f -> f==null || (f.getType().equals(r.getResult().getType()) && f.getAmount() <= f.getMaxStackSize()-r.getResult().getAmount()))) {
                                c.getInventory().addItem(r.getResult());
                                return false;
                            }
                        }
                        loc.getWorld().dropItemNaturally(loc, r.getResult());
                    }
                    return false;
                });
                if(i!=droppers.size()) save();
            }
        }.runTaskTimer(instance, 25, 25);
    }

    public void create(Location l, Material type) {
        eLocation el = new eLocation(l.getBlock());
        droppers.keySet().removeIf(f -> f.equals(el));
        droppers.put(el, type);
        save();
    }
    public void destroy(Location l) { eLocation el = new eLocation(l.getBlock()); droppers.keySet().removeIf(f -> f.equals(el)); save(); }

    private void save() {
        try {
            if(!file.exists()) file.createNewFile();
            FileWriter fw = new FileWriter(file);
            JsonWriter jw = new JsonWriter(fw);
            jw.beginObject();
            for(eLocation d : droppers.keySet()) { jw.name(droppers.get(d)==null ? "null" : droppers.get(d).name()); jw.jsonValue(new GsonBuilder().create().toJson(d)); }
            jw.endObject();
            jw.flush();
            jw.close();
            fw.close();
        } catch(Exception x) { x.printStackTrace(); }
    }
}
