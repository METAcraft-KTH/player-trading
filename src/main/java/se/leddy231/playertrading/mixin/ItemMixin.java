package se.leddy231.playertrading.mixin;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.leddy231.playertrading.interfaces.IBarrelEntity;
import se.leddy231.playertrading.interfaces.ISkullEntity;

@Mixin(Item.class)
public class ItemMixin {

    @Inject(at = @At("RETURN"), method = "useOn")
    public void useOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> ci) {
        var entity = context.getLevel().getBlockEntity(context.getClickedPos());
        if (entity instanceof BarrelBlockEntity barrelEntity) {
            var shop = IBarrelEntity.getConnectedShop(barrelEntity);
            if (shop != null) {
                shop.onBarrelUse(context.getPlayer());
            }
        }
        if (entity instanceof SkullBlockEntity skullEntity) {
            var shop = ((ISkullEntity) skullEntity).player_trading$getShop();
            if (shop != null) {
                shop.onSkullUse(context.getPlayer());
            }
        }
    }
}
