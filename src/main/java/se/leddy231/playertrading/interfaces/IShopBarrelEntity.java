package se.leddy231.playertrading.interfaces;

import se.leddy231.playertrading.ShopExpansion;
import se.leddy231.playertrading.ShopMerchant;

public interface IShopBarrelEntity extends IExpansionBarrelEntity {

    public abstract ShopMerchant getShopMerchant();

    public abstract void linkToExpansion(ShopExpansion expansion);

    public abstract void onInventoryChange();
}
