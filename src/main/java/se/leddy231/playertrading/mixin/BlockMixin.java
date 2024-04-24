package se.leddy231.playertrading.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PlayerHeadBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.leddy231.playertrading.interfaces.ISkullEntity;

@Mixin(Block.class)
public class BlockMixin {

    @Inject(at = @At("RETURN"), method = "setPlacedBy")
    private void onPlace(
            Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack, CallbackInfo ci
    ) {
        //Have to check root class since PlayerHeadBlock no longer has a setPlaceBy implemented.
        if (!((Object) this instanceof PlayerHeadBlock)) return;
        if (!(level.getBlockEntity(pos) instanceof SkullBlockEntity skullBlockEntity)) {
            return;
        }
        var entity = (ISkullEntity) skullBlockEntity;
        entity.player_trading$onPlace(level, pos, state, placer);
    }
}