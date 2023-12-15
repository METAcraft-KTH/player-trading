package se.leddy231.playertrading.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
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
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    void use(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit,
            CallbackInfoReturnable<InteractionResult> ci
    ) {
        if (state.getBlock() != Blocks.PLAYER_HEAD && state.getBlock() != Blocks.PLAYER_WALL_HEAD) {
            return;
        }
        if (!(level.getBlockEntity(pos) instanceof SkullBlockEntity skullBlockEntity)) {
            return;
        }
        var entity = (ISkullEntity) skullBlockEntity;
        var result = entity.onUse(state, level, pos, player, hand, hit);
        ci.setReturnValue(result);
        ci.cancel();
    }
}
