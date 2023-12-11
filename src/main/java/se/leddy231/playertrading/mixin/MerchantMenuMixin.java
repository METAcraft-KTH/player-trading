package se.leddy231.playertrading.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.Merchant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.leddy231.playertrading.shop.ShopMerchant;

@Mixin(MerchantMenu.class)
abstract public class MerchantMenuMixin {

    @Accessor
    abstract Merchant getTrader();

    @Accessor
    abstract MerchantContainer getTradeContainer();

    // playTradeSound attempts to cast the Merchant instance to an Entity to get its position in the world
    // This would crash since our ShopMerchant is not a entity in the world, therefore we cancel it
    @Inject(at = @At("HEAD"), method = "playTradeSound", cancellable = true)
    private void sayYes(CallbackInfo ci) {
        if (getTrader() instanceof ShopMerchant) {
            ci.cancel();
        }
    }

    @Inject(at = @At("RETURN"), method = "removed")
    private void onScreenClose(Player player, CallbackInfo ci) {
        Merchant merchant = getTrader();
        if (merchant instanceof ShopMerchant) {
            ((ShopMerchant) merchant).onScreenClose();
        }
    }

}
