package se.leddy231.playertrading.shop;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

public class ShopTradeOffer extends MerchantOffer {
    public int offerIndex;
    public boolean valid;
    public String invalidReason;

    private ShopTradeOffer(
            ItemStack first, ItemStack second, ItemStack result, boolean valid, int index, String invalidReason
    ) {
        super(first, second, result, 0, valid ? 1 : 0, 0, 1, 0);
        this.valid = valid;
        this.invalidReason = invalidReason;
        offerIndex = index;
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

    public ShopTradeOffer asInvalid(String reason) {
        return ShopTradeOffer.invalid(getFirst(), getSecond(), getResult(), offerIndex, reason);
    }

    public ShopTradeOffer asValid() {
        return ShopTradeOffer.valid(getFirst(), getSecond(), getResult(), offerIndex);
    }
}
