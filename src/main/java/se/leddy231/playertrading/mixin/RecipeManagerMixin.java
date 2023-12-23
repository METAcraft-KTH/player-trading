package se.leddy231.playertrading.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.ShapedRecipe;
import se.leddy231.playertrading.PlayerTrading;
import se.leddy231.playertrading.ShopBlock;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {

    @Inject(method = "fromJson", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/RecipeHolder;<init>(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/world/item/crafting/Recipe;)V"))
    private static void deserialize(
            ResourceLocation id, JsonObject json, CallbackInfoReturnable<RecipeHolder<?>> cir,
            @Local Recipe<?> recipe) {
        if (id.getNamespace().equals("playertrading")) {
            var result = recipe.getResultItem(null);
            if (result != null) {
                ShopBlock.makeIntoShopBlock(result);
            }
        }
    }

}