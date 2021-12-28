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
import se.leddy231.playertrading.BarrelType;
import se.leddy231.playertrading.PlayerTrading;
import se.leddy231.playertrading.interfaces.IAugmentedBarrelEntity;
import se.leddy231.playertrading.interfaces.IShopBarrelEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BarrelBlock.class)
public class BarrelBlockMixin {
	@Inject(at = @At("HEAD"), method = "onUse", cancellable = true)
	private void onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit,
			CallbackInfoReturnable<ActionResult> ci) {
		BlockEntity entity = world.getBlockEntity(pos);
		if (!(entity instanceof BarrelBlockEntity)) {
			PlayerTrading.LOGGER.error("Somehow, a BarrelBlockEntity was not returned from a BarrelBlock");
			return;
		}
		IAugmentedBarrelEntity barrelEntity = (IAugmentedBarrelEntity) (Object) entity;

		if (barrelEntity.getType() == BarrelType.SHOP) {
			if (barrelEntity.getOwner().equals(player.getUuid()) && !player.isSneaking()) {
				return;
			}
			ci.setReturnValue(ActionResult.SUCCESS);
			IShopBarrelEntity shop = (IShopBarrelEntity) barrelEntity;
			shop.getShopMerchant().openShop(player);
		}
	}

	//Update inventory on block break
	@Inject(at = @At("HEAD"), method = "onStateReplaced")
	private void onUse(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved,
			CallbackInfo ci) {
		BlockEntity entity = world.getBlockEntity(pos);
		if (!(entity instanceof BarrelBlockEntity)) {
			return;
		}
		IAugmentedBarrelEntity barrelEntity = (IAugmentedBarrelEntity) (Object) entity;
		if (barrelEntity.getType().isExpansionType()) {
			barrelEntity.onInventoryChange();
		}
		if (barrelEntity.getType() == BarrelType.SHOP) {
			((IShopBarrelEntity) barrelEntity).getShopMerchant().forceCloseShop();
		}
	}
}
