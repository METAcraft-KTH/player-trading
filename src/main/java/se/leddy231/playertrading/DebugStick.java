package se.leddy231.playertrading;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

public class DebugStick {

    public static final ItemStack DEBUG_STICK = new ItemStack(Items.STICK, 1);
    public static final ItemStack ADMIN_STICK = new ItemStack(Items.STICK, 1);
    public static final ItemStack SINGLE_USE_STICK = new ItemStack(Items.STICK, 1);

    public static boolean isDebugStick(ItemStack stack) {
        return ItemStack.isSameItemSameComponents(stack, DebugStick.DEBUG_STICK);
    }

    public static boolean isMakeAdminStick(ItemStack stack) {
        return ItemStack.isSameItemSameComponents(stack, DebugStick.ADMIN_STICK);

    }

    public static boolean isMakeSingleUseStick(ItemStack stack) {
        return ItemStack.isSameItemSameComponents(stack, DebugStick.SINGLE_USE_STICK);
    }

    public static void initStick() {
        DEBUG_STICK.set(
                DataComponents.ITEM_NAME,
                Component.translatableWithFallback(
                "item.playertrading.shop_debug_stick",
                "Shop debug stick"
        ).withStyle(style -> style.withItalic(false)));
        DEBUG_STICK.set(
                DataComponents.CUSTOM_DATA,
                DEBUG_STICK.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).update(nbt -> {
                    nbt.putString("Shop", "Debug stick");
                })
        );

        ADMIN_STICK.set(
                DataComponents.ITEM_NAME,
                Component.translatableWithFallback(
                "item.playertrading.admin_shop_debug_stick",
                "Make admin shop stick"
        ).withStyle(style -> style.withItalic(false)));
        ADMIN_STICK.set(
                DataComponents.CUSTOM_DATA,
                ADMIN_STICK.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).update(nbt -> {
                    nbt.putString("Shop", "Make admin shop stick");
                })
        );

        SINGLE_USE_STICK.set(
                DataComponents.ITEM_NAME,
                Component.translatableWithFallback(
                "item.playertrading.single_use_shop_debug_stick",
                "Make single use shop stick"
        ).withStyle(style -> style.withItalic(false)));
        SINGLE_USE_STICK.set(
                DataComponents.CUSTOM_DATA,
                SINGLE_USE_STICK.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).update(nbt -> {
                    nbt.putString("Shop", "Make single use shop stick");
                })
        );
    }

    public static int giveDebugStick(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ItemStack stack = DEBUG_STICK.copy();
        source.getPlayer().addItem(stack);
        return 0;
    }

    public static int giveAdminStick(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ItemStack stack = ADMIN_STICK.copy();
        source.getPlayer().addItem(stack);
        return 0;
    }

    public static int giveSingleUseStick(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ItemStack stack = SINGLE_USE_STICK.copy();
        source.getPlayer().addItem(stack);
        return 0;
    }
}
