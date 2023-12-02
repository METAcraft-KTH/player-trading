package se.leddy231.playertrading.interfaces;

import java.util.UUID;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import se.leddy231.playertrading.BarrelType;

public interface IAugmentedBarrelEntity {

    public abstract BarrelType getType();

    public abstract UUID getOwner();

    public abstract void activate(Player player, BarrelType signType);

    public abstract void onInventoryChange();

    public abstract IShopBarrelEntity findConnectedShop();

    public abstract boolean isBarrelOpen();

    public abstract BarrelBlockEntity getEntity();
}
