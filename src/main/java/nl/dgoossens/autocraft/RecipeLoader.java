package nl.dgoossens.autocraft;

import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import nl.dgoossens.autocraft.compat.CustomCraftingCompat;
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
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class RecipeLoader {
    private final AutomatedCrafting instance;
    private Set<String> loadedFilenames = new HashSet<>();
    private Set<Recipe> loadedRecipes = new HashSet<>();
    private FileSystem fileSystem;

    public RecipeLoader(final AutomatedCrafting inst) {
        instance=inst;
        //We load the recipes once on start, this is all we do on 1.12.
        new BukkitRunnable() {
            public void run() {
                reload(null);
            }
        }.runTaskAsynchronously(instance);
    }

    /**
     * Get a list containing the names of all files that were loaded, includes .json!
     * For example:
     * [ acacia_boat.json, iron_ingot.json, furnace.json ]
     */
    public Set<String> getLoadedFileNames() { return loadedFilenames; }

    /**
     * Get all recipes that were loaded.
     * These recipes are always shapeless or shaped as we ignore furnace, stonecutter,
     * loom, etc. recipes.
     */
    public Set<Recipe> getLoadedRecipes() { return loadedRecipes; }

    /**
     * Get all recipes that will create the given itemstack. If the given itemstack
     * has a displayname and the recipe result does not the recipe will not be returned!
     */
    public Set<Recipe> getRecipesFor(final ItemStack item) {
        return loadedRecipes.parallelStream()
                .filter(f -> f.getResult().isSimilar(item))
                .collect(Collectors.toSet());
    }

    /**
     * Get a list of all ingredients that this recipe uses.
     * Every JsonElement can be either of two things:
     *   - A JsonItem (use AutomatedCrafting.GSON_ITEM.fromJson(jsonElement, JsonItem.class)) to get it.
     *   - A JsonArray of JsonItem's (forEach() through the list and then use AutomatedCrafting.GSON_ITEM.fromJson(jsonElement, JsonItem.class)) on each element)
     */
    public List<ItemStack> getIngredients(final Recipe recipe) {
        if(recipe==null) return new ArrayList<>();
        List<ItemStack> ret = new ArrayList<>();
        if(recipe.getType().equalsIgnoreCase("crafting_shaped")) {
            for(String s : recipe.getPattern()) {
                for(char c : s.toCharArray())
                    if(recipe.getKeys().containsKey(c))
                        ret.addAll(recipe.getKeys().get(c));
            }
        } else if(recipe.getType().equalsIgnoreCase("crafting_shapeless"))
            ret.addAll(recipe.getIngredients());
        return ret;
    }

    /**
     * Reloads all recipes from the minecraft jar and otherwise from bukkit.
     */
    protected void reload(final CommandSender listener) {
        if(listener!=null) listener.sendMessage("Reloading recipes...");
        instance.getLogger().info("Reloading recipes...");
        loadedRecipes.clear();
        loadedFilenames.clear();

        long t = System.currentTimeMillis();
        //Load from Minecraft's assets
        try {
            if(fileSystem == null) {
                URI uri = Bukkit.class.getResource("/assets/.mcassetsroot").toURI();
                fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
            }
            Path path = fileSystem.getPath("/"+(MinecraftVersion.get().atLeast(MinecraftVersion.THIRTEEN) ? "data" : "assets")+"/minecraft/recipes");
            searchFolder(path);
        } catch(Exception x) {
            x.printStackTrace();
        }

        //Load recipes from bukkit, skip any that we already know.
        //In 1.14 we backup load them from bukkit because bukkit decided to allow us to fetch the alternate recipe choices finally. (but tags are handled awfully)
        Iterator<org.bukkit.inventory.Recipe> it = Bukkit.recipeIterator();
        while(it.hasNext()) {
            org.bukkit.inventory.Recipe bukkitRecipe = it.next();
            if(bukkitRecipe instanceof Keyed) {
                if(!loadedFilenames.contains(((Keyed) bukkitRecipe).getKey().getKey()+".json")) {
                    //Have we already loaded it?
                    Recipe r = new Recipe(bukkitRecipe);
                    if(!r.getType().equalsIgnoreCase("crafting_shaped") &&
                            !r.getType().equalsIgnoreCase("crafting_shapeless")) continue; //We don't want the others!

                    loadedFilenames.add(((Keyed) bukkitRecipe).getKey().getKey()+".json");
                    loadedRecipes.add(r);
                }
            }
        }

        //Custom compatibility
        if(Bukkit.getPluginManager().isPluginEnabled("CustomCrafting"))
            new CustomCraftingCompat().load(loadedRecipes);

        if(listener!=null) listener.sendMessage("Finished reloading "+loadedRecipes.size()+" recipes, took "+(System.currentTimeMillis()-t)+" ms...");
        instance.getLogger().info("Finished reloading "+loadedRecipes.size()+" recipes, took "+(System.currentTimeMillis()-t)+" ms...");
    }

    /**
     * Automatically walks through the folder at the path
     * and loads all files in it.
     */
    private void searchFolder(final Path path) throws Exception {
        if(path==null) return;
        Files.walk(path).forEach(this::loadFile);
    }

    /**
     * Loads a single file at a path.
     * This is passed as a Path instead of a File because
     * we also use this to load .json files as resources from
     * within the jar if it's 1.12 and we're loading the recipes
     * from the /assets/ directory.
     */
    private void loadFile(final Path file) {
        if(!file.toString().endsWith(".json")) return;
        if(loadedFilenames.contains(file.getFileName().toString())) return; //Don't load the same filename twice! (allows overwriting)
        loadedFilenames.add(file.getFileName().toString());
        try {
            final BufferedReader bufferedReader = new BufferedReader(Files.newBufferedReader(file));
            final JsonReader reader = new JsonReader(bufferedReader);
            loadReader(reader);
        } catch(Exception x) {
            x.printStackTrace();
        }
    }

    /**
     * Loads a single recipe from a JsonReader.
     */
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
