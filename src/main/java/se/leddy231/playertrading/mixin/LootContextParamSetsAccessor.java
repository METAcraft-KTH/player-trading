package se.leddy231.playertrading.mixin;

import com.google.common.collect.BiMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.throwables.MixinError;

@Mixin(LootContextParamSets.class)
public interface LootContextParamSetsAccessor {

	@Accessor("REGISTRY")
	static BiMap<ResourceLocation, ContextKeySet> getRegistry() {
		throw new MixinError("Unable to apply mixin");
	}

}
