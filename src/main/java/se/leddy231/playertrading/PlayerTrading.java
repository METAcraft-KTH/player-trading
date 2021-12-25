package se.leddy231.playertrading;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PlayerTrading implements ModInitializer {
	public static final Logger LOGGER = LogManager.getLogger("playertrading");

	@Override
	public void onInitialize() {
		LOGGER.info("Loaded Player Trading by Leddy231");
	}
}
