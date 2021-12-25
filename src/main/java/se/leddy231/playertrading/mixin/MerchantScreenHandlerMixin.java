package se.leddy231.playertrading.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.village.Merchant;
import se.leddy231.playertrading.ShopMerchant;

@Mixin(MerchantScreenHandler.class)
public class MerchantScreenHandlerMixin {
    // playYesSound attempts to cast the Merchant instance to an Entity to get its position in the world
    // This would crash since our ShopMerchant is not a entity in the world, therefore we cancel it
    @Inject(at = @At("HEAD"), method = "playYesSound", cancellable = true)
	private void sayYes(CallbackInfo ci) {
        MerchantScreenHandlerAccessor accessor = (MerchantScreenHandlerAccessor) this;
        if (accessor.getMerchant() instanceof ShopMerchant) {
            ci.cancel();
        }
	}

    @Inject(at = @At("RETURN"), method = "close")
	private void onScreenClose(PlayerEntity player, CallbackInfo ci) {
        MerchantScreenHandlerAccessor accessor = (MerchantScreenHandlerAccessor) this;
        Merchant merchant = accessor.getMerchant();
        if (accessor.getMerchant() instanceof ShopMerchant) {
            ((ShopMerchant) merchant).onScreenClose();
        }
	}
    
}
