package se.leddy231.playertrading;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;

public class Commands {
    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        var root = LiteralArgumentBuilder.<CommandSourceStack>literal("shop").requires(source -> source.hasPermission(4));
        root.then(LiteralArgumentBuilder.<CommandSourceStack>literal("debug-stick").executes(DebugStick::giveDebugStick));
        root.then(LiteralArgumentBuilder.<CommandSourceStack>literal("make-admin-stick").executes(DebugStick::giveAdminStick));
        root.then(LiteralArgumentBuilder.<CommandSourceStack>literal("make-single-use-stick").executes(DebugStick::giveSingleUseStick));
        root.then(LiteralArgumentBuilder.<CommandSourceStack>literal("block").executes(ShopBlock::giveShopBlock));
        dispatcher.register(root);
    }
}
