package se.leddy231.playertrading.interfaces;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import se.leddy231.playertrading.BarrelType;

import java.util.UUID;

public interface IAugmentedBarrelEntity {

    BarrelType getType();

    UUID getOwner();

    void activate(Player player, BarrelType signType);

    void playerTroubleshoot(Player player);

    void onInventoryChange();

    IShopBarrelEntity findConnectedShop();

    boolean isBarrelOpen();

    BarrelBlockEntity getEntity();
}
