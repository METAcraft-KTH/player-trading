package se.leddy231.playertrading.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.leddy231.playertrading.interfaces.ISkullEntity;

@Mixin(BlockBehaviour.class)
public class BlockBehaviourMixin {
    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    void use(
            BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (blockState.getBlock() != Blocks.PLAYER_HEAD && blockState.getBlock() != Blocks.PLAYER_WALL_HEAD) {
            return;
        }
        if (!(level.getBlockEntity(blockPos) instanceof SkullBlockEntity skullBlockEntity)) {
            return;
        }
        var entity = (ISkullEntity) skullBlockEntity;
        var result = entity.player_trading$onUseWithoutItem(blockState, level, blockPos, player, blockHitResult);
        cir.setReturnValue(result);
        cir.cancel();
    }
}
