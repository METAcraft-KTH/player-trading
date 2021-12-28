package se.leddy231.playertrading;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public class Utils {
    public static String posToString(BlockPos pos) {
        return "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
    }

    public static void sendMessage(PlayerEntity player, String text) {
        player.sendMessage(new LiteralText(text), false);
    }

    /**
     * Send message method that supports formatting
     */
    public static void sendMessage(PlayerEntity player, LiteralText literalText) {
        player.sendMessage(literalText, false);
    }

    public static boolean canStacksCombine(ItemStack first, ItemStack second) {
        if (first.isEmpty() || second.isEmpty()) {
            return true;
        }
        Item item = first.getItem();
        return second.isOf(item) && ItemStack.areNbtEqual(first, second) && first.getCount() + second.getCount() <= item.getMaxCount();
    }
    //  /!\ Assumes canStacksCombine is true
    public static ItemStack combine(ItemStack first, ItemStack second) {
        if (first.isEmpty()) {
            return second.copy();
        }
        if (second.isEmpty()) {
            return first.copy();
        }
        ItemStack ret = first.copy();
        ret.setCount(first.getCount() + second.getCount());
        return ret;
    }

    public static boolean canStacksSubtract(ItemStack first, ItemStack second) {
        if (second.isEmpty()) {
            return true;
        }
        if (first.isEmpty()) {
            return false;
        }
        Item item = first.getItem();
        return second.isOf(item) && ItemStack.areNbtEqual(first, second) && first.getCount() >= second.getCount();
    }

    public static ItemStack subtract(ItemStack first, ItemStack second) {
        if (second.isEmpty()) {
            return first.copy();
        }
        ItemStack ret = first.copy();
        ret.setCount(first.getCount() - second.getCount());
        return ret;
    }

    public static boolean canPutInInventory(ItemStack stack, Inventory inventory) {
        return inventoryIteration(stack, inventory, false);
    }

    public static boolean tryPutInInventory(ItemStack stack, Inventory inventory) {
        return inventoryIteration(stack, inventory, true);
    }

    private static boolean inventoryIteration(ItemStack stack, Inventory inventory, boolean putItemIn) {
        if (stack.isEmpty())
            return true;
        if (inventory == null)
            return false;

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack current = inventory.getStack(i);
            if (Utils.canStacksCombine(current, stack)) {
                if (putItemIn) {
                    current = Utils.combine(current, stack);
                    inventory.setStack(i, current);
                }
                return true;
            }
        }
        return false;
    }

    public static boolean tryPullFromInventory(ItemStack stack, Inventory inventory) {
        if (stack.isEmpty())
            return true;
        if (inventory == null)
            return false;
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack current = inventory.getStack(i);
            if (Utils.canStacksSubtract(current, stack)) {
                current = Utils.subtract(current, stack);
                inventory.setStack(i, current);
                return true;
            }
        }
        return false;
    }
}
