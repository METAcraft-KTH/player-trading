package se.leddy231.playertrading.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.leddy231.playertrading.interfaces.IHopperEntity;

@Mixin(HopperBlockEntity.class)
public class HopperEntityMixin {

    @Inject(method = "ejectItems", at = @At("HEAD"), cancellable = true)
    private static void ejectItems(
            Level level,
            BlockPos pos,
            BlockState state,
            Container sourceContainer,
            CallbackInfoReturnable<Boolean> callback
    ) {
        var facing = state.getValue(HopperBlock.FACING);
        IHopperEntity.cancelMoveIfConnectedToShop(level, pos.relative(facing), callback);
    }

    @Inject(method = "suckInItems", at = @At("HEAD"), cancellable = true)
    private static void suckInItems(Level level, Hopper hopper, CallbackInfoReturnable<Boolean> callback) {
        var pos = BlockPos.containing(hopper.getLevelX(), hopper.getLevelY(), hopper.getLevelZ());
        IHopperEntity.cancelMoveIfConnectedToShop(level, pos.relative(Direction.UP), callback);
    }
}
