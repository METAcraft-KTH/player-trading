package se.leddy231.playertrading.shop;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ShopConfigContainer extends SimpleContainer implements MenuProvider {

    public boolean menuOpen = false;

    public Shop shop;

    public ShopConfigContainer(Shop shop) {
        super(27);
        this.shop = shop;
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Shop Config Menu");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        menuOpen = true;
        return new ShopConfigMenu(i, inventory, this);
    }


    public ShopTradeOffer getOfferFromIndex(int offerIndex) {
        var index = offerIndex * 3;
        ItemStack first = getItem(index).copy();
        ItemStack second = getItem(index + 1).copy();
        ItemStack result = getItem(index + 2).copy();

        if (first.isEmpty() || result.isEmpty()) {
            return null;
        }

        return ShopTradeOffer.valid(first, second, result, offerIndex);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        shop.entity.setChanged();
    }
}
