package se.leddy231.playertrading.mixin;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;

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
    public IAugmentedBarrelEntity getOutputBarrel() {
        IAugmentedBarrelEntity entity = getNeighbourByType(BarrelType.OUTPUT);
        if (entity == null) {
            entity = getNeighbourByType(BarrelType.STORAGE);
        }
        if (entity != null) {
            return entity;
        }
        return null;
    }

    @Override
    public IAugmentedBarrelEntity getStockBarrel() {
        IAugmentedBarrelEntity entity = getNeighbourByType(BarrelType.STOCK);
        if (entity == null) {
            entity = getNeighbourByType(BarrelType.STORAGE);
        }
        if (entity != null) {
            return entity;
        }
        return null;
    }

    @Override
    public IShopBarrelEntity getShopBarrel() {
        if (type.isShopType()) {
            return this;
        }
        return null;
    }

    @Override
    public IShopBarrelEntity findConnectedShop() {
        if (type.isShopType()) {
            return this;
        }
        IAugmentedBarrelEntity entity = getNeighbourByType(BarrelType.SHOP);
        if (entity == null) {
            entity = getNeighbourByType(BarrelType.PERMANENT);
        }
        if (entity == null) {
            return null;
        }
        return (IShopBarrelEntity) entity;
    }

    @Override
    public BarrelBlockEntity getEntity() {
        return (BarrelBlockEntity) (Object) this;
    }

    @Override
    public ShopMerchant getShopMerchant() {
        if (!type.isShopType()) {
            return null;
        }
        if (shopMerchant == null) {
            shopMerchant = new ShopMerchant(this);
        }
        return shopMerchant;
    }

    public IAugmentedBarrelEntity getNeighbourByType(BarrelType type) {
        BarrelBlockEntity entity = getEntity();
        Level world = entity.getLevel();
        BlockPos pos = entity.getBlockPos();
        for (Direction dir : Direction.values()) {
            BlockPos newPos = pos.relative(dir);
            if (!(world.getBlockState(newPos).getBlock() instanceof BarrelBlock))
                continue;
            IAugmentedBarrelEntity barrelEntity = (IAugmentedBarrelEntity) world.getBlockEntity(newPos);
            if (barrelEntity.getType() == type && barrelEntity.getOwner().equals(owner))
                return barrelEntity;
        }
        return null;
    }

    @Override
    public boolean isBarrelOpen() {
        BarrelBlockEntity entity = getEntity();
        Level world = entity.getLevel();
        BlockPos pos = entity.getBlockPos();
        BlockState state = world.getBlockState(pos);
        return state.getValue(BarrelBlock.OPEN);
    }

    @Override
    public boolean isAnyBarrelOpen() {
        if (isBarrelOpen())
            return true;
        IAugmentedBarrelEntity output = getNeighbourByType(BarrelType.OUTPUT);
        if (output != null && output.isBarrelOpen())
            return true;
        IAugmentedBarrelEntity stock = getNeighbourByType(BarrelType.STOCK);
        if (stock != null && stock.isBarrelOpen())
            return true;
        return false;
    }

    @Override
    public void activate(Player player, BarrelType signType) {
        if (owner != null) {
            boolean opDebugBypass = player.hasPermissions(4) && ItemStack.isSameItemSameTags(player.getMainHandItem(), DebugStickCommand.STICK);
            if (owner.equals(player.getUUID())|| opDebugBypass) {
                playerTroubleshoot(player);
            } else {
                Utils.sendMessage(player, "Someone already owns this");
            }
        } else {
            if(signType.isAdminType() && !player.hasPermissions(4)) {
                Utils.sendMessage(player, "You do not have permission for this");
                return;
            }
            create(player, signType);
        }
    }

    public void create(Player player, BarrelType newType) {
        owner = player.getUUID();
        type = newType;

        BarrelBlockEntity entity = (BarrelBlockEntity) (Object) this;
        entity.setChanged();

        Utils.sendMessage(player, newType.typeName() + " barrel created");
    }

    public void playerTroubleshoot(Player player) {
        if (owner.equals(player.getUUID())) {
            Utils.sendMessage(player, type.typeName() + " barrel owned by you");
        } else {
            Player ownerEntity = player.getServer().getPlayerList().getPlayer(owner);
            if (ownerEntity != null) {

                Utils.sendMessage(player, Component.literal(type.typeName() + " barrel owned by ").append(ownerEntity.getDisplayName()));
            } else {
                Utils.sendMessage(player, type.typeName() + " barrel owned by offline player " + owner);
            }
        }

        if (type.isShopType()) {
            IAugmentedBarrelEntity output = getOutputBarrel();
            IAugmentedBarrelEntity stock = getStockBarrel();

            if (output == null) {
                if(type == BarrelType.PERMANENT) {
                    Utils.sendMessage(player, "No Output barrel seen, a Permanent shop does not function without one.");
                }
            } else {
                Utils.sendMessage(player, "Sees " + output.getType().typeName() + " barrel as output at " + Utils.posToString(output.getEntity().getBlockPos()));
            }

            if (stock == null) {
                if(type == BarrelType.PERMANENT) {
                    Utils.sendMessage(player, "No Stock barrel seen, a Permanent shop does not function without one.");
                }
            } else {
                Utils.sendMessage(player, "Sees " + stock.getType().typeName() + " barrel as stock at " + Utils.posToString(stock.getEntity().getBlockPos()));
                if (output == null) {
                    Utils.sendMessage(player, "No Output barrel seen, Stock barrel does not function without one.");
                }
            }
            getShopMerchant().checkTrades(player);
        }
        if (type.isExpansionType()) {
            IShopBarrelEntity shop = findConnectedShop();
            if (shop == null) {
                Utils.sendMessage(player, "No shop connected");
            }
        }
    }

    @Override
    public void onInventoryChange() {
        if (type.isExpansionType()) {
            // Notify neightbours
            BarrelBlockEntity entity = getEntity();
            Level world = entity.getLevel();
            BlockPos pos = entity.getBlockPos();
            for (Direction dir : Direction.values()) {
                BlockPos newPos = pos.relative(dir);
                if (!(world.getBlockState(newPos).getBlock() instanceof BarrelBlock))
                    continue;
                IAugmentedBarrelEntity barrelEntity = (IAugmentedBarrelEntity) world.getBlockEntity(newPos);
                if (barrelEntity.getType().isShopType())
                    barrelEntity.onInventoryChange();
            }
        }
        if (type.isShopType() && getShopMerchant().currentCustomer != null) {
            shopMerchant.refreshTrades();
        }
    }

    @Inject(at = @At("HEAD"), method = "getDefaultName", cancellable = true)
    public void getContainerName(CallbackInfoReturnable<Component> callback) {
        if (type != BarrelType.NONE) {
            callback.setReturnValue(Component.literal(type.typeName() + " Barrel"));
        }
    }

    @Inject(at = @At("RETURN"), method = "saveAdditional")
    public void onNbtWrite(CompoundTag tag, CallbackInfo callback) {
        if (owner != null) {
            tag.putUUID(SHOP_OWNER_NBT_TAG, owner);
        }
        if (type != BarrelType.NONE) {
            tag.putInt(TYPE_NBT_TAG, type.toInt());
        }
    }

    @Inject(at = @At("RETURN"), method = "load")
    public void onNbtRead(CompoundTag tag, CallbackInfo callback) {
        if (tag.hasUUID(SHOP_OWNER_NBT_TAG)) {
            owner = tag.getUUID(SHOP_OWNER_NBT_TAG);
        }
        if (tag.contains(TYPE_NBT_TAG)) {
            type = BarrelType.fromInt(tag.getInt(TYPE_NBT_TAG));
        }

    }
}
