package se.leddy231.playertrading.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.leddy231.playertrading.interfaces.IHopperEntity;

@Mixin(HopperBlock.class)
public class HopperBlockMixin {
    @Inject(at = @At("HEAD"), method = "affectNeighborsAfterRemoval")
    private void onRemove(
            BlockState blockState, ServerLevel world, BlockPos pos, boolean bl, CallbackInfo ci
    ) {
        var entity = world.getBlockEntity(pos);
        if (!(entity instanceof HopperBlockEntity hopperEntity)) {
            return;
        }

        var shops = IHopperEntity.getConnectedShops(hopperEntity);
        for (var shop : shops) {
            shop.merchant.forceCloseShop();
        }
    }
}
