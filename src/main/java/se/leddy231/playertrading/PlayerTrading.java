package se.leddy231.playertrading;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;

import com.mojang.brigadier.CommandDispatcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static net.minecraft.server.command.CommandManager.literal;

public class PlayerTrading implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("playertrading");

	public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, Boolean dedicated) {
		dispatcher.register(literal("shop")
				.then(literal("debug").requires(source -> source.hasPermissionLevel(4))
						.executes(context -> DebugStickCommand.run(context)))

		);
	}

	@Override
	public void onInitialize() {
		DebugStickCommand.initStick();
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> registerCommands(dispatcher, dedicated));

		LOGGER.info("Loaded Player Trading by Leddy231");
	}
}