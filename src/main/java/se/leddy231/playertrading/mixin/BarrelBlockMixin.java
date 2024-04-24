package se.leddy231.playertrading.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.leddy231.playertrading.interfaces.IBarrelEntity;

@Mixin(BarrelBlock.class)
public class BarrelBlockMixin {

    @Inject(at = @At("HEAD"), method = "useWithoutItem", cancellable = true)
    public void onUse(
            BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!(blockEntity instanceof BarrelBlockEntity barrelEntity)) {
            return;
        }
        var shop = IBarrelEntity.getConnectedShop(barrelEntity);
        if (shop == null) {
            return;
        }
        var result = shop.onBarrelUse(player);
        if (result != null) {
            cir.setReturnValue(result);
            cir.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "onRemove")
    private void onRemove(
            BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved, CallbackInfo ci
    ) {
        if (state.is(newState.getBlock())) {
            //Ignore state changes when for example changing texture to show the barrel being open.
            return;
        }
        var entity = world.getBlockEntity(pos);
        if (!(entity instanceof BarrelBlockEntity barrelEntity)) {
            return;
        }

        var shop = IBarrelEntity.getConnectedShop(barrelEntity);
        if (shop != null) {
            shop.merchant.forceCloseShop();
        }
    }
}
