package se.leddy231.playertrading;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class DebugStickCommand {

    public static final ItemStack STICK = new ItemStack(Items.STICK, 1);

    public static void initStick() {
        STICK.setHoverName(Component.literal("Shop debug stick"));
        STICK.addTagElement("Shop", StringTag.valueOf("Debug stick"));
    }

    public static int run(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Player player;
        player = source.getPlayer();
        ItemStack stack = STICK.copy();
        player.addItem(stack);
        return 0;
    }
}
