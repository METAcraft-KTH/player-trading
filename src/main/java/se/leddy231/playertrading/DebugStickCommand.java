package se.leddy231.playertrading;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

public class DebugStickCommand {

    public static final ItemStack STICK = new ItemStack(Items.STICK, 1);

    public static void initStick() {
        STICK.setCustomName(new LiteralText("Shop debug stick"));
        STICK.setSubNbt("Shop", NbtString.of("Debug stick"));
    }

    public static int run(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        PlayerEntity player;
        try {
            player = source.getPlayer();
        } catch (CommandSyntaxException e) {
            source.sendError(new LiteralText("You are not a player, you can not get a debug stick"));
            return 0;
        }
        ItemStack stack = STICK.copy();
        player.giveItemStack(stack);
        return 0;
    }
}
