package se.leddy231.playertrading.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.V1460;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.function.Supplier;

@Mixin(V1460.class)
public class V1460Mixin {

	@Inject(
		method = "registerBlockEntities",
		at = @At("RETURN")
	)
	public void registerBlockEntities(
			Schema schema, CallbackInfoReturnable<Map<String, Supplier<TypeTemplate>>> cir,
			@Local Map<String, Supplier<TypeTemplate>> map
	) {
		String id = "minecraft:skull";
		var current = map.get(id).get();
		map.put(id, () -> DSL.and(
				DSL.optional(DSL.field(
					"shop_config", DSL.optionalFields(
						"Items",
						DSL.list(References.ITEM_STACK.in(schema))
					)
				)), current
		));
	}

}
