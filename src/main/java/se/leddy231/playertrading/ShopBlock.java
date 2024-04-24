package se.leddy231.playertrading;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

import java.util.Optional;
import java.util.UUID;

public class ShopBlock {
    public static final ItemStack SHOP_HEAD_BLOCK = new ItemStack(Items.PLAYER_HEAD, 1);
    public static final UUID SHOP_BLOCK_UUID = UUID.fromString("8df67171-1b9a-4eae-b35d-b03a56f8dacb");
    public static final String texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2UzZGViNTdlYWEyZjRkNDAzYWQ1NzI4M2NlOGI0MTgwNWVlNWI2ZGU5MTJlZTJiNGVhNzM2YTlkMWY0NjVhNyJ9fX0=";

    public static void initShopBlock() {
        makeIntoShopBlock(SHOP_HEAD_BLOCK);
    }

    public static void makeIntoShopBlock(ItemStack item) {
        var propertyMap = new PropertyMap();
        propertyMap.put("textures", new Property("textures", texture));
        item.set(
                DataComponents.ITEM_NAME,
                Component.translatableWithFallback(
                        "item.playertrading.shop", "Shop"
                ).withStyle(style -> style.withItalic(false).withColor(ChatFormatting.WHITE))
        );
        item.set(
                DataComponents.PROFILE,
                new ResolvableProfile(
                        Optional.empty(), Optional.of(SHOP_BLOCK_UUID), propertyMap
                )
        );
    }

    public static boolean isShopBlock(SkullBlockEntity entity) {
        var ownerProfile = entity.getOwnerProfile();
        if (ownerProfile == null || !ownerProfile.id().map(id -> id.equals(SHOP_BLOCK_UUID)).orElse(false)) {
            return false;
        }
        return true;
    }

    public static int giveShopBlock(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ItemStack stack = SHOP_HEAD_BLOCK.copy();
        source.getPlayer().addItem(stack);
        return 0;
    }
}
