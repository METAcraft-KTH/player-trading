package se.leddy231.playertrading.interfaces;

import se.leddy231.playertrading.ShopMerchant;

public interface IShopBarrelEntity extends IAugmentedBarrelEntity {

    ShopMerchant getShopMerchant();

    IAugmentedBarrelEntity getOutputBarrel();

    IAugmentedBarrelEntity getStockBarrel();

    IShopBarrelEntity getShopBarrel();

    boolean isAnyBarrelOpen();
}
