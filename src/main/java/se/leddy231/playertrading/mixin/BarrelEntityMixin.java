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
    public UUID owner;
    public ShopMerchant shopMerchant;
    public BarrelType type = BarrelType.NONE;

    @Override
    public BarrelType getType() {
        return type;
    };

    @Override
    public UUID getOwner() {
        return owner;
    }

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

    @Override
    public void activate(PlayerEntity player, BarrelType signType) {
        if (owner != null) {
            if (owner.equals(player.getUuid())) {
                playerTroubleshoot(player);
            } else {
                Utils.sendMessage(player, "Someone already owns this");
            }
        } else {
            create(player, signType);
        }
    }

    public void create(PlayerEntity player, BarrelType newType) {
        owner = player.getUuid();
        type = newType;

        BarrelBlockEntity entity = (BarrelBlockEntity) (Object) this;
        entity.markDirty();

        Utils.sendMessage(player, newType.typeName() + " barrel created");
    }

    public void playerTroubleshoot(PlayerEntity player) {
        Utils.sendMessage(player, type.typeName() + " barrel owned by you");
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
