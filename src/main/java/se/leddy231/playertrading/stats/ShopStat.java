package se.leddy231.playertrading.stats;

import java.util.List;
import java.util.UUID;

import net.minecraft.util.math.BlockPos;
import net.minecraft.village.TradeOffer;

public class ShopStat {
 
    public BlockPos pos;
    public UUID owner;
    public List<TradeOffer> offers;

    public ShopStat(BlockPos pos, UUID owner, List<TradeOffer> offers) {
        this.pos = pos;
        this.owner = owner;
        this.offers = offers;
    }

    public String hashString() {
        return pos.toString();
    }

    public String toString() {
        String ret = pos.toString() + " " + owner.toString();
        for (TradeOffer tradeOffer : offers) {
            ret += tradeOffer.toString() + "\n";
        }
        return ret;
    }
}
