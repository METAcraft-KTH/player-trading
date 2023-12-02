package se.leddy231.playertrading;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ModInitializer;

import com.mojang.brigadier.CommandDispatcher;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

public class PlayerTrading implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("playertrading");

	public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(LiteralArgumentBuilder.<CommandSourceStack>literal("shop")
				.then(LiteralArgumentBuilder.<CommandSourceStack>literal("debug").requires(source -> source.hasPermission(4))
						.executes(DebugStickCommand::run))

		);
	}

	@Override
	public void onInitialize() {
		DebugStickCommand.initStick();
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			registerCommands(dispatcher);
		});

		LOGGER.info("Loaded Player Trading by Leddy231");
	}
}