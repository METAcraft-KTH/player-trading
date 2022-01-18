package se.leddy231.playertrading.mixin;

import net.minecraft.block.BarrelBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import se.leddy231.playertrading.BarrelType;
import se.leddy231.playertrading.DebugStickCommand;
import se.leddy231.playertrading.PlayerTrading;
import se.leddy231.playertrading.ShopKey;
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

		if (barrelEntity.getType() != BarrelType.NONE) {
			ItemStack usedItem = player.getMainHandStack();
			boolean isOwner = barrelEntity.getOwner().equals(player.getUuid());
			boolean makeKey = isOwner && usedItem.isOf(Items.GOLD_INGOT) && !ShopKey.isKey(usedItem);

			if (makeKey) {
				ShopKey.makeIntoKeyForPlayer(usedItem, player);
				ci.setReturnValue(ActionResult.SUCCESS);
				return;
			}

			IShopBarrelEntity shop = barrelEntity.findConnectedShop();
			if (shop == null) {
				return;
			}

			boolean ownerBypass = isOwner && !player.isSneaking();
			boolean keyBypass = ShopKey.isKeyForUUID(usedItem, barrelEntity.getOwner());
			boolean opDebugBypass = player.hasPermissionLevel(4) && ItemStack.areNbtEqual(usedItem, DebugStickCommand.STICK);
			if (ownerBypass || keyBypass || opDebugBypass) {
				//Close the trade window if a customer is using the shop
				shop.getShopMerchant().forceCloseShop();
				return;
			}

			ci.setReturnValue(ActionResult.SUCCESS);
			shop.getShopMerchant().openShop(player);
		}
	}

	// Update inventory on block break
	@Inject(at = @At("HEAD"), method = "onStateReplaced")
	private void onUse(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved,
			CallbackInfo ci) {
		if (state.isOf(newState.getBlock())) {
			//Ignore state changes when for example chaning texture to show the barrel being open.
			return;
		}
		BlockEntity entity = world.getBlockEntity(pos);
		if (!(entity instanceof BarrelBlockEntity)) {
			return;
		}
		IAugmentedBarrelEntity barrelEntity = (IAugmentedBarrelEntity) (Object) entity;
		if (barrelEntity.getType().isExpansionType()) {
			barrelEntity.onInventoryChange();
		}
		if (barrelEntity.getType().isShopType()) {
			((IShopBarrelEntity) barrelEntity).getShopMerchant().forceCloseShop();
		}
	}
}
