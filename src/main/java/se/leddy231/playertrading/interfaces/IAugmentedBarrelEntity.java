package se.leddy231.playertrading.interfaces;

import java.util.UUID;

import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import se.leddy231.playertrading.BarrelType;

public interface IAugmentedBarrelEntity {

    public abstract BarrelType getType();

    public abstract UUID getOwner();

    public abstract void activate(PlayerEntity player, BarrelType signType);

    public abstract void onInventoryChange();

    public abstract IShopBarrelEntity findConnectedShop();

    public abstract boolean isBarrelOpen();

    public abstract BarrelBlockEntity getEntity();
}
