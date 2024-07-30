package se.leddy231.playertrading;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

import java.util.Map;

public class DataGen implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		fabricDataGenerator.createPack().addProvider((output, registries) -> {
			return new RecipeProvider(output, registries) {
				@Override
				public void buildRecipes(RecipeOutput recipeOutput) {
					var shopBlock = ResourceLocation.fromNamespaceAndPath("playertrading", "shop_block");
					Advancement.Builder builder = recipeOutput.advancement().addCriterion(
							"has_the_recipe", RecipeUnlockedTrigger.unlocked(shopBlock)
					).rewards(AdvancementRewards.Builder.recipe(shopBlock)).requirements(
							AdvancementRequirements.Strategy.OR
					);
					builder.addCriterion(RecipeProvider.getHasName(Items.BARREL), RecipeProvider.has(Items.BARREL));
					recipeOutput.accept(
							shopBlock,
							new ShapedRecipe(
									"playertrading",
									CraftingBookCategory.MISC,
									ShapedRecipePattern.of(
											Map.of(
													'A', Ingredient.of(ItemTags.WOOL),
													'B', Ingredient.of(ItemTags.PLANKS),
													'C', Ingredient.of(Items.BARREL),
													'D', Ingredient.of(Items.IRON_INGOT)
											),
											"AAA",
											"BCB",
											"BDB"
									),
									ShopBlock.SHOP_HEAD_BLOCK.copy()
							),
							builder.build(shopBlock.withPrefix("recipes/" + RecipeCategory.MISC.getFolderName() + "/"))
					);
				}
			};
		});
	}
}
