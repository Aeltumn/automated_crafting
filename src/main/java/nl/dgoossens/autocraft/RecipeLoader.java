package nl.dgoossens.autocraft;

import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import nl.dgoossens.autocraft.helpers.MinecraftVersion;
import nl.dgoossens.autocraft.helpers.Recipe;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class RecipeLoader {
    private final AutomatedCrafting instance;
    private Set<String> loadedFilenames = new HashSet<>();
    private Set<Recipe> loadedRecipes = new HashSet<>();

    public Set<Recipe> getLoadedRecipes() { return loadedRecipes; }
    public Set<Recipe> getRecipesFor(final ItemStack item) {
        return loadedRecipes.parallelStream()
                .filter(f -> f.getResult().getStack().isSimilar(item))
                .collect(Collectors.toSet());
    }
    public List<JsonElement> getIngredients(final Recipe recipe) {
        if(recipe==null) return new ArrayList<>();
        List<JsonElement> ret = new ArrayList<>();
        if(recipe.getType().equalsIgnoreCase("crafting_shaped")) {
            for(String s : recipe.getPattern()) {
                for(char c : s.toCharArray())
                    ret.add(recipe.getKeys().get(c));
            }
        } else if(recipe.getType().equalsIgnoreCase("crafting_shapeless"))
            ret.addAll(recipe.getIngredients());
        return ret;
    }

    public RecipeLoader(final AutomatedCrafting inst) {
        instance=inst;
        //We load the recipes once on start, this is all we do on 1.12.
        new BukkitRunnable() {
            public void run() {
                reload(null);
            }
        }.runTaskAsynchronously(instance);
    }

    protected void reload(final CommandSender listener) {
        if(listener!=null) listener.sendMessage("Reloading recipes...");
        instance.getLogger().info("Reloading recipes...");
        loadedRecipes.clear();
        loadedFilenames.clear();

        long t = System.currentTimeMillis();
        //Load our custom recipes first to allow the to overwrite!
        File recipeFolder = new File(instance.getDataFolder(), "recipes");
        recipeFolder.mkdirs(); //Create folders  if they don't exist yet.
        try {
            searchFolder(recipeFolder.toPath());
        } catch(Exception x) { x.printStackTrace(); }

        if(MinecraftVersion.get()==MinecraftVersion.TWELVE) {
            //Fallback loading from assets in 1.12
            try {
                URI uri = Bukkit.class.getResource("/assets/.mcassetsroot").toURI();
                Path path = FileSystems.newFileSystem(uri, Collections.emptyMap()).getPath("/assets/minecraft/recipes");
                searchFolder(path);
            } catch(Exception x) { x.printStackTrace(); }
        }

        //Load recipes from bukkit, skip any that we already know.
        //In 1.14 we only load them from bukkit because bukkit decided to allow us to fetch the alternate recipe choices finally.
        Iterator<org.bukkit.inventory.Recipe> it = Bukkit.recipeIterator();
        while(it.hasNext()) {
            org.bukkit.inventory.Recipe bukkitRecipe = it.next();
            if(bukkitRecipe instanceof Keyed) {
                if(!loadedFilenames.contains(((Keyed) bukkitRecipe).getKey().getKey()+".json")) {
                    //Have we already loaded it?
                    //System.out.println("[DEBUG] Backup loaded "+((Keyed) bukkitRecipe).getKey().getKey()+".json");
                    Recipe r = new Recipe(bukkitRecipe);
                    if(!r.getType().equalsIgnoreCase("crafting_shaped") &&
                            !r.getType().equalsIgnoreCase("crafting_shapeless")) continue; //We don't want the others!
                    loadedFilenames.add(((Keyed) bukkitRecipe).getKey().getKey()+".json");
                    loadedRecipes.add(r);
                }
            }
        }

        if(listener!=null) listener.sendMessage("Finished reloading "+loadedRecipes.size()+" recipes, took "+(System.currentTimeMillis()-t)+" ms...");
        instance.getLogger().info("Finished reloading "+loadedRecipes.size()+" recipes, took "+(System.currentTimeMillis()-t)+" ms...");
    }

    private void searchFolder(final Path path) throws Exception {
        if(path==null) return;
        Files.walk(path).forEach(this::loadFile);
    }

    private void loadFile(final Path file) {
        if(!file.toString().endsWith(".json")) return;
        if(loadedFilenames.contains(file.getFileName().toString())) return; //Don't load the same filename twice! (allows overwriting)
        loadedFilenames.add(file.getFileName().toString());
        //System.out.println("[DEBUG] Loaded "+file.getFileName().toString());
        try {
            final BufferedReader bufferedReader = new BufferedReader(Files.newBufferedReader(file));
            final JsonReader reader = new JsonReader(bufferedReader);
            loadReader(reader);
        } catch(Exception x) {
            x.printStackTrace();
        }
    }

    private void loadReader(final JsonReader reader) throws Exception {
        if(!reader.hasNext() || reader.peek() != JsonToken.BEGIN_OBJECT) return;
        Recipe r = AutomatedCrafting.GSON.fromJson(reader, Recipe.class);
        reader.close();
        //If the type is unknown, don't add it.
        if(!r.getType().equalsIgnoreCase("crafting_shaped") &&
                !r.getType().equalsIgnoreCase("crafting_shapeless")) return;
        loadedRecipes.add(r);
    }
}
