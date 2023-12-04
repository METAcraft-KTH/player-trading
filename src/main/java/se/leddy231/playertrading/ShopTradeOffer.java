package se.leddy231.playertrading;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

public class ShopTradeOffer extends MerchantOffer {
    public int shopBarrelInventoryIndex;
    public boolean valid;
    public String invalidReason;

    private ShopTradeOffer(
            ItemStack first, ItemStack second, ItemStack result, boolean valid, int index, String invalidReason
    ) {
        super(first, second, result, 0, valid ? 1 : 0, 0, 1, 0);
        this.valid = valid;
        this.invalidReason = invalidReason;
        shopBarrelInventoryIndex = index;
    }

    public static ShopTradeOffer valid(ItemStack first, ItemStack second, ItemStack result, int index) {
        return new ShopTradeOffer(first, second, result, true, index, "");
    }

    public static ShopTradeOffer invalid(
            ItemStack first, ItemStack second, ItemStack result, int index, String invalidReason
    ) {
        return new ShopTradeOffer(first, second, result, false, index, invalidReason);
    }

    public ItemStack getFirst() {
        return getCostA();
    }

    public ItemStack getSecond() {
        return getCostB();
    }

    public ShopTradeOffer asUsed() {
        return ShopTradeOffer.invalid(
                getFirst(),
                getSecond(),
                getResult(),
                shopBarrelInventoryIndex,
                "this trade is old (someone has the trade screen still open)"
        );
    }
}
