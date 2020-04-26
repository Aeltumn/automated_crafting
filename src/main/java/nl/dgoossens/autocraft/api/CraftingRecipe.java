package nl.dgoossens.autocraft.api;

import nl.dgoossens.autocraft.helpers.ReflectionHelper;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public interface CraftingRecipe {
    static final Class<?> craftItemStack = ReflectionHelper.getOptionalBukkitClass("inventory.CraftItemStack").orElse(null);
    static final Class<?> iMaterial = ReflectionHelper.getOptionalNMSClass("IMaterial").orElse(null);
    static final Class<?> itemStack = ReflectionHelper.getOptionalNMSClass("ItemStack").orElse(null);
    static final Class<?> item = ReflectionHelper.getOptionalNMSClass("Item").orElse(null);

    /**
     * The type of this recipe.
     */
    RecipeType getType();

    /**
     * Return true if this recipe can create this item stack.
     * NBT of input item matters!
     */
    boolean creates(ItemStack stack);

    /**
     * Get the item dropped if the crafting was successfull.
     */
    ItemStack getResultDrop();

    /**
     * Check if an inventory contains the required items.
     */
    boolean containsRequirements(Inventory inv);

    /**
     * Take the materials from the inventory that are required for this craft.
     * Put resulting container items into the returned array list.
     */
    ArrayList<ItemStack> takeMaterials(Inventory inv);

    /**
     * Get the 'container item' which is the item
     * left in the crafting area after an item is used
     * in a crafting recipe.
     *
     * Returns null if nothing/air is the container item.
     */
    public default ItemStack getContainerItem(ItemStack input) {
        try {
            Object nmsStack = craftItemStack.getMethod("asNMSCopy", ItemStack.class).invoke(null, input);
            Object craftingResult = ReflectionHelper.getField(item, ReflectionHelper.getField(itemStack, nmsStack, "item"), "craftingResult");
            // since 1.15 we have IMaterial
            Object stack = iMaterial != null ? itemStack.getConstructor(iMaterial).newInstance(craftingResult) : itemStack.getConstructor(item).newInstance(craftingResult);
            ItemStack i = (ItemStack) craftItemStack.getMethod("asCraftMirror", itemStack).invoke(null, stack);
            if(i == null || i.getType() == Material.AIR) return null;
            else return i;
        } catch(Exception x) {
            x.printStackTrace();
        }
        return null;
    }
}
