package se.leddy231.playertrading;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.village.Merchant;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;

public class ShopMerchant implements Merchant {
    @Nullable
    public PlayerEntity currentCustomer;
    public BarrelBlockEntity tradesChest;
    // Trades that expire/are used up needs to be kept inte the trades list until
    // the screen is closed
    // Otherwise, the client game crashes
    public ShopTradeOffer[] oldTrades;

    public ShopMerchant(BarrelBlockEntity tradesChest) {
        super();
        this.tradesChest = tradesChest;
        clearOldTrades();
    }

    public void clearOldTrades() {
        oldTrades = new ShopTradeOffer[tradesChest.size()];
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
        sendOffers(player, new LiteralText("Shop!"), 0);
    }

    public TradeOfferList getOffers() {
        TradeOfferList list = new TradeOfferList();

        if (tradesChest != null) {
            int maxIndex = tradesChest.size() - 1;
            int index = 0;
            while (index + 2 <= maxIndex) {
                ShopTradeOffer offer = getOfferFromIndex(index);
                if (offer != null) {
                    list.add(offer);
                }
                index += 3;
            }
        }
        return list;
    }

    private ShopTradeOffer getOfferFromIndex(int index) {
        if (oldTrades[index] != null) {
            return oldTrades[index];
        }
        ItemStack first = tradesChest.getStack(index).copy();
        ItemStack second = tradesChest.getStack(index + 1).copy();
        ItemStack result = tradesChest.getStack(index + 2).copy();
        if (first.isEmpty() || result.isEmpty()) {
            return null;
        }
        boolean fitsInOutputChest = false; // TODO: implement output chest addition
        if (first.getMaxCount() <= 1 && !fitsInOutputChest) {
            return ShopTradeOffer.invalid(first, second, result, index,
                    "the payment item does not stack, and does not fit in output chest");
        }
        boolean canStackSelf = first.getCount() <= first.getMaxCount() / 2;
        if (!canStackSelf && !fitsInOutputChest) {
            return ShopTradeOffer.invalid(first, second, result, index,
                    "the payment items stack size is too big to merge with itself, and does not fit in output chest");
        }
        return ShopTradeOffer.valid(first, second, result, index);

    }

    public void refreshTrades() {
        int syncid = currentCustomer.currentScreenHandler.syncId;
        currentCustomer.sendTradeOffers(syncid, getOffers(), 0, 0, this.isLeveledMerchant(), this.canRefreshTrades());
    }

    @Override
    public void setOffersFromServer(TradeOfferList var1) {
    }

    public void trade(TradeOffer var1) {
        ShopTradeOffer offer = (ShopTradeOffer) var1;
        int index = offer.tradeChestInventoryIndex;
        boolean first = ItemStack.areEqual(tradesChest.getStack(index), offer.getFirst());
        boolean second = ItemStack.areEqual(tradesChest.getStack(index + 1), offer.getSecond());
        boolean result = ItemStack.areEqual(tradesChest.getStack(index + 2), offer.getResult());
        if (!first) {
            PlayerTrading.LOGGER.error("First items of trade mismatch");
        }
        if (!second) {
            PlayerTrading.LOGGER.error("Second items of trade mismatch");
        }
        if (!result) {
            PlayerTrading.LOGGER.error("Result items of trade mismatch");
        }
        if (first && second && result) {
            ItemStack firstPayout = tradesChest.getStack(index);
            ItemStack secondPayout = tradesChest.getStack(index + 1);
            firstPayout.setCount(firstPayout.getCount() * 2);
            secondPayout.setCount(secondPayout.getCount() * 2);
            tradesChest.setStack(index, firstPayout);
            tradesChest.setStack(index + 1, secondPayout);
            tradesChest.setStack(index + 2, ItemStack.EMPTY);
        }
        offer.use();
        oldTrades[index] = offer;
        refreshTrades();
    }

    public void onSellingItem(ItemStack var1) {
    }

    public int getExperience() {
        return 0;
    }

    public void setExperienceFromServer(int var1) {
    }

    public boolean isLeveledMerchant() {
        return false;
    }

    public SoundEvent getYesSound() {
        return SoundEvents.ENTITY_VILLAGER_YES;
    }

    public boolean isClient() {
        return false;
    }
}
