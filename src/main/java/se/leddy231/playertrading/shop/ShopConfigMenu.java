package se.leddy231.playertrading.shop;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class ShopConfigMenu extends ChestMenu {
    public ShopConfigContainer configContainer;
    private int quickCraftSlot = -999;

    ShopConfigMenu(int containerId, Inventory playerInventory, ShopConfigContainer configContainer) {
        super(MenuType.GENERIC_9x3, containerId, playerInventory, configContainer, 3);
        this.configContainer = configContainer;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        configContainer.menuOpen = false;
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput ContainerInput, Player player) {
        var isConfigSlots = slotId >= 0 && slotId < getContainer().getContainerSize();

        // Clear ghost item
        if (ContainerInput == ContainerInput.QUICK_MOVE && isConfigSlots) {
            getSlot(slotId).set(ItemStack.EMPTY);
            return;
        }
        if (ContainerInput == ContainerInput.QUICK_CRAFT) {

            if (slotId == -999) {
                if (button == 2) {
                    clicked(quickCraftSlot, 0, ContainerInput.PICKUP, player);
                }
                if (button == 6) {
                    clicked(quickCraftSlot, 1, ContainerInput.PICKUP, player);
                }
            } else {
                quickCraftSlot = slotId;
            }
        }
        if (ContainerInput == ContainerInput.PICKUP) {
            if (!isConfigSlots) {
                super.clicked(slotId, button, ContainerInput, player);
                return;
            }
            ItemStack carried = getCarried();
            ItemStack targetItem = getSlot(slotId).getItem();
            if (carried.isEmpty()) {
                var newCount = Math.min(targetItem.getCount() + 1, targetItem.getMaxStackSize());
                if (button == 1) {
                    newCount = Math.max(targetItem.getCount() - 1, 1);
                }
                targetItem.setCount(newCount);
                getSlot(slotId).setChanged();
                return;
            }
            if (!ItemStack.isSameItemSameComponents(carried, targetItem)) {
                var newTarget = carried.copy();
                if (button == 1) {
                    newTarget.setCount(1);
                }
                getSlot(slotId).set(newTarget);
            } else {
                var newCount = Math.min(targetItem.getCount() + carried.getCount(),
                                        targetItem.getMaxStackSize()
                );
                if (button == 1) {
                    newCount = Math.min(targetItem.getCount() + 1, targetItem.getMaxStackSize());
                }
                targetItem.setCount(newCount);
                getSlot(slotId).setChanged();
            }
        }
    }
}
