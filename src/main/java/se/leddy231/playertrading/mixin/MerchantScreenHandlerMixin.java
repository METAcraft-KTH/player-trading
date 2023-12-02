package se.leddy231.playertrading.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.Merchant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import se.leddy231.playertrading.ShopMerchant;

@Mixin(MerchantMenu.class)
public class MerchantScreenHandlerMixin {
    // playYesSound attempts to cast the Merchant instance to an Entity to get its position in the world
    // This would crash since our ShopMerchant is not a entity in the world, therefore we cancel it
    @Inject(at = @At("HEAD"), method = "playTradeSound", cancellable = true)
	private void sayYes(CallbackInfo ci) {
        MerchantScreenHandlerAccessor accessor = (MerchantScreenHandlerAccessor) this;
        if (accessor.getTrader() instanceof ShopMerchant) {
            ci.cancel();
        }
	}

    @Inject(at = @At("RETURN"), method = "removed")
	private void onScreenClose(Player player, CallbackInfo ci) {
        MerchantScreenHandlerAccessor accessor = (MerchantScreenHandlerAccessor) this;
        Merchant merchant = accessor.getTrader();
        if (merchant instanceof ShopMerchant) {
            ((ShopMerchant) merchant).onScreenClose();
        }
	}
    
}
