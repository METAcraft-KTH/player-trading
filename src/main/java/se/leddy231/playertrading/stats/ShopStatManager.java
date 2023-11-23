package se.leddy231.playertrading.stats;

import java.util.*;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import se.leddy231.playertrading.PlayerTrading;

public class ShopStatManager {
    
    private static ShopStatManager instance;

    private ShopStatManager() {}

    public static ShopStatManager getInstance() {
        if (instance == null) {
            instance = new ShopStatManager();
        }
        return instance;
    }

    private Map<String, ShopStat> shops = new HashMap<>();

    public void addStat(ShopStat stat) {
        shops.put(stat.hashString(), stat);
    }

    public int displayStats(CommandContext<ServerCommandSource> context) {
        PlayerTrading.LOGGER.info(shops);
        ServerCommandSource source = context.getSource();
        for (ShopStat stat : shops.values()) {
            source.sendFeedback(new LiteralText(stat.toString()), false);
            
        }
        return 0;
    }
}
