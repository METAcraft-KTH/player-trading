package se.leddy231.playertrading.interfaces;

import se.leddy231.playertrading.ShopMerchant;

public interface IShopBarrelEntity extends IAugmentedBarrelEntity {

    public abstract ShopMerchant getShopMerchant();

    public abstract IAugmentedBarrelEntity getOutputBarrel();

    public abstract IAugmentedBarrelEntity getStockBarrel();

    public abstract IShopBarrelEntity getShopBarrel();

    public abstract boolean isAnyBarrelOpen();
}
