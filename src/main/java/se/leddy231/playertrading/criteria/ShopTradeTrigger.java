package se.leddy231.playertrading.criteria;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import se.leddy231.playertrading.shop.Shop;

import java.util.Optional;

public class ShopTradeTrigger extends SimpleCriterionTrigger<ShopTradeTrigger.TriggerInstance> {

	@Override
	public Codec<TriggerInstance> codec() {
		return TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer serverPlayer, Shop shop, ItemStack itemStack) {
		LootParams lootParams = new LootParams.Builder(serverPlayer.serverLevel())
				.withParameter(LootContextParams.THIS_ENTITY, serverPlayer)
				.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(shop.entity.getBlockPos()))
				.withParameter(LootContextParams.BLOCK_STATE, shop.entity.getBlockState())
				.withParameter(LootContextParams.BLOCK_ENTITY, shop.entity)
				.create(ShopCriteriaTriggers.SHOP_CONTEXT);
		var ctx = new LootContext.Builder(lootParams).create(Optional.empty());
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(ctx, itemStack));
	}

	public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> shop, Optional<ItemPredicate> item)
			implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
				instance -> instance.group(
								EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
								ContextAwarePredicate.CODEC.optionalFieldOf("shop").forGetter(TriggerInstance::shop),
								ItemPredicate.CODEC.optionalFieldOf("item").forGetter(TriggerInstance::item)
						)
						.apply(instance, TriggerInstance::new)
		);

		public boolean matches(LootContext lootContext, ItemStack itemStack) {
			return (this.shop.isEmpty() || this.shop.get().matches(lootContext)) && (this.item.isEmpty() || this.item.get().test(itemStack));
		}

		@Override
		public void validate(CriterionValidator criterionValidator) {
			SimpleCriterionTrigger.SimpleInstance.super.validate(criterionValidator);
			criterionValidator.validateEntity(this.shop, ".shop");
		}
	}

}
