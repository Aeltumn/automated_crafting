package nl.dgoossens.autocraft.helpers;

import com.google.gson.JsonElement;
import nl.dgoossens.autocraft.api.CraftingRecipe;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class JsonRecipe implements CraftingRecipe {
    private String type;
    private String group;
    private String[] pattern;
    private List<JsonElement> ingredients; //JsonItem or JsonArray of JsonItem
    private Map<String, JsonElement> key; //JsonItem or JsonArray of JsonItem
    private JsonItem result;

    public String getType() {
        return type.startsWith("minecraft:") ? type.substring("minecraft:".length()) : type;
    }

    @Override
    public boolean creates(ItemStack stack) {
        if(result == null) return false;
        return result.isSimilar(stack);
    }
}
