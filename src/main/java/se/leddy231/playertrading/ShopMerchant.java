package se.leddy231.playertrading;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import org.jetbrains.annotations.Nullable;

import se.leddy231.playertrading.interfaces.IAugmentedBarrelEntity;
import se.leddy231.playertrading.interfaces.IShopBarrelEntity;
import se.leddy231.playertrading.mixin.MerchantScreenHandlerAccessor;

public class ShopMerchant implements Merchant {
    private static final String SHOP_TITLE = "Barrel shop";
    @Nullable
    public Player currentCustomer;
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
        oldTrades = new ShopTradeOffer[shopEntity.getShopBarrel().getEntity().getContainerSize()];
    }

    public void onScreenClose() {
        clearOldTrades();
        if (shopEntity.getType() == BarrelType.SINGLEUSE) {
            boolean hasValidTradesLeft = false;
            for (MerchantOffer tradeOffer : getOffers()) {
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
    public void setTradingPlayer(@Nullable Player var1) {
        currentCustomer = var1;
    }

    @Nullable
    @Override
    public Player getTradingPlayer() {
        return currentCustomer;
    }

    private Container getOutputInventory() {
        IAugmentedBarrelEntity barrel = shopEntity.getOutputBarrel();
        if (barrel != null) {
            return barrel.getEntity();
        }
        return null;
    }

    private Container getStockInventory() {
        IAugmentedBarrelEntity barrel = shopEntity.getStockBarrel();
        if (barrel != null) {
            return barrel.getEntity();
        }
        return null;
    }

    public void openShop(Player player) {
        if (currentCustomer != null) {
            Utils.sendToast(player, "This shop is currently in use");
            return;
        }
        if (shopEntity.isAnyBarrelOpen()) {
            Utils.sendToast(player, "This shop is currently being edited by its owner");
            return;
        }
        setTradingPlayer(player);
        openTradingScreen(player, Component.literal(SHOP_TITLE), 0);
    }

    public void forceCloseShop() {
        if (currentCustomer != null) {
            currentCustomer.containerMenu.removed(currentCustomer);
        }
    }

    // Optimization: cache this and only refresh on barrel inventory changes
    public MerchantOffers getOffers() {
        MerchantOffers list = new MerchantOffers();
        Container outputBarrel = getOutputInventory();
        Container stockBarrel = getStockInventory();

        int maxIndex = shopEntity.getShopBarrel().getEntity().getContainerSize() - 1;
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

    private ShopTradeOffer getCachedOfferFromIndex(int index, Container outputBarrel, Container stockBarrel) {
        ShopTradeOffer newOffer = getOfferFromIndex(index, outputBarrel, stockBarrel);
        if (newOffer != null) {
            oldTrades[index] = newOffer.asUsed();
            return newOffer;
        }
        return oldTrades[index];
    }

    private ShopTradeOffer getOfferFromIndex(int index, Container outputBarrel, Container stockBarrel) {
        Container shopBarrel = shopEntity.getShopBarrel().getEntity();
        BarrelType shopType = shopEntity.getType();

        ItemStack first = shopBarrel.getItem(index).copy();
        ItemStack second = shopBarrel.getItem(index + 1).copy();
        ItemStack result = shopBarrel.getItem(index + 2).copy();
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

    public void checkTrades(Player player) {
        MerchantOffers offers = getOffers();
        boolean allValid = true;
        for (MerchantOffer offer : offers) {
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
        int syncid = currentCustomer.containerMenu.containerId;
        currentCustomer.sendMerchantOffers(syncid, getOffers(), 0, 0, this.showProgressBar(), this.canRestock());
        MerchantScreenHandlerAccessor accessor = (MerchantScreenHandlerAccessor) currentCustomer.containerMenu;
        accessor.getTradeContainer().setChanged();
    }

    public void trade(MerchantOffer var1) {
        ShopTradeOffer offer = (ShopTradeOffer) var1;
        ignoreRefresh = true;

        if (shopEntity.getType().isAdminType()) {
            Container outputBarrel = getOutputInventory();
            ItemStack first = offer.getFirst().copy();
            ItemStack second = offer.getSecond().copy();
            Utils.tryPutInInventory(first, outputBarrel);
            Utils.tryPutInInventory(second, outputBarrel);
            if (shopEntity.getType() == BarrelType.SINGLEUSE) {
                int firstSlot = offer.shopBarrelInventoryIndex;
                int secondSlot = firstSlot + 1;
                int resultSlot = firstSlot + 2;
                Container shopBarrel = shopEntity.getShopBarrel().getEntity();
                shopBarrel.setItem(firstSlot, ItemStack.EMPTY);
                shopBarrel.setItem(secondSlot, ItemStack.EMPTY);
                shopBarrel.setItem(resultSlot, ItemStack.EMPTY);
            }
            ignoreRefresh = false;
            refreshTrades();
            return;
        }

        int firstSlot = offer.shopBarrelInventoryIndex;
        int secondSlot = firstSlot + 1;
        int resultSlot = firstSlot + 2;
        Container shopBarrel = shopEntity.getShopBarrel().getEntity();
        Container outputBarrel = getOutputInventory();
        Container stockBarrel = getStockInventory();

        ItemStack first = offer.getFirst().copy();
        ItemStack second = offer.getSecond().copy();
        ItemStack result = offer.getResult().copy();
        boolean firstValid = ItemStack.isSameItem(shopBarrel.getItem(firstSlot), first);
        boolean secondValid = ItemStack.isSameItem(shopBarrel.getItem(secondSlot), second);
        boolean resultValid = ItemStack.isSameItem(shopBarrel.getItem(resultSlot), result);
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
            shopBarrel.setItem(firstSlot, Utils.combine(first, first));
        }
        if (!secondWentToOutput) {
            shopBarrel.setItem(secondSlot, Utils.combine(second, second));
        }
        boolean resultTakenFromStock = firstWentToOutput && secondWentToOutput
                && Utils.tryPullFromInventory(result, stockBarrel);
        if (!resultTakenFromStock) {
            shopBarrel.setItem(resultSlot, ItemStack.EMPTY);
        }
        offer.setToOutOfStock();
        ignoreRefresh = false;
        refreshTrades();
    }

    private void selfDestruct() {
        BarrelBlockEntity entity = shopEntity.getEntity();
        entity.setRemoved();
        BlockPos pos = entity.getBlockPos();
        shopEntity.getEntity().getLevel().destroyBlock(pos, false);
    }

    @Override
    public void overrideOffers(MerchantOffers var1) {
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {

    }

    @Override
    public void notifyTradeUpdated(ItemStack stack) {

    }

    @Override
    public int getVillagerXp() {
        return 0;
    }

    @Override
    public void overrideXp(int var1) {
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_YES;
    }

    @Override
    public boolean isClientSide() {
        return false;
    }
}
