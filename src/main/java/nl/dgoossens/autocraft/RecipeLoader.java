package nl.dgoossens.autocraft;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import nl.dgoossens.autocraft.api.CraftingRecipe;
import nl.dgoossens.autocraft.compat.CustomCraftingCompat;
import nl.dgoossens.autocraft.helpers.JsonRecipe;
import nl.dgoossens.autocraft.helpers.MinecraftVersion;
import nl.dgoossens.autocraft.helpers.BukkitRecipe;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class RecipeLoader {
    private final AutomatedCrafting instance;
    private Set<String> loadedFilenames = new HashSet<>();
    private Set<CraftingRecipe> loadedRecipes = new HashSet<>();
    private FileSystem fileSystem;
    private JsonParser parser;

    public RecipeLoader(final AutomatedCrafting inst) {
        instance = inst;
        parser = new JsonParser();

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
    public Set<String> getLoadedFileNames() {
        return loadedFilenames;
    }

    /**
     * Get all recipes that were loaded.
     * These recipes are always shapeless or shaped as we ignore furnace, stonecutter,
     * loom, etc. recipes.
     */
    public Set<CraftingRecipe> getLoadedRecipes() {
        return loadedRecipes;
    }

    /**
     * Get all recipes that will create the given itemstack. If the given itemstack
     * has a displayname and the recipe result does not the recipe will not be returned!
     */
    public Set<CraftingRecipe> getRecipesFor(final ItemStack item) {
        return loadedRecipes.parallelStream().filter(f -> f.creates(item)).collect(Collectors.toSet());
    }

    /**
     * Reloads all recipes from the minecraft jar and otherwise from bukkit.
     */
    protected void reload(final CommandSender listener) {
        if (listener != null) listener.sendMessage("(Re)loading recipes...");
        instance.getLogger().info("(Re)loading recipes...");
        loadedRecipes.clear();
        loadedFilenames.clear();
        long t = System.currentTimeMillis();

        //Load from Minecraft's assets
        try {
            if (fileSystem == null) {
                URI uri = Bukkit.class.getResource("/assets/.mcassetsroot").toURI();
                try {
                    fileSystem = FileSystems.getFileSystem(uri);
                } catch (Exception x) {
                    fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                }
            }
            Path path = fileSystem.getPath("/" + (MinecraftVersion.get().atLeast(MinecraftVersion.THIRTEEN) ? "data" : "assets") + "/minecraft/recipes");
            searchFolder(path);
        } catch (Exception x) {
            x.printStackTrace();
        }

        //Check if we loaded anything this iteration
        long j = loadedRecipes.size();
        if (j > 0) {
            if (listener != null)
                listener.sendMessage("(Re)loaded " + j + " vanilla recipes, took " + (System.currentTimeMillis() - t) + " ms...");
            instance.getLogger().info("(Re)loaded " + j + " vanilla recipes, took " + (System.currentTimeMillis() - t) + " ms...");
        }

        //Set up for next iteration
        t = System.currentTimeMillis();
        j = loadedRecipes.size();

        //Load recipes from bukkit, skip any that we already know.
        Iterator<org.bukkit.inventory.Recipe> it = Bukkit.recipeIterator();
        while (it.hasNext()) {
            org.bukkit.inventory.Recipe bukkitRecipe = it.next();
            if (bukkitRecipe instanceof Keyed) {
                if (!loadedFilenames.contains(((Keyed) bukkitRecipe).getKey().getKey() + ".json")) {
                    //Have we already loaded it?
                    BukkitRecipe r = new BukkitRecipe(bukkitRecipe);
                    if (!r.getType().equalsIgnoreCase("crafting_shaped") &&
                            !r.getType().equalsIgnoreCase("crafting_shapeless")) continue; //We don't want the others!

                    loadedFilenames.add(((Keyed) bukkitRecipe).getKey().getKey() + ".json");
                    loadedRecipes.add(r);
                    System.out.println("BL: " + ((Keyed) bukkitRecipe).getKey().getKey() + ".json");
                }
            }
        }

        //Check if something was loaded this iteration
        j = loadedRecipes.size() - j;
        if (j > 0) {
            if (listener != null)
                listener.sendMessage("(Re)loaded " + j + " bukkit recipes, took " + (System.currentTimeMillis() - t) + " ms...");
            instance.getLogger().info("(Re)loaded " + j + " bukkit recipes, took " + (System.currentTimeMillis() - t) + " ms...");
        }

        //Set up for next iteration
        t = System.currentTimeMillis();
        j = loadedRecipes.size();

        //Custom compatibility
        if (Bukkit.getPluginManager().isPluginEnabled("CustomCrafting")) {
            if(Bukkit.getPluginManager().getPlugin("CustomCrafting").getDescription().getVersion().startsWith("1.5")) {
                String s = "You are running CustomCrafting v1.5+ which is unfortunately not compatible with AutomatedCrafting. Please use an older version of CustomCrafting.";
                if (listener != null)
                    listener.sendMessage(s);
                instance.getLogger().info(s);
            } else {
                loadedRecipes.addAll(new CustomCraftingCompat().load());
            }
        }

        //Check if something was loaded this iteration
        j = loadedRecipes.size() - j;
        if (j > 0) {
            if (listener != null)
                listener.sendMessage("(Re)loaded " + j + " custom recipes from compatible plugins, took " + (System.currentTimeMillis() - t) + " ms...");
            instance.getLogger().info("(Re)loaded " + j + " custom recipes from compatible plugins, took " + (System.currentTimeMillis() - t) + " ms...");
        }
    }

    /**
     * Automatically walks through the folder at the path
     * and loads all files in it.
     */
    private void searchFolder(final Path path) throws Exception {
        if (path == null) return;
        //We use none match here as it stops the moment one does match. (when we get an exception)
        Files.walk(path).noneMatch(this::loadFile);
    }

    /**
     * Loads a single file at a path.
     * This is passed as a Path instead of a File because
     * we also use this to load .json files as resources from
     * within the jar if it's 1.12 and we're loading the recipes
     * from the /assets/ directory.
     *
     * @return true if an exception occurred
     */
    private boolean loadFile(final Path file) {
        if (!file.toString().endsWith(".json")) return false;
        if (loadedFilenames.contains(file.getFileName().toString()))
            return false; //Don't load the same filename twice! (allows overwriting)
        try {
            final BufferedReader bufferedReader = new BufferedReader(Files.newBufferedReader(file));
            final JsonReader reader = new JsonReader(bufferedReader);
            loadReader(reader);
            reader.close();
            loadedFilenames.add(file.getFileName().toString());
            System.out.println("VL: " + file.getFileName().toString());
        } catch (Exception x) {
            x.printStackTrace();

            AutomatedCrafting.getInstance().getLogger().warning("Failed to load file " + file.getFileName().toString() + "! Here's a printout of said file:");
            //Print file to console
            try {
                final BufferedReader bufferedReader = new BufferedReader(Files.newBufferedReader(file));
                while (bufferedReader.ready())
                    System.out.println(bufferedReader.readLine());
                bufferedReader.close();
            } catch (Exception ignored) {
            }
            return true;
        }
        return false;
    }

    /**
     * Loads a single recipe from a JsonReader.
     */
    private void loadReader(final JsonReader reader) throws Exception {
        JsonElement obj = parser.parse(reader);
        if (!obj.isJsonObject()) return;
        if (!obj.getAsJsonObject().has("type")) return;
        String type = obj.getAsJsonObject().get("type").getAsString();

        //On 1.12 there wasn't a minecraft: prefix yet
        if (type.startsWith("minecraft:")) type = type.substring("minecraft:".length());

        //if it's not shaped or shapeless we don't want it!
        if (!type.equalsIgnoreCase("crafting_shaped") && !type.equalsIgnoreCase("crafting_shapeless")) return;

        //Add to loaded recipes list
        loadedRecipes.add(AutomatedCrafting.GSON.fromJson(obj, JsonRecipe.class));
    }
}
