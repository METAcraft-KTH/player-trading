package se.leddy231.playertrading;

import java.util.UUID;

import net.minecraft.entity.player.PlayerEntity;

public interface AugmentedBarrelEntity {
    public abstract boolean isShop();

    public abstract UUID getShopOwner();

    public abstract ShopMerchant getShopMerchant();

    public abstract void tryCreateShop(PlayerEntity player);

    public abstract void onInventoryChange();
}
