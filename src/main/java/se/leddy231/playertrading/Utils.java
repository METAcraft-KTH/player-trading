package se.leddy231.playertrading;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static String posToString(BlockPos pos) {
        return "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
    }

    public static void sendMessage(Player player, Component component) {
        player.displayClientMessage(component, false);
    }

    public static void sendToast(Player player, Component text) {
        //lösa
    }

    public static boolean canStacksCombine(ItemStack first, ItemStack second) {
        if (first.isEmpty() || second.isEmpty()) {
            return true;
        }
        Item item = first.getItem();
        return ItemStack.isSameItemSameComponents(first, second)
                && first.getCount() + second.getCount() <= first.getMaxStackSize();
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
        return second.is(item) && ItemStack.isSameItemSameComponents(first, second) && first.getCount() >= second.getCount();
    }

    public static ItemStack subtract(ItemStack first, ItemStack second) {
        if (second.isEmpty()) {
            return first.copy();
        }
        ItemStack ret = first.copy();
        ret.setCount(first.getCount() - second.getCount());
        return ret;
    }

    public static boolean canPutInInventory(ItemStack stack, Container inventory) {
        return inventoryIteration(stack, inventory, false);
    }

    public static boolean tryPutInInventory(ItemStack stack, Container inventory) {
        return inventoryIteration(stack, inventory, true);
    }

    private static boolean inventoryIteration(ItemStack stack, Container inventory, boolean putItemIn) {
        if (stack.isEmpty()) return true;
        if (inventory == null) return false;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack current = inventory.getItem(i);
            if (Utils.canStacksCombine(current, stack)) {
                if (putItemIn) {
                    current = Utils.combine(current, stack);
                    inventory.setItem(i, current);
                }
                return true;
            }
        }
        return false;
    }

    public static boolean canPullFromInventory(ItemStack stack, Container inventory) {
        if (stack.isEmpty()) return true;
        if (inventory == null) return false;

        int count = stack.getCount();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack current = inventory.getItem(i);
            if (current.is(stack.getItem()) && ItemStack.isSameItemSameComponents(current, stack)) {
                count -= current.getCount();
            }
            if (count <= 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean tryPullFromInventory(ItemStack stack, Container inventory) {
        List<Integer> slotsToClear = new ArrayList<>();
        if (stack.isEmpty()) return true;
        if (inventory == null) return false;

        int amountToPull = stack.getCount();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack current = inventory.getItem(i);
            if (current.is(stack.getItem()) && ItemStack.isSameItemSameComponents(current, stack)) {

                int currentAmount = current.getCount();
                if (currentAmount <= amountToPull) {
                    amountToPull -= currentAmount;
                    slotsToClear.add(i);
                    if (amountToPull == 0) break;
                } else {
                    current.shrink(amountToPull);
                    amountToPull = 0;
                    break;
                }

            }
        }
        if (amountToPull == 0) {
            for (Integer i : slotsToClear) {
                inventory.setItem(i, ItemStack.EMPTY);
            }
            return true;
        }
        return false;
    }

    public static int emptySlotsInContainer(Container container) {
        int count = 0;
        for (int i = 0; i < container.getContainerSize(); i++) {
            var stack = container.getItem(i);
            if (stack.isEmpty()) {
                count++;
            }
        }
        return count;
    }
}
