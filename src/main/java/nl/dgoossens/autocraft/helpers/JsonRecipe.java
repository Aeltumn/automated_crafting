package nl.dgoossens.autocraft.helpers;

import nl.dgoossens.autocraft.api.CraftingRecipe;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class JsonRecipe implements CraftingRecipe {
    private String type;
    private String group;
    private String[] pattern;
    private List<JsonItem> ingredients;
    private Map<String, JsonItem> key;
    private JsonItem result;

    public String getType() {
        return type.startsWith("minecraft:") ? type.substring("minecraft:".length()) : type;
    }

    @Override
    public boolean creates(ItemStack stack) {
        return false; //TODO impl!
    }
}
