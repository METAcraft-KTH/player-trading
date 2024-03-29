package se.leddy231.playertrading.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.leddy231.playertrading.ShopBlock;
import se.leddy231.playertrading.Utils;
import se.leddy231.playertrading.interfaces.IBarrelEntity;
import se.leddy231.playertrading.interfaces.ISkullEntity;
import se.leddy231.playertrading.shop.Shop;

@Mixin(SkullBlockEntity.class)
public class SkullEntityMixin implements ISkullEntity {

    private Shop shop;

    SkullBlockEntity entity() {
        return (SkullBlockEntity) (Object) this;
    }

    @Nullable
    public Shop getShop() {
        return shop;
    }

    public InteractionResult onUse(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit
    ) {
        if (shop == null || hand == InteractionHand.OFF_HAND) {
            return InteractionResult.PASS;
        }
        return shop.onSkullUse(player);
    }

    public void onPlace(
            Level level, BlockPos pos, BlockState state, LivingEntity placer
    ) {
        //check so the placer is a player
        if (!(placer instanceof Player player)) {
            return;
        }

        if (!ShopBlock.isShopBlock(entity())) {
            return;
        }

        var barrelPos = ISkullEntity.attachedToPosition(entity());
        if (!(level.getBlockEntity(barrelPos) instanceof BarrelBlockEntity barrelEntity)) {
            return;
        }

        var existingShop = IBarrelEntity.getConnectedShop(barrelEntity);
        if (existingShop != null) {
            Utils.sendMessage(player, Component.translatableWithFallback(
                    "message.playertrading.already_shop", "This barrel is already a shop."
            ));
            return;
        }

        shop = new Shop(player.getUUID(), entity());
        Utils.sendMessage(player, Component.translatableWithFallback(
                "message.playertrading.created",
                shop.shopType.typeName().getString() + " created", shop.shopType.typeName()
        ));
    }

    @Inject(at = @At("RETURN"), method = "saveAdditional")
    public void onNbtWrite(CompoundTag tag, CallbackInfo callback) {
        if (shop != null) {
            shop.saveAsTag(tag);
        }
    }

    @Inject(at = @At("RETURN"), method = "load")
    public void onNbtRead(CompoundTag tag, CallbackInfo callback) {
        shop = Shop.loadFromTag(tag, entity());
    }
}
