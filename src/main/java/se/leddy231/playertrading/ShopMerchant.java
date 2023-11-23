package se.leddy231.playertrading;

import java.util.*;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.Merchant;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import se.leddy231.playertrading.interfaces.IAugmentedBarrelEntity;
import se.leddy231.playertrading.interfaces.IShopBarrelEntity;
import se.leddy231.playertrading.mixin.MerchantScreenHandlerAccessor;
import se.leddy231.playertrading.stats.ShopStat;
import se.leddy231.playertrading.stats.ShopStatManager;

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
    public TradeOfferList cachedOffers;

    public ShopMerchant(IShopBarrelEntity shopEntity) {
        super();
        this.shopEntity = shopEntity;
        clearOldTrades();
    }

    public void clearOldTrades() {
        oldTrades = new ShopTradeOffer[shopEntity.getShopBarrel().getEntity().size()];
    }

    public void onScreenClose() {
        clearOldTrades();
        if (shopEntity.getType() == BarrelType.SINGLEUSE) {
            boolean hasValidTradesLeft = false;
            for (TradeOffer tradeOffer : getOffers()) {
                ShopTradeOffer shopOffer = (ShopTradeOffer) tradeOffer;
                if (shopOffer.valid) {
                    hasValidTradesLeft = true;
                }
            }
            if (!hasValidTradesLeft) {
                selfDestruct();
            }
        }
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

    private Inventory getOutputInventory() {
        IAugmentedBarrelEntity barrel = shopEntity.getOutputBarrel();
        if (barrel != null) {
            return barrel.getEntity();
        }
        return null;
    }

    private Inventory getStockInventory() {
        IAugmentedBarrelEntity barrel = shopEntity.getStockBarrel();
        if (barrel != null) {
            return barrel.getEntity();
        }
        return null;
    }

    public void openShop(PlayerEntity player) {
        if (currentCustomer != null) {
            Utils.sendToast(player, "This shop is currently in use");
            return;
        }
        if (shopEntity.isAnyBarrelOpen()) {
            Utils.sendToast(player, "This shop is currently being edited by its owner");
            return;
        }
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
        Inventory outputBarrel = getOutputInventory();
        Inventory stockBarrel = getStockInventory();

        int maxIndex = shopEntity.getShopBarrel().getEntity().size() - 1;
        int index = 0;
        while (index + 2 <= maxIndex) {
            ShopTradeOffer offer = getCachedOfferFromIndex(index, outputBarrel, stockBarrel);
            if (offer != null) {
                list.add(offer);
            }
            index += 3;
        }
        return list;
    }

    private ShopTradeOffer getCachedOfferFromIndex(int index, Inventory outputBarrel, Inventory stockBarrel) {
        ShopTradeOffer newOffer = getOfferFromIndex(index, outputBarrel, stockBarrel);
        if (newOffer != null) {
            oldTrades[index] = newOffer.asUsed();
            return newOffer;
        }
        return oldTrades[index];
    }

    private ShopTradeOffer getOfferFromIndex(int index, Inventory outputBarrel, Inventory stockBarrel) {
        Inventory shopBarrel = shopEntity.getShopBarrel().getEntity();
        BarrelType shopType = shopEntity.getType();

        ItemStack first = shopBarrel.getStack(index).copy();
        ItemStack second = shopBarrel.getStack(index + 1).copy();
        ItemStack result = shopBarrel.getStack(index + 2).copy();
        if (first.isEmpty() || result.isEmpty()) {
            return null;
        }

        if (shopType.isAdminType()) {
            return ShopTradeOffer.valid(first, second, result, index);
        }

        boolean firstCanMerge = Utils.canStacksCombine(first, first);
        boolean secondCanMerge = Utils.canStacksCombine(second, second);
        boolean firstFitsInOutputBarrel = Utils.canPutInInventory(first, outputBarrel);
        boolean secondFitsInOutputBarrel = Utils.canPutInInventory(second, outputBarrel);
        boolean canPullFromStock = Utils.canPullFromInventory(result, stockBarrel);

        if (shopEntity.getType() == BarrelType.PERMANENT) {
            if (outputBarrel == null) {
                return ShopTradeOffer.invalid(first, second, result, index, "no output barrel is connected");
            }
            if (stockBarrel == null) {
                return ShopTradeOffer.invalid(first, second, result, index, "no stock barrel is connected");
            }
            if (!firstFitsInOutputBarrel) {
                return ShopTradeOffer.invalid(first, second, result, index,
                        "the first payment item(s) does not fit in output barrel");
            }
            if (!secondFitsInOutputBarrel) {
                return ShopTradeOffer.invalid(first, second, result, index,
                        "the second payment item(s) does not fit in output barrel");
            }
            if (!canPullFromStock) {
                return ShopTradeOffer.invalid(first, second, result, index,
                        "the result item(s) are not in the stock barrel");
            }
            return ShopTradeOffer.valid(first, second, result, index);
        }

        if (shopEntity.getType() == BarrelType.SHOP) {
            if (!firstCanMerge && !firstFitsInOutputBarrel) {
                String error = "the first payment item(s) does not stack with itself";
                if (outputBarrel != null) {
                    error += ", and does not fit in Output barrel";
                }
                return ShopTradeOffer.invalid(first, second, result, index, error);
            }
            if (!secondCanMerge && !secondFitsInOutputBarrel) {
                String error = "the second payment item(s) does not stack with itself";
                if (outputBarrel != null) {
                    error += ", and does not fit in Output barrel";
                }
                return ShopTradeOffer.invalid(first, second, result, index, error);
            }
            return ShopTradeOffer.valid(first, second, result, index);
        }
        return ShopTradeOffer.invalid(first, second, result, index, "this is a unknown shop type");
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
        cachedOffers = getOffers();
        if (currentCustomer != null ){
            int syncid = currentCustomer.currentScreenHandler.syncId;
            currentCustomer.sendTradeOffers(syncid, cachedOffers, 0, 0, this.isLeveledMerchant(), this.canRefreshTrades());
            MerchantScreenHandlerAccessor accessor = (MerchantScreenHandlerAccessor) currentCustomer.currentScreenHandler;
            accessor.getMerchantInventory().updateOffers();
        }
        PlayerTrading.LOGGER.info("test");
        BlockPos pos = shopEntity.getEntity().getPos();
        ShopStat stat = new ShopStat(pos, shopEntity.getOwner(), cachedOffers);
        ShopStatManager.getInstance().addStat(stat);
    }

    public void trade(TradeOffer var1) {
        ShopTradeOffer offer = (ShopTradeOffer) var1;
        ignoreRefresh = true;

        if (shopEntity.getType().isAdminType()) {
            Inventory outputBarrel = getOutputInventory();
            ItemStack first = offer.getFirst().copy();
            ItemStack second = offer.getSecond().copy();
            Utils.tryPutInInventory(first, outputBarrel);
            Utils.tryPutInInventory(second, outputBarrel);
            if (shopEntity.getType() == BarrelType.SINGLEUSE) {
                int firstSlot = offer.shopBarrelInventoryIndex;
                int secondSlot = firstSlot + 1;
                int resultSlot = firstSlot + 2;
                Inventory shopBarrel = shopEntity.getShopBarrel().getEntity();
                shopBarrel.setStack(firstSlot, ItemStack.EMPTY);
                shopBarrel.setStack(secondSlot, ItemStack.EMPTY);
                shopBarrel.setStack(resultSlot, ItemStack.EMPTY);
            }
            ignoreRefresh = false;
            refreshTrades();
            return;
        }

        int firstSlot = offer.shopBarrelInventoryIndex;
        int secondSlot = firstSlot + 1;
        int resultSlot = firstSlot + 2;
        Inventory shopBarrel = shopEntity.getShopBarrel().getEntity();
        Inventory outputBarrel = getOutputInventory();
        Inventory stockBarrel = getStockInventory();

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
        boolean firstWentToOutput = Utils.tryPutInInventory(first, outputBarrel);
        boolean secondWentToOutput = Utils.tryPutInInventory(second, outputBarrel);
        if (!firstWentToOutput) {
            shopBarrel.setStack(firstSlot, Utils.combine(first, first));
        }
        if (!secondWentToOutput) {
            shopBarrel.setStack(secondSlot, Utils.combine(second, second));
        }
        boolean resultTakenFromStock = firstWentToOutput && secondWentToOutput
                && Utils.tryPullFromInventory(result, stockBarrel);
        if (!resultTakenFromStock) {
            shopBarrel.setStack(resultSlot, ItemStack.EMPTY);
        }
        offer.use();
        ignoreRefresh = false;
        refreshTrades();
    }

    private void selfDestruct() {
        BarrelBlockEntity entity = shopEntity.getEntity();
        entity.clear();
        BlockPos pos = entity.getPos();
        shopEntity.getEntity().getWorld().breakBlock(pos, false);
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
