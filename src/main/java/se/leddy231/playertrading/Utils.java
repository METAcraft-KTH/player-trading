package se.leddy231.playertrading;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;

public class Utils {
    public static String posToString(BlockPos pos) {
        return "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
    }

    public static void sendMessage(PlayerEntity player, String text) {
        player.sendMessage(new LiteralText(text), false);
    }

    public static void sendToast(PlayerEntity player, String text) {
        player.sendMessage(new LiteralText(text), true);
    }
    
    public static boolean canStacksCombine(ItemStack first, ItemStack second) {
        if (first.isEmpty() || second.isEmpty()) {
            return true;
        }
        Item item = first.getItem();
        return second.isOf(item) && ItemStack.areNbtEqual(first, second)
                && first.getCount() + second.getCount() <= item.getMaxCount();
    }

    // /!\ Assumes canStacksCombine is true
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

    public static boolean canPullFromInventory(ItemStack stack, Inventory inventory) {
        if (stack.isEmpty())
            return true;
        if (inventory == null)
            return false;

        int count = stack.getCount();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack current = inventory.getStack(i);
            if (current.isOf(stack.getItem()) && ItemStack.areNbtEqual(current, stack)) {
                count -= current.getCount();
            }
            if (count <= 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean tryPullFromInventory(ItemStack stack, Inventory inventory) {
        List<Integer> slotsToClear = new ArrayList<>();
        if (stack.isEmpty())
            return true;
        if (inventory == null)
            return false;

        int amountToPull = stack.getCount();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack current = inventory.getStack(i);
            if (current.isOf(stack.getItem()) && ItemStack.areNbtEqual(current, stack)) {

                int currentAmount = current.getCount();
                if (currentAmount <= amountToPull) {
                    amountToPull -= currentAmount;
                    slotsToClear.add(i);
                    if (amountToPull == 0)
                        break;
                } else {
                    currentAmount -= amountToPull;
                    inventory.setStack(i, new ItemStack(stack.getItem(), currentAmount));
                    amountToPull = 0;
                    break;
                }

            }
        }
        if (amountToPull == 0) {
            for (Integer i : slotsToClear) {
                inventory.setStack(i, ItemStack.EMPTY);
            }
            return true;
        }
        return false;
    }
}
