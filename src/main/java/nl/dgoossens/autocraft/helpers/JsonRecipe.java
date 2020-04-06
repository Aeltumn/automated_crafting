package nl.dgoossens.autocraft.helpers;

import com.google.gson.JsonElement;
import nl.dgoossens.autocraft.api.CraftingRecipe;
import nl.dgoossens.autocraft.api.RecipeType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonRecipe implements CraftingRecipe {
    private String type;
    private String group;
    private String[] pattern;
    private List<JsonElement> ingredients; //JsonItem or JsonArray of JsonItem
    private Map<String, JsonElement> key; //JsonItem or JsonArray of JsonItem
    private JsonItem result;

    public RecipeType getType() {
        String t = type.startsWith("minecraft:") ? type.substring("minecraft:".length()) : type;
        for(RecipeType type : RecipeType.values()) {
            if(type.getId().equals(t)) return type;
        }
        return RecipeType.UNKNOWN;
    }

    @Override
    public boolean containsRequirements(Inventory inv) {
        switch(getType()) {
            case SHAPED:

                break;
            case SHAPELESS:

                break;
        }
        return false;
    }

    @Override
    public ArrayList<ItemStack> takeMaterials(Inventory inv) {
        return null; //TODO impl
    }

    @Override
    public boolean creates(ItemStack stack) {
        if(result == null) return false;
        return result.isSimilar(stack);
    }

    @Override
    public ItemStack getResultDrop() {
        return result.toStack();
    }
}
