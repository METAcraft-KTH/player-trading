package se.leddy231.playertrading.shop;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.jetbrains.annotations.Nullable;
import se.leddy231.playertrading.Utils;
import se.leddy231.playertrading.mixin.MerchantMenuAccessor;

public class ShopMerchant implements Merchant {
    private static final Component SHOP_TITLE = Component.translatableWithFallback(
            "gui.playertrading.shop", "Shop"
    );
    @Nullable
    public Player currentCustomer;

    // Trades that expire/are used up needs to be kept in the trades list until
    // the screen is closed
    // Otherwise, the client game crashes

    public Shop shop;
    public boolean hasTraded = false;
    // Ignore inventory refresh while moving things around in the inventory
    private boolean ignoreRefresh = false;

    public ShopMerchant(Shop shop) {
        super();
        this.shop = shop;
    }

    public void onScreenClose() {
        shop.onShopClose();
    }

    @Nullable
    @Override
    public Player getTradingPlayer() {
        return currentCustomer;
    }

    @Override
    public void setTradingPlayer(@Nullable Player var1) {
        currentCustomer = var1;
    }

    public void openShop(Player player) {
        if (currentCustomer != null) {
            Utils.sendToast(player, Component.translatableWithFallback(
                    "toast.playertrading.shop_busy", "This shop is currently in use"
            ));
            return;
        }
        setTradingPlayer(player);
        openTradingScreen(player, SHOP_TITLE, 0);
    }

    public void forceCloseShop() {
        if (currentCustomer != null) {
            currentCustomer.containerMenu.removed(currentCustomer);
        }
    }

    // Optimization: cache this and only refresh on barrel inventory changes
    public MerchantOffers getOffers() {
        MerchantOffers list = new MerchantOffers();

        //Container outputBarrel = getOutputInventory();
        //Container stockBarrel = getStockInventory();

        int maxIndex = shop.maxNumberOfTrades();
        for (int i = 0; i < maxIndex; i++) {
            ShopTradeOffer offer = shop.configContainer.getOfferFromIndex(i);
            if (offer != null) {
                offer = shop.validateOffer(offer);
                if (hasTraded && shop.shopType == ShopType.SINGLEUSE) {
                    offer = offer.asInvalid(Component.translatableWithFallback(
                            "message.playertrading.invalid_reason.expired",
                            "Trade expired"
                    ));
                }
                list.add(offer);
            }
        }
        return list;
    }

    public void refreshTrades() {
        if (ignoreRefresh) return;
        if (currentCustomer == null) return;
        int syncid = currentCustomer.containerMenu.containerId;
        currentCustomer.sendMerchantOffers(syncid, getOffers(), 0, 0, this.showProgressBar(), this.canRestock());
        MerchantMenuAccessor accessor = (MerchantMenuAccessor) currentCustomer.containerMenu;
        accessor.getTradeContainer().setChanged();
    }

    @Override
    public void notifyTrade(MerchantOffer var1) {
        ShopTradeOffer offer = (ShopTradeOffer) var1;
        ignoreRefresh = true;
        hasTraded = true;
        shop.performTrade(offer);
        offer.setToOutOfStock();
        ignoreRefresh = false;
        refreshTrades();
    }

    @Override
    public void overrideOffers(MerchantOffers var1) {
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
        return null;
    }

    @Override
    public boolean isClientSide() {
        return false;
    }
}
