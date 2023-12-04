package se.leddy231.playertrading.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.leddy231.playertrading.BarrelType;
import se.leddy231.playertrading.DebugStick;
import se.leddy231.playertrading.PlayerTrading;
import se.leddy231.playertrading.ShopKey;
import se.leddy231.playertrading.interfaces.IAugmentedBarrelEntity;
import se.leddy231.playertrading.interfaces.IShopBarrelEntity;

@Mixin(BarrelBlock.class)
public class BarrelBlockMixin {
    @Inject(at = @At("HEAD"), method = "use", cancellable = true)
    private void onUse(
            BlockState state,
            Level world,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit,
            CallbackInfoReturnable<InteractionResult> ci
    ) {
        BlockEntity entity = world.getBlockEntity(pos);
        if (!(entity instanceof BarrelBlockEntity)) {
            PlayerTrading.LOGGER.error("Somehow, a BarrelBlockEntity was not returned from a BarrelBlock");
            return;
        }
        var barrelEntity = (IAugmentedBarrelEntity) entity;

        if (barrelEntity.getType() == BarrelType.NONE) {
            return;
        }

        var usedItem = player.getMainHandItem();
        var isOwner = barrelEntity.getOwner().equals(player.getUUID());

        var shouldMakeKey = isOwner && usedItem.is(Items.GOLD_INGOT) && !ShopKey.isKey(usedItem);
        if (shouldMakeKey) {
            ShopKey.makeIntoKeyForPlayer(usedItem, player);
            ci.setReturnValue(InteractionResult.SUCCESS);
            return;
        }

        var shop = barrelEntity.findConnectedShop();
        if (shop == null) {
            return;
        }

        boolean hasKey = ShopKey.isKeyForUUID(usedItem, barrelEntity.getOwner());
        if (player.isShiftKeyDown() && (isOwner || hasKey)) {
            shop.playerTroubleshoot(player);
            ci.setReturnValue(InteractionResult.SUCCESS);
            return;
        }


        boolean opDebugBypass = player.hasPermissions(4) && DebugStick.isStick(usedItem);
        if (isOwner || hasKey || opDebugBypass) {
            //Close the trade window if a customer is using the shop
            shop.getShopMerchant().forceCloseShop();
            return;
        }

        ci.setReturnValue(InteractionResult.SUCCESS);
        shop.getShopMerchant().openShop(player);
    }

    // Update inventory on block break
    @Inject(at = @At("HEAD"), method = "onRemove")
    private void onRemove(
            BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved, CallbackInfo ci
    ) {
        if (state.is(newState.getBlock())) {
            //Ignore state changes when for example changing texture to show the barrel being open.
            return;
        }
        BlockEntity entity = world.getBlockEntity(pos);
        if (!(entity instanceof BarrelBlockEntity)) {
            return;
        }
        IAugmentedBarrelEntity barrelEntity = (IAugmentedBarrelEntity) entity;
        if (barrelEntity.getType().isExpansionType()) {
            barrelEntity.onInventoryChange();
        }
        if (barrelEntity.getType().isShopType()) {
            ((IShopBarrelEntity) barrelEntity).getShopMerchant().forceCloseShop();
        }
    }
}
