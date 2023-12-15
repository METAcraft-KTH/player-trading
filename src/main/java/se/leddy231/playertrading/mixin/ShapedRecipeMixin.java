package se.leddy231.playertrading.mixin;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.leddy231.playertrading.ShopBlock;

@Mixin(ShapedRecipe.class)
public class ShapedRecipeMixin {

    @Inject(method = "getResultItem", at = @At("RETURN"), cancellable = true)
    void getResultItem(RegistryAccess registryAccess, CallbackInfoReturnable<ItemStack> ci) {
        var stack = ci.getReturnValue();
        var recipe = (ShapedRecipe) (Object) this;
        if (recipe.getGroup().equals("playertrading") && stack.getItem() == Items.PLAYER_HEAD) {
            ci.setReturnValue(ShopBlock.SHOP_HEAD_BLOCK.copy());
            ci.cancel();
        }
    }
}
