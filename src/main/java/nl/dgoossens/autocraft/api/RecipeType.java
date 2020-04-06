package nl.dgoossens.autocraft.api;

/**
 * The possible shapes of a crafting recipe.
 */
public enum RecipeType {
    SHAPED("crafting_shaped"),
    SHAPELESS("crafting_shapeless"),
    UNKNOWN("")
    ;

    private String id;
    RecipeType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
