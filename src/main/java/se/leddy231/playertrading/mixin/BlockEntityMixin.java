package se.leddy231.playertrading.mixin;

import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.leddy231.playertrading.interfaces.IBarrelEntity;
import se.leddy231.playertrading.interfaces.IHopperEntity;

@Mixin(BlockEntity.class)
public class BlockEntityMixin {

    @Inject(at = @At("RETURN"), method = "setChanged()V")
    public void onMarkDirty(CallbackInfo callback) {
        var entity = (BlockEntity) (Object) this;
        if (entity instanceof BarrelBlockEntity barrelEntity) {
            var shop = IBarrelEntity.getConnectedShop(barrelEntity);
            if (shop != null) {
                shop.onContainerChanges();
            }
        }
        if (entity instanceof HopperBlockEntity hopperEntity) {
            var shops = IHopperEntity.getConnectedShops(hopperEntity);
            for (var shop : shops) {
                shop.onContainerChanges();
            }
        }
    }
}
