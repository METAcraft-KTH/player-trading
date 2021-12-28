package se.leddy231.playertrading.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.village.Merchant;
import net.minecraft.village.MerchantInventory;

@Mixin(MerchantScreenHandler.class)
public interface MerchantScreenHandlerAccessor {
    
    @Accessor
    public Merchant getMerchant();

    @Accessor
    public MerchantInventory getMerchantInventory();
}
