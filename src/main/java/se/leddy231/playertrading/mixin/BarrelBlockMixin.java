package se.leddy231.playertrading.mixin;

import net.minecraft.block.BarrelBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import se.leddy231.playertrading.AugmentedBarrelEntity;
import se.leddy231.playertrading.PlayerTrading;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BarrelBlock.class)
public class BarrelBlockMixin {
    @Inject(at = @At("HEAD"), method = "onUse", cancellable = true)
	private void onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> ci) {
		BlockEntity entity = world.getBlockEntity(pos);
		if (!(entity instanceof BarrelBlockEntity)) {
			PlayerTrading.LOGGER.error("Somehow, a BarrelBlockEntity was not returned from a BarrelBlock");
			return;
		}
		AugmentedBarrelEntity barrelEntity = (AugmentedBarrelEntity) (Object) entity;
		
		if (barrelEntity.isShop()) {
			if (barrelEntity.getShopOwner() == player.getUuid() && !player.isSneaking()) {
				return;
			}
			ci.setReturnValue(ActionResult.SUCCESS);
			barrelEntity.getShopMerchant().openShop(player);
		}

	}
}
