package nl.dgoossens.autocraft.api;

import java.util.Set;

/**
 * Generic interface from compatibility loaders.
 */
public interface CompatClass {
    /**
     * Load any recipes and pass them back.
     */
    Set<CraftingRecipe> load();
}
