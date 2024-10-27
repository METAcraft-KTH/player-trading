package se.leddy231.playertrading;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

import java.util.Map;
import java.util.Optional;

public class DataGen implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		fabricDataGenerator.createPack().addProvider(
				(output, registries) -> new FabricRecipeProvider(output, registries) {
					@Override
					protected RecipeProvider createRecipeProvider(HolderLookup.Provider provider, RecipeOutput recipeOutput) {
						return new RecipeProvider(provider, recipeOutput) {
							@Override
							public void buildRecipes() {
								var shopBlock = ResourceKey.create(
										Registries.RECIPE,
										ResourceLocation.fromNamespaceAndPath(PlayerTrading.MODID, "shop_block")
								);
								Advancement.Builder builder = recipeOutput.advancement().addCriterion(
										"has_the_recipe", RecipeUnlockedTrigger.unlocked(shopBlock)
								).rewards(AdvancementRewards.Builder.recipe(shopBlock)).requirements(
										AdvancementRequirements.Strategy.OR
								);
								builder.addCriterion(
										"trigger_always",
										CriteriaTriggers.TICK.createCriterion(new PlayerTrigger.TriggerInstance(
												Optional.empty()
										))
								);
								recipeOutput.accept(
										shopBlock,
										new ShapedRecipe(
												"playertrading",
												CraftingBookCategory.MISC,
												ShapedRecipePattern.of(
														Map.of(
																'A', tag(ItemTags.WOOL),
																'B', tag(ItemTags.PLANKS),
																'C', Ingredient.of(Items.BARREL),
																'D', Ingredient.of(Items.IRON_INGOT)
														),
														"AAA",
														"BCB",
														"BDB"
												),
												ShopBlock.SHOP_HEAD_BLOCK.copy()
										),
										builder.build(shopBlock.location().withPrefix("recipes/" + RecipeCategory.MISC.getFolderName() + "/"))
								);
							}
						};
					}

					@Override
					public String getName() {
						return "";
					}
				}
		);
	}
}
