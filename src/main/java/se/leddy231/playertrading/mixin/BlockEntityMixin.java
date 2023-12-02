package se.leddy231.playertrading.mixin;

import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import se.leddy231.playertrading.interfaces.IAugmentedBarrelEntity;

@Mixin(BlockEntity.class)
public class BlockEntityMixin {
    
    @Inject(at = @At("RETURN"), method = "setChanged()V")
    public void onMarkDirty(CallbackInfo callback) {
        BlockEntity entity = (BlockEntity) (Object) this;
        if (entity instanceof BarrelBlockEntity) {
            IAugmentedBarrelEntity barrelEntity = (IAugmentedBarrelEntity) entity;
            barrelEntity.onInventoryChange();
        }
    }
}
