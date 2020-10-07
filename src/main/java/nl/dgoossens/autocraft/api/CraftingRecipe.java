package nl.dgoossens.autocraft.api;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import nl.dgoossens.autocraft.helpers.ReflectionHelper;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public interface CraftingRecipe {
    Class<?> craftItemStack = ReflectionHelper.getCraftBukkitClass("inventory.CraftItemStack").orElse(null);
    Class<?> iMaterial = ReflectionHelper.getNMSClass("IMaterial").orElse(null);
    Class<?> itemStack = ReflectionHelper.getNMSClass("ItemStack").orElse(null);
    Class<?> item = ReflectionHelper.getNMSClass("Item").orElse(null);
    Method asNMSCopyMethod = ReflectionHelper.getMethod(craftItemStack, "asNMSCopy", ItemStack.class).orElse(null);
    Method asCraftMirrorMethod = ReflectionHelper.getMethod(craftItemStack, "asCraftMirror", itemStack).orElse(null);
    Field itemField = ReflectionHelper.getField(itemStack, "item").orElse(null);
    Field craftingResultField = ReflectionHelper.getField(item, "craftingResult").orElse(null);
    Constructor<?> iMaterialConstructor = ReflectionHelper.getConstructor(itemStack, iMaterial).orElse(null);
    Constructor<?> itemConstructor = ReflectionHelper.getConstructor(itemStack, item).orElse(null);

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
     * <p>
     * Returns null if nothing/air is the container item.
     */
    default ItemStack getContainerItem(ItemStack input) {
        try {
            Object nmsStack = asNMSCopyMethod.invoke(null, input);
            Object item = ReflectionHelper.getFieldValue(nmsStack, itemField).orElse(null);
            Object craftingResult = ReflectionHelper.getFieldValue(item, craftingResultField).orElse(null);
            // since 1.15 we have IMaterial so we need to use a different constructor
            Object stack = iMaterial != null ? iMaterialConstructor.newInstance(craftingResult) : itemConstructor.newInstance(craftingResult);
            ItemStack i = (ItemStack) asCraftMirrorMethod.invoke(null, stack);
            if (i == null || i.getType() == Material.AIR) return null;
            else return i;
        } catch (Exception x) {
            x.printStackTrace();
        }
        return null;
    }
}
