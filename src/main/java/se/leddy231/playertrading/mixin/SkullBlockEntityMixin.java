package se.leddy231.playertrading.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.PlayerWallHeadBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.leddy231.playertrading.BarrelType;
import se.leddy231.playertrading.PlayerTrading;
import se.leddy231.playertrading.interfaces.IAugmentedBarrelEntity;

@Mixin(PlayerWallHeadBlock.class)
public class SkullBlockEntityMixin {


    @Inject(at = @At("RETURN"), method = "setPlacedBy")
    private void onPlace(
            Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack, CallbackInfo ci
    ) {
        //check so the placer is a player
        if (!(placer instanceof Player)) {
            return;
        }
        PlayerTrading.LOGGER.info("Placed head");
        var direction = state.getValue(WallSkullBlock.FACING).getOpposite();
        var barrelPos = pos.relative(direction);
        PlayerTrading.LOGGER.info("Barrel pos");
        PlayerTrading.LOGGER.info(barrelPos);
        //check so the block entity exists and is a skullblock entity
        if (!(level.getBlockEntity(pos) instanceof SkullBlockEntity skullBlockEntity)) {
            return;
        }
        var ownerProfile = skullBlockEntity.getOwnerProfile();
        //check so that playerhead is valid
        PlayerTrading.LOGGER.info("Owner profile");
        PlayerTrading.LOGGER.info(ownerProfile);
        if (ownerProfile == null) {
            return;
        }

        if (!(level.getBlockState(barrelPos).getBlock() instanceof BarrelBlock)) {
            return;
        }

        //compare placer uuid with uuid from shop block head
        if (!ownerProfile.getId().equals(PlayerTrading.SHOP_BLOCK_UUID)) {
            return;
        }
        PlayerTrading.LOGGER.info("Activating");
        IAugmentedBarrelEntity barrelEntity = (IAugmentedBarrelEntity) level.getBlockEntity(barrelPos);
        barrelEntity.activate((Player) placer, BarrelType.SHOP);
    }

}
