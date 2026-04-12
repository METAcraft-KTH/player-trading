package se.leddy231.playertrading;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

public class DebugStick {

    public static final ItemStackTemplate DEBUG_STICK = new ItemStackTemplate(
            Items.STICK, DataComponentPatch.builder().set(
                    DataComponents.ITEM_NAME,
                    Component.translatableWithFallback(
                            "item.playertrading.shop_debug_stick",
                            "Shop debug stick"
                    ).withStyle(style -> style.withItalic(false))
            ).set(
                    DataComponents.CUSTOM_DATA,
                    CustomData.EMPTY.update(nbt -> {
                        nbt.putString("Shop", "Debug stick");
                    })
            ).build()
    );
    public static final ItemStackTemplate ADMIN_STICK = new ItemStackTemplate(
            Items.STICK, DataComponentPatch.builder().set(
                    DataComponents.ITEM_NAME,
                    Component.translatableWithFallback(
                            "item.playertrading.admin_shop_debug_stick",
                            "Make admin shop stick"
                    ).withStyle(style -> style.withItalic(false))
            ).set(
                    DataComponents.CUSTOM_DATA,
                    CustomData.EMPTY.update(nbt -> {
                        nbt.putString("Shop", "Make admin shop stick");
                    })
            ).build()
    );
    public static final ItemStackTemplate SINGLE_USE_STICK = new ItemStackTemplate(
            Items.STICK, DataComponentPatch.builder().set(
                    DataComponents.ITEM_NAME,
                    Component.translatableWithFallback(
                            "item.playertrading.single_use_shop_debug_stick",
                            "Make single use shop stick"
                    ).withStyle(style -> style.withItalic(false))
            ).set(
                    DataComponents.CUSTOM_DATA,
                    CustomData.EMPTY.update(nbt -> {
                        nbt.putString("Shop", "Make single use shop stick");
                    })
            ).build()
    );

    private static boolean matches(ItemStack stack, ItemStackTemplate template) {
        if (stack.is(template.item())) {
            return stack.get(DataComponents.CUSTOM_DATA).matchedBy(template.get(DataComponents.CUSTOM_DATA).copyTag());
        }
        return false;
    }

    public static boolean isDebugStick(ItemStack stack) {
        return matches(stack, DebugStick.DEBUG_STICK);
    }

    public static boolean isMakeAdminStick(ItemStack stack) {
        return matches(stack, DebugStick.ADMIN_STICK);

    }

    public static boolean isMakeSingleUseStick(ItemStack stack) {
        return matches(stack, DebugStick.SINGLE_USE_STICK);
    }

    public static int giveDebugStick(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ItemStack stack = DEBUG_STICK.create();
        source.getPlayer().addItem(stack);
        return 0;
    }

    public static int giveAdminStick(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ItemStack stack = ADMIN_STICK.create();
        source.getPlayer().addItem(stack);
        return 0;
    }

    public static int giveSingleUseStick(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ItemStack stack = SINGLE_USE_STICK.create();
        source.getPlayer().addItem(stack);
        return 0;
    }
}
