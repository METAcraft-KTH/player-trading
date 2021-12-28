package se.leddy231.playertrading.mixin;

import java.util.UUID;

import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.block.BarrelBlock;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import se.leddy231.playertrading.*;
import se.leddy231.playertrading.interfaces.IAugmentedBarrelEntity;
import se.leddy231.playertrading.interfaces.IShopBarrelEntity;

@Mixin(BarrelBlockEntity.class)
public class BarrelEntityMixin implements IShopBarrelEntity {
    private static final String SHOP_OWNER_NBT_TAG = "shop_owner";
    private static final String TYPE_NBT_TAG = "shop_barrely_type";
    private static final String SHOP_NAME_NBT_TAG = "shop_name";
    public UUID owner;
    public ShopMerchant shopMerchant;
    public BarrelType type = BarrelType.NONE;
    public String shopName;

    @Override
    public BarrelType getType() {
        return type;
    };

    @Override
    public UUID getOwner() {
        return owner;
    }

    @Override
    public String getShopName() { return shopName; }

    @Override
    public BarrelBlockEntity getOutputBarrel() {
        IAugmentedBarrelEntity entity = getNeighbourByType(BarrelType.OUTPUT);
        if (entity != null) {
            return (BarrelBlockEntity) entity;
        }
        return null;
    }

    @Override
    public BarrelBlockEntity getStockBarrel() {
        IAugmentedBarrelEntity entity = getNeighbourByType(BarrelType.STOCK);
        if (entity != null) {
            return (BarrelBlockEntity) entity;
        }
        return null;
    }

    @Override
    public BarrelBlockEntity getShopBarrel() {
        if (type == BarrelType.SHOP) {
            return getEntity();
        }
        return null;
    }

    private BarrelBlockEntity getEntity() {
        return (BarrelBlockEntity) (Object) this;
    }

    @Override
    public ShopMerchant getShopMerchant() {
        if (type != BarrelType.SHOP) {
            return null;
        }
        if (shopMerchant == null) {
            shopMerchant = new ShopMerchant(this);
        }
        return shopMerchant;
    }

    public IAugmentedBarrelEntity getNeighbourByType(BarrelType type) {
        BarrelBlockEntity entity = getEntity();
        World world = entity.getWorld();
        BlockPos pos = entity.getPos();
        for (Direction dir : Direction.values()) {
            BlockPos newPos = pos.offset(dir);
            if (!(world.getBlockState(newPos).getBlock() instanceof BarrelBlock))
                continue;
            IAugmentedBarrelEntity barrelEntity = (IAugmentedBarrelEntity) world.getBlockEntity(newPos);
            if (barrelEntity.getType() == type)
                return barrelEntity;
        }
        return null;
    }

    /**
     * "Activate" will attempt to create
     * a shop if all requirements are
     * met.
     * Requirements:
     * 1. The shop doesn't already have an owner
     * 2. The shop is valid and has all its parts intact
     */
    @Override
    public void activate(PlayerEntity player, BarrelType signType, String shopName) {

        // TODO
        /* Not fully implemented yet
        if (!completeSetup(signType)) {
            Utils.sendMessage(player, (LiteralText) new LiteralText("Incomplete setup, shop must be connected to a stock and an output").formatted(Formatting.RED));
            return;
        }*/

        if (owner != null) {
            if (owner.equals(player.getUuid())) {
                playerTroubleshoot(player);
            } else {
                Utils.sendMessage(player, (LiteralText) new LiteralText("Someone already owns this").formatted(Formatting.RED));
            }
        } else {
            create(player, signType, shopName);
        }
    }

    /**
     * "Create" will instantiate a shop
     * and set its corresponding fields
     */
    public void create(PlayerEntity player, BarrelType newType, String shopName) {
        owner = player.getUuid();
        type = newType;
        this.shopName = shopName;

        BarrelBlockEntity entity = (BarrelBlockEntity) (Object) this;
        entity.markDirty();

        Utils.sendMessage(player, (LiteralText) new LiteralText(newType.typeName() + " barrel created").formatted(Formatting.GREEN));
    }

    public void playerTroubleshoot(PlayerEntity player) {
        Utils.sendMessage(player, (LiteralText) new LiteralText(type.typeName() + " barrel already owned by you").formatted(Formatting.RED));
        if(type == BarrelType.SHOP) {
            BarrelBlockEntity output = getOutputBarrel();
            if (output != null) {
                Utils.sendMessage(player, "Sees Output barrel at " + Utils.posToString(output.getPos()));
            }
            BarrelBlockEntity stock = getStockBarrel();
            if (stock != null) {
                Utils.sendMessage(player, "Sees Stock barrel at " + Utils.posToString(stock.getPos()));
            }
            getShopMerchant().checkTrades(player);
        }
    }

    @Override
    public void onInventoryChange() {
        if (type.isExpansionType()) {
            //Notify neightbours
            BarrelBlockEntity entity = getEntity();
            World world = entity.getWorld();
            BlockPos pos = entity.getPos();
            for (Direction dir : Direction.values()) {
                BlockPos newPos = pos.offset(dir);
                if (!(world.getBlockState(newPos).getBlock() instanceof BarrelBlock))
                    continue;
                IAugmentedBarrelEntity barrelEntity = (IAugmentedBarrelEntity) world.getBlockEntity(newPos);
                if (barrelEntity.getType() == BarrelType.SHOP)
                    barrelEntity.onInventoryChange();
            }
        }
        if (type == BarrelType.SHOP && getShopMerchant().currentCustomer != null) {
            shopMerchant.refreshTrades();
        }
    }


    /**
     * "completeSetup" will prevent a shop from being created
     * unless all parts of the shop are there. Aka this will
     * require both a stock and an output to be attached before
     * the shop is seen as valid
     */
    public boolean completeSetup(BarrelType signType) {
        return (
            (signType == BarrelType.SHOP &&
                getNeighbourByType(BarrelType.OUTPUT) != null &&
                getNeighbourByType(BarrelType.STOCK) != null) ||
            (signType == BarrelType.OUTPUT &&
                getNeighbourByType(BarrelType.SHOP) != null &&
                getNeighbourByType(BarrelType.STOCK) != null) ||
            (signType == BarrelType.STOCK &&
                getNeighbourByType(BarrelType.SHOP) != null &&
                getNeighbourByType(BarrelType.OUTPUT) != null)
        );
    }

    @Inject(at = @At("RETURN"), method = "writeNbt")
    public void onNbtWrite(NbtCompound tag, CallbackInfo callback) {
        if (owner != null) {
            tag.putUuid(SHOP_OWNER_NBT_TAG, owner);
        }
        if (type != BarrelType.NONE) {
            tag.putInt(TYPE_NBT_TAG, type.toInt());
        }
        if (shopName != null)
            tag.putString(SHOP_NAME_NBT_TAG, shopName);
    }

    @Inject(at = @At("RETURN"), method = "readNbt")
    public void onNbtRead(NbtCompound tag, CallbackInfo callback) {
        if (tag.containsUuid(SHOP_OWNER_NBT_TAG)) {
            owner = tag.getUuid(SHOP_OWNER_NBT_TAG);
        }
        if (tag.contains(TYPE_NBT_TAG)) {
            type = BarrelType.fromInt(tag.getInt(TYPE_NBT_TAG));
        }
        if (tag.contains(SHOP_NAME_NBT_TAG))
            shopName = tag.getString(SHOP_NAME_NBT_TAG);
    }
}
