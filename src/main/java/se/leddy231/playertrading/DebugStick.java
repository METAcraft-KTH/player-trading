package se.leddy231.playertrading;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class DebugStick {

    public static final ItemStack STICK = new ItemStack(Items.STICK, 1);

    public static boolean isStick(ItemStack stack) {
        return ItemStack.isSameItemSameTags(stack, DebugStick.STICK);
    }

    public static void initStick() {
        STICK.setHoverName(Component.literal("Shop debug stick"));
        STICK.addTagElement("Shop", StringTag.valueOf("Debug stick"));
    }

    public static int runCommand(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Player player;
        player = source.getPlayer();
        ItemStack stack = STICK.copy();
        player.addItem(stack);
        return 0;
    }
}
