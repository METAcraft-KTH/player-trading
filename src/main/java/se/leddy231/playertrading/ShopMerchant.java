package se.leddy231.playertrading;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.village.Merchant;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import se.leddy231.playertrading.interfaces.IShopBarrelEntity;
import se.leddy231.playertrading.mixin.MerchantScreenHandlerAccessor;

public class ShopMerchant implements Merchant {
    private static final String SHOP_TITLE = "Barrel shop";
    @Nullable
    public PlayerEntity currentCustomer;
    public IShopBarrelEntity shopEntity;
    // Trades that expire/are used up needs to be kept inte the trades list until
    // the screen is closed
    // Otherwise, the client game crashes
    public ShopTradeOffer[] oldTrades;
    // Ignore inventory refresh while moving thins around in the inventory
    private boolean ignoreRefresh = false;

    public ShopMerchant(IShopBarrelEntity shopEntity) {
        super();
        this.shopEntity = shopEntity;
        clearOldTrades();
    }

    public void clearOldTrades() {
        oldTrades = new ShopTradeOffer[shopEntity.getShopBarrel().size()];
    }

    public void onScreenClose() {
        clearOldTrades();
    }

    @Override
    public void setCurrentCustomer(@Nullable PlayerEntity var1) {
        currentCustomer = var1;
    }

    @Nullable
    @Override
    public PlayerEntity getCurrentCustomer() {
        return currentCustomer;
    }

    public void openShop(PlayerEntity player) {
        setCurrentCustomer(player);
        sendOffers(player, new LiteralText(SHOP_TITLE), 0);
    }

    public void forceCloseShop() {
        if (currentCustomer != null) {
            currentCustomer.currentScreenHandler.close(currentCustomer);
        }
    }

    // Optimization: cache this and only refresh on barrel inventory changes
    public TradeOfferList getOffers() {
        TradeOfferList list = new TradeOfferList();
        Inventory outputBarrel = shopEntity.getOutputBarrel();

        int maxIndex = shopEntity.getShopBarrel().size() - 1;
        int index = 0;
        while (index + 2 <= maxIndex) {
            ShopTradeOffer offer = getCachedOfferFromIndex(index, outputBarrel);
            if (offer != null) {
                list.add(offer);
            }
            index += 3;
        }
        return list;
    }

    private ShopTradeOffer getCachedOfferFromIndex(int index, Inventory outputBarrel) {
        ShopTradeOffer newOffer = getOfferFromIndex(index, outputBarrel);
        if (newOffer != null) {
            oldTrades[index] = newOffer.asUsed();
            return newOffer;
        }
        return oldTrades[index];
    }

    private ShopTradeOffer getOfferFromIndex(int index, Inventory outputBarrel) {
        Inventory shopBarrel = shopEntity.getShopBarrel();
        ItemStack first = shopBarrel.getStack(index).copy();
        ItemStack second = shopBarrel.getStack(index + 1).copy();
        ItemStack result = shopBarrel.getStack(index + 2).copy();
        if (first.isEmpty() || result.isEmpty()) {
            return null;
        }
        boolean firstCanMerge = Utils.canStacksCombine(first, first);
        boolean secondCanMerge = Utils.canStacksCombine(second, second);
        boolean firstFitsInOutputBarrel = Utils.canPutInInventory(first, outputBarrel);
        boolean secondFitsInOutputBarrel = Utils.canPutInInventory(second, outputBarrel);

        if(!firstCanMerge && !firstFitsInOutputBarrel) {
            String error = "the first payment item(s) does not stack with itself";
            if (outputBarrel != null) {
                error += ", and does not fit in Output barrel";
            }
            return ShopTradeOffer.invalid(first, second, result, index, error);
        }
        if(!secondCanMerge && !secondFitsInOutputBarrel) {
            String error = "the second payment item(s) does not stack with itself";
            if (outputBarrel != null) {
                error += ", and does not fit in Output barrel";
            }
            return ShopTradeOffer.invalid(first, second, result, index, error);
        }
        return ShopTradeOffer.valid(first, second, result, index);
    }

    public void checkTrades(PlayerEntity player) {
        TradeOfferList offers = getOffers();
        boolean allValid = true;
        for (TradeOffer offer : offers) {
            ShopTradeOffer shopOffer = (ShopTradeOffer) offer;
            if (shopOffer.valid)
                continue;
            allValid = false;
            int slot = shopOffer.shopBarrelInventoryIndex + 1;
            Utils.sendMessage(player,
                    "Offer at slot " + slot + "-" + (slot + 2) + " is invalid because " + shopOffer.invalidReason);
        }
        if (allValid) {
            Utils.sendMessage(player, "All trades are valid");
        }
    }

    public void refreshTrades() {
        if (ignoreRefresh)
            return;
        int syncid = currentCustomer.currentScreenHandler.syncId;
        currentCustomer.sendTradeOffers(syncid, getOffers(), 0, 0, this.isLeveledMerchant(), this.canRefreshTrades());
        MerchantScreenHandlerAccessor accessor = (MerchantScreenHandlerAccessor) currentCustomer.currentScreenHandler;
        accessor.getMerchantInventory().updateOffers();
    }

    public void trade(TradeOffer var1) {
        ignoreRefresh = true;
        ShopTradeOffer offer = (ShopTradeOffer) var1;
        int firstSlot = offer.shopBarrelInventoryIndex;
        int secondSlot = firstSlot + 1;
        int resultSlot = firstSlot + 2;
        Inventory shopBarrel = shopEntity.getShopBarrel();
        Inventory outputBarrel = shopEntity.getOutputBarrel();

        ItemStack first = offer.getFirst().copy();
        ItemStack second = offer.getSecond().copy();
        ItemStack result = offer.getResult().copy();
        boolean firstValid = ItemStack.areEqual(shopBarrel.getStack(firstSlot), first);
        boolean secondValid = ItemStack.areEqual(shopBarrel.getStack(secondSlot), second);
        boolean resultValid = ItemStack.areEqual(shopBarrel.getStack(resultSlot), result);
        if (!firstValid) {
            PlayerTrading.LOGGER.error("First items of a trade mismatched!");
        }
        if (!secondValid) {
            PlayerTrading.LOGGER.error("Second items of a trade mismatched!");
        }
        if (!resultValid) {
            PlayerTrading.LOGGER.error("Result items of a trade mismatched!");
        }
        boolean pullResultFromStock = true;
        if (!Utils.tryPutInInventory(first, outputBarrel)) {
            shopBarrel.setStack(firstSlot, Utils.combine(first, first));
            pullResultFromStock = false;
        }
        if (!Utils.tryPutInInventory(second, outputBarrel)) {
            shopBarrel.setStack(secondSlot, Utils.combine(second, second));
            pullResultFromStock = false;
        }
        boolean resultRemoved = false;
        if (pullResultFromStock) {
           resultRemoved = Utils.tryPullFromInventory(result, shopEntity.getStockBarrel());
        }
        if(!resultRemoved) {
            shopBarrel.setStack(resultSlot, ItemStack.EMPTY);
        }
        offer.use();
        ignoreRefresh = false;
        refreshTrades();
    }

    @Override
    public void setOffersFromServer(TradeOfferList var1) {
    }

    @Override
    public void onSellingItem(ItemStack var1) {
    }

    @Override
    public int getExperience() {
        return 0;
    }

    @Override
    public void setExperienceFromServer(int var1) {
    }

    @Override
    public boolean isLeveledMerchant() {
        return false;
    }

    @Override
    public SoundEvent getYesSound() {
        return SoundEvents.ENTITY_VILLAGER_YES;
    }

    @Override
    public boolean isClient() {
        return false;
    }
}
