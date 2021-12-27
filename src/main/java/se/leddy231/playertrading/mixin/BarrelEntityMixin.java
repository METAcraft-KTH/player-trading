package se.leddy231.playertrading.mixin;

import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.block.BarrelBlock;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.World;
import se.leddy231.playertrading.*;
import se.leddy231.playertrading.interfaces.IExpansionBarrelEntity;
import se.leddy231.playertrading.interfaces.IShopBarrelEntity;

@Mixin(BarrelBlockEntity.class)
public class BarrelEntityMixin implements IShopBarrelEntity {
    private static final String SHOP_OWNER_NBT_TAG = "shop_owner";
    private static final String TYPE_NBT_TAG = "shop_barrely_type";
    private static final Vec3i[] neighbours = new Vec3i[] {
            new Vec3i(1, 0, 0),
            new Vec3i(0, 1, 0),
            new Vec3i(0, 0, 1),
            new Vec3i(-1, 0, 0),
            new Vec3i(0, -1, 0),
            new Vec3i(0, 0, -1),
    };
    public UUID owner;
    public ShopMerchant shopMerchant;
    public IShopBarrelEntity shop;
    public BarrelType type = BarrelType.NONE;
    public ShopExpansion output;
    public ShopExpansion stock;

    @Override
    public BarrelType getType() {
        return type;
    };

    @Override
    public UUID getOwner() {
        return owner;
    }

    @Override
    public IShopBarrelEntity getShop() {
        return shop;
    }

    @Override
    public ShopMerchant getShopMerchant() {
        if (type != BarrelType.SHOP) {
            return null;
        }
        if (shopMerchant == null) {
            shopMerchant = new ShopMerchant((BarrelBlockEntity) (Object) this);
        }
        return shopMerchant;
    }

    public void sendMessage(PlayerEntity player, String text) {
        player.sendMessage(new LiteralText(text), false);
    }

    @Override
    public void tryCreate(PlayerEntity player, BarrelType newType) {
        if (type != BarrelType.NONE) {
            if (owner == player.getUuid()) {
                playerTroubleshoot(player);
            } else {
                sendMessage(player, "Someone already owns this");
            }
        }
        owner = player.getUuid();
        type = newType;
        BarrelBlockEntity entity = (BarrelBlockEntity) (Object) this;
        entity.markDirty();
        switch (type) {
            case NONE:
                sendMessage(player, "This is not a shop barrel");
                break;
            case SHOP:
                sendMessage(player, "Shop created");
                break;
            case OUTPUT:
                sendMessage(player, "Output barrel created");
                break;
            case STOCK:
                sendMessage(player, "Stock barrel created");
                break;
        }
        if (type.isExpansionType()) {
            linkExpansions(player);
        }

    }

    public void playerTroubleshoot(PlayerEntity player) {
        switch (type) {
            case NONE:
                sendMessage(player, "This is not a shop barrel");
                break;
            case SHOP:
                checkTrades(player);
                break;
            case OUTPUT:
                sendMessage(player, "You own this output barrel");
                break;
            case STOCK:
                sendMessage(player, "You own this stock barrel");
                break;
        }
    }

    public void checkTrades(PlayerEntity player) {
        TradeOfferList offers = getShopMerchant().getOffers();
        boolean allValid = true;
        for (TradeOffer offer : offers) {
            ShopTradeOffer shopOffer = (ShopTradeOffer) offer;
            if (shopOffer.valid)
                continue;
            allValid = false;
            int slot = shopOffer.tradeChestInventoryIndex + 1;
            sendMessage(player,
                    "Offer at slot " + slot + "-" + (slot + 2) + " is invalid because " + shopOffer.invalidReason);
        }
        if (allValid) {
            sendMessage(player, "All trades are valid");
        }
    }

    public void linkExpansions(PlayerEntity player) {
        if (!type.isExpansionType())
            return;

        BarrelBlockEntity entity = (BarrelBlockEntity) (Object) this;
        World world = entity.getWorld();
        BlockPos pos = entity.getPos();
        for (Vec3i offset : neighbours) {
            BlockPos newPos = pos.add(offset);
            if (!(world.getBlockState(newPos).getBlock() instanceof BarrelBlock))
                continue;
            IExpansionBarrelEntity barrelEntity = (IExpansionBarrelEntity) world.getBlockEntity(newPos);
            if (barrelEntity.getType() != BarrelType.SHOP)
                continue;
            IShopBarrelEntity shop = (IShopBarrelEntity) barrelEntity;
            ShopExpansion expansion = new ShopExpansion(offset, this);
            shop.linkToExpansion(expansion);
            sendMessage(player, "Shop at " + newPos + " linked to " + type.typeName() + " at " + pos);
        }
    }

    public void linkToExpansion(ShopExpansion expansion) {
        if (type != BarrelType.SHOP || !expansion.entity.getType().isExpansionType())
            return;

        if (expansion.entity.getType() == BarrelType.OUTPUT) {
            output = expansion;
        } else {
            stock = expansion;
        }
        onInventoryChange();
    }

    @Override
    public void onInventoryChange() {
        if (type == BarrelType.SHOP && owner != null && shopMerchant != null && shopMerchant.currentCustomer != null) {
            shopMerchant.refreshTrades();
        }
    }

    @Inject(at = @At("RETURN"), method = "writeNbt")
    public void onNbtWrite(NbtCompound tag, CallbackInfo callback) {
        if (owner != null) {
            tag.putUuid(SHOP_OWNER_NBT_TAG, owner);
        }
        if (type != BarrelType.NONE) {
            tag.putInt(TYPE_NBT_TAG, type.toInt());
        }
    }

    @Inject(at = @At("RETURN"), method = "readNbt")
    public void onNbtRead(NbtCompound tag, CallbackInfo callback) {
        if (tag.containsUuid(SHOP_OWNER_NBT_TAG)) {
            owner = tag.getUuid(SHOP_OWNER_NBT_TAG);
        }
        if (tag.contains(TYPE_NBT_TAG)) {
            type = BarrelType.fromInt(tag.getInt(TYPE_NBT_TAG));
        }

    }
}
