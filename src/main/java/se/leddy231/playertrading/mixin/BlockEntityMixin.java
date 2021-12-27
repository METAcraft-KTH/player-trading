package se.leddy231.playertrading.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import se.leddy231.playertrading.interfaces.IExpansionBarrelEntity;
import se.leddy231.playertrading.interfaces.IShopBarrelEntity;

@Mixin(BlockEntity.class)
public class BlockEntityMixin {
    
    @Inject(at = @At("RETURN"), method = "markDirty")
    public void onMarkDirty(CallbackInfo callback) {
        BlockEntity entity = (BlockEntity) (Object) this;
        if (entity instanceof BarrelBlockEntity) {
            IExpansionBarrelEntity barrelEntity = (IExpansionBarrelEntity) entity;
            IShopBarrelEntity shop = barrelEntity.getShop();
            if(shop != null) {
                shop.onInventoryChange();
            }
        }
    }
}
