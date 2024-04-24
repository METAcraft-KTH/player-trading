package se.leddy231.playertrading.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PlayerWallHeadBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import se.leddy231.playertrading.shop.Shop;

public interface ISkullEntity {
    static BlockPos attachedToPosition(SkullBlockEntity entity) {
        var pos = entity.getBlockPos();
        var state = entity.getLevel().getBlockState(pos);
        if (state.getBlock() instanceof PlayerWallHeadBlock) {
            var direction = state.getValue(WallSkullBlock.FACING).getOpposite();
            return pos.relative(direction);
        }
        return pos.relative(Direction.DOWN);
    }

    @Nullable Shop player_trading$getShop();

    void player_trading$onPlace(Level level, BlockPos pos, BlockState state, LivingEntity placer);

    InteractionResult player_trading$onUseWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit
    );
}
