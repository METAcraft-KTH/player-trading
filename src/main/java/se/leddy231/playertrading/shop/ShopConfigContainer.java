package se.leddy231.playertrading.shop;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
        var title = Component.translatableWithFallback(
                "gui.playertrading.config_menu",
                "Shop Config Menu"
        );

        // Font magic
        // a = move cursor by -8
        //     (back by 8, aligns to vanilla GUI corner)
        // b = shop_config_menu.png
        //     (width: 176, so moves cursor by 176)
        // c = move cursor by -169
        //     (back by 169 = -8 + 176 + 1, resets cursor.
        //      + 1 is to count space between characters)
        //
        // See: https://github.com/METAcraft-KTH/resource-pack
        //
        var fontMagic = Component.literal("abc").withStyle(style ->
            style
                .withFont(new ResourceLocation("playertrading", "shop_config_menu"))
                .withColor(ChatFormatting.WHITE)
        );

        return Component.empty().append(fontMagic).append(title);
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
