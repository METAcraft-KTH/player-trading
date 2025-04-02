package se.leddy231.playertrading.criteria;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import se.leddy231.playertrading.PlayerTrading;
import se.leddy231.playertrading.mixin.LootContextParamSetsAccessor;

import java.util.function.Consumer;

public class ShopCriteriaTriggers {

	public static final ContextKeySet SHOP_CONTEXT = registerKeySet(
			"shop", builder -> builder
					.required(LootContextParams.THIS_ENTITY)
					.required(LootContextParams.ORIGIN)
					.required(LootContextParams.BLOCK_STATE)
					.required(LootContextParams.BLOCK_ENTITY)
	);


	public static final ShopTradeTrigger TRADE = register("shop_trade", new ShopTradeTrigger());


	public static void init() {

	}


	public static <T extends CriterionTrigger<?>> T register(String string, T criterionTrigger) {
		return Registry.register(
				BuiltInRegistries.TRIGGER_TYPES,
				ResourceLocation.fromNamespaceAndPath(PlayerTrading.MODID, string),
				criterionTrigger
		);
	}

	private static ContextKeySet registerKeySet(String string, Consumer<ContextKeySet.Builder> consumer) {
		ContextKeySet.Builder builder = new ContextKeySet.Builder();
		consumer.accept(builder);
		ContextKeySet contextKeySet = builder.build();
		ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath(PlayerTrading.MODID, string);
		ContextKeySet contextKeySet2 = LootContextParamSetsAccessor.getRegistry().put(resourceLocation, contextKeySet);
		if (contextKeySet2 != null) {
			throw new IllegalStateException("Loot table parameter set " + resourceLocation + " is already registered");
		} else {
			return contextKeySet;
		}
	}
}
