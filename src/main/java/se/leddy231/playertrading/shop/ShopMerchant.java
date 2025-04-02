package se.leddy231.playertrading.shop;

import com.google.common.base.Suppliers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import se.leddy231.playertrading.Utils;
import se.leddy231.playertrading.criteria.ShopCriteriaTriggers;
import se.leddy231.playertrading.mixin.MerchantMenuAccessor;

import java.util.Optional;
import java.util.function.Supplier;

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

    private LootContext getContext() {
        if (currentCustomer == null || !(currentCustomer.level() instanceof ServerLevel world)) {
            return null;
        }
        return new LootContext.Builder(
                new LootParams.Builder(world)
                        .withParameter(LootContextParams.ORIGIN, currentCustomer.position())
                        .withParameter(LootContextParams.THIS_ENTITY, currentCustomer)
                        .withParameter(LootContextParams.BLOCK_STATE, shop.entity.getBlockState())
                        .withParameter(LootContextParams.BLOCK_ENTITY, shop.entity)
                        .create(ShopCriteriaTriggers.SHOP_CONTEXT)
        ).create(Optional.empty());
    }

    @Override
    // Optimization: cache this and only refresh on barrel inventory changes
    public MerchantOffers getOffers() {
        MerchantOffers list = new MerchantOffers();

        //Container outputBarrel = getOutputInventory();
        //Container stockBarrel = getStockInventory();

        int maxIndex = shop.maxNumberOfTrades();

        Supplier<LootContext> ctx = Suppliers.memoize(this::getContext);

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
                if (offer.valid && shop.hasCondition(i) && ctx.get() != null && !shop.getCondition(i).test(ctx.get())) {
                    offer = offer.asInvalid(Component.translatableWithFallback(
                            "message.playertrading.invalid_reason.condition_failed",
                            "Trade condition not met"
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
        if (this.getTradingPlayer() instanceof ServerPlayer player) {
            ShopCriteriaTriggers.TRADE.trigger(player, shop, offer.getResult());
        }
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

    @Override
    public boolean stillValid(Player player) {
        return this.getTradingPlayer() == player && !shop.entity.isRemoved() &&
                player.canInteractWithEntity(new AABB(shop.entity.getBlockPos()), 4.0);
    }
}
