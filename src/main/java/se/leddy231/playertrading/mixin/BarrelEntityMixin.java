package se.leddy231.playertrading.mixin;

import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import se.leddy231.playertrading.*;

@Mixin(BarrelBlockEntity.class)
public class BarrelEntityMixin implements AugmentedBarrelEntity{
    private static final String SHOP_OWNER_NBT_TAG = "shop_owner";
    public UUID shopOwner;
    public ShopMerchant shopMerchant;

    @Override
    public boolean isShop() { return shopOwner != null; }
    @Override
    public UUID getShopOwner() { return shopOwner; }
    @Override
    public ShopMerchant getShopMerchant() {
        if (!isShop()) {
            return null;
        }
        if (shopMerchant == null) {
            shopMerchant = new ShopMerchant((BarrelBlockEntity) (Object) this);
        }
        return shopMerchant;
    }
    @Override
    public void tryCreateShop(PlayerEntity player) {
        if (isShop()) {
            if (shopOwner == player.getUuid()) {
                checkTrades(player);
            } else {
                player.sendMessage(new LiteralText("Someone already owns this shop"), false);
            }
        }
        shopOwner = player.getUuid();
        BarrelBlockEntity entity = (BarrelBlockEntity) (Object) this;
        entity.markDirty();
        player.sendMessage(new LiteralText("Shop created"), false);
    }

    public void checkTrades(PlayerEntity player) {
        TradeOfferList offers = getShopMerchant().getOffers();
        boolean allValid = true;
        for (TradeOffer offer : offers) {
            ShopTradeOffer shopOffer = (ShopTradeOffer) offer;
            if(shopOffer.valid)
                continue;
            allValid = false;
            int slot = shopOffer.tradeChestInventoryIndex + 1;
            player.sendMessage(new LiteralText("Offer at slot " + slot + "-" + (slot + 2) + " is invalid because " + shopOffer.invalidReason), false);
        }
        if(allValid) {
            player.sendMessage(new LiteralText("All trades are valid"), false);
        }
    }

    @Override
    public void onInventoryChange() {
        if (shopOwner != null && shopMerchant != null && shopMerchant.currentCustomer != null) {
            shopMerchant.refreshTrades();
        }
    }

    @Inject(at = @At("RETURN"), method = "writeNbt")
    public void onNbtWrite(NbtCompound tag, CallbackInfo callback) {
        if (shopOwner != null) {
            tag.putUuid(SHOP_OWNER_NBT_TAG, shopOwner);
        }
    }
    
    @Inject(at = @At("RETURN"), method = "readNbt")
    public void onNbtRead(NbtCompound tag, CallbackInfo callback) {
        if(tag.containsUuid(SHOP_OWNER_NBT_TAG)) {
            shopOwner = tag.getUuid(SHOP_OWNER_NBT_TAG);
        }

    }
}
