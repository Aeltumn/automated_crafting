package nl.dgoossens.autocraft.compat;

import nl.dgoossens.autocraft.helpers.Recipe;

import java.util.Set;

/**
 * Generic interface from compatibility loaders.
 */
public interface CompatClass {
    void load(final Set<Recipe> loadedRecipes);
}
