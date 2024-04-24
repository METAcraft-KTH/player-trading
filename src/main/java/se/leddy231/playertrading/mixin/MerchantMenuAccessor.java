package se.leddy231.playertrading.mixin;

import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MerchantMenu.class)
public interface MerchantMenuAccessor {

    @Accessor
    MerchantContainer getTradeContainer();
}
