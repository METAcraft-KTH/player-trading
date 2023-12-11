package se.leddy231.playertrading.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PlayerHeadBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.leddy231.playertrading.interfaces.ISkullEntity;

@Mixin(PlayerHeadBlock.class)
public class PlayerHeadBlockMixin {

    @Inject(at = @At("RETURN"), method = "setPlacedBy")
    private void onPlace(
            Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack, CallbackInfo ci
    ) {
        if (!(level.getBlockEntity(pos) instanceof SkullBlockEntity skullBlockEntity)) {
            return;
        }
        var entity = (ISkullEntity) skullBlockEntity;
        entity.onPlace(level, pos, state, placer);
    }

    InteractionResult use(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit
    ) {
        if (!(level.getBlockEntity(pos) instanceof SkullBlockEntity skullBlockEntity)) {
            return InteractionResult.FAIL;
        }
        var entity = (ISkullEntity) skullBlockEntity;
        return entity.onUse(state, level, pos, player, hand, hit);
    }
}
