package se.leddy231.playertrading.interfaces;

import net.minecraft.block.entity.BarrelBlockEntity;
import se.leddy231.playertrading.ShopMerchant;

public interface IShopBarrelEntity extends IAugmentedBarrelEntity {

    public abstract ShopMerchant getShopMerchant();

    public abstract BarrelBlockEntity getOutputBarrel();

    public abstract BarrelBlockEntity getStockBarrel();

    public abstract BarrelBlockEntity getShopBarrel();
}
