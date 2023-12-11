package se.leddy231.playertrading;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

public class PlayerTrading implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("playertrading");
    public static UUID SHOP_BLOCK_UUID = UUID.fromString("8df67171-1b9a-4eae-b35d-b03a56f8dacb");


    @Override
    public void onInitialize() {
        DebugStick.initStick();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            Commands.registerCommands(dispatcher);
        });

        LOGGER.info("Loaded Player Trading by Leddy231");
    }
}