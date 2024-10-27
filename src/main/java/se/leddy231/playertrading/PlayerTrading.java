package se.leddy231.playertrading;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PlayerTrading implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("data/playertrading");
    public static final String MODID = "leddy231-playertrading";

    @Override
    public void onInitialize() {
        DebugStick.initStick();
        ShopBlock.initShopBlock();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            Commands.registerCommands(dispatcher);
        });

        LOGGER.info("Loaded Player Trading by Leddy231");
    }
}