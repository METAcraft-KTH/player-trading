package se.leddy231.playertrading.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.leddy231.playertrading.BarrelType;
import se.leddy231.playertrading.DebugStick;
import se.leddy231.playertrading.ShopMerchant;
import se.leddy231.playertrading.Utils;
import se.leddy231.playertrading.interfaces.IAugmentedBarrelEntity;
import se.leddy231.playertrading.interfaces.IShopBarrelEntity;
import se.leddy231.playertrading.records.ConnectedContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mixin(BarrelBlockEntity.class)
public class BarrelEntityMixin implements IShopBarrelEntity {
    private static final String SHOP_OWNER_NBT_TAG = "shop_owner";
    private static final String TYPE_NBT_TAG = "shop_barrel_type";
    public UUID owner;
    public ShopMerchant shopMerchant;
    public BarrelType type = BarrelType.NONE;

    @Override
    public BarrelType getType() {
        return type;
    }

    @Override
    public UUID getOwner() {
        return owner;
    }

    public List<ConnectedContainer> getOutputContainers() {
        var list = new ArrayList<ConnectedContainer>();
        var entity = getEntity();
        var world = entity.getLevel();
        var shopPosition = entity.getBlockPos();

        // First priority, look for hopper under the barrel
        var hopperPos = shopPosition.relative(Direction.DOWN);
        var hopperState = world.getBlockState(hopperPos);
        if (hopperState.getBlock() instanceof HopperBlock) {
            var hopperEntity = (HopperBlockEntity) world.getBlockEntity(hopperPos);
            list.add(new ConnectedContainer(hopperPos, hopperEntity, "Prioritized Hopper"));
        }

        // Second priority, find non-shop barrels
        for (var direction : Direction.values()) {
            var barrelPos = shopPosition.relative(direction);
            var state = world.getBlockState(barrelPos);
            if (!(state.getBlock() instanceof BarrelBlock)) {
                continue;
            }

            var barrelEntity = (IAugmentedBarrelEntity) world.getBlockEntity(barrelPos);
            if (barrelEntity.getType() != BarrelType.NONE) {
                continue;
            }

            list.add(new ConnectedContainer(barrelPos, barrelEntity.getEntity(), "Barrel"));
        }
        return list;
    }

    public List<ConnectedContainer> getStockContainers() {
        var list = new ArrayList<ConnectedContainer>();
        var entity = getEntity();
        var world = entity.getLevel();
        var shopPosition = entity.getBlockPos();

        // First priority, find hoppers facing the barrel
        for (var direction : Direction.values()) {
            var hopperPos = shopPosition.relative(direction);
            var state = world.getBlockState(hopperPos);
            if (!(state.getBlock() instanceof HopperBlock)) {
                continue;
            }

            var facing = state.getValue(HopperBlock.FACING);
            if (!hopperPos.relative(facing).equals(shopPosition)) {
                continue;
            }

            var hopperEntity = (HopperBlockEntity) world.getBlockEntity(hopperPos);
            list.add(new ConnectedContainer(hopperPos, hopperEntity, "Prioritized Hopper"));
        }

        // Second priority, find non-shop barrels
        for (var direction : Direction.values()) {
            var barrelPos = shopPosition.relative(direction);
            var state = world.getBlockState(barrelPos);
            if (!(state.getBlock() instanceof BarrelBlock)) {
                continue;
            }

            var barrelEntity = (IAugmentedBarrelEntity) world.getBlockEntity(barrelPos);
            if (barrelEntity.getType() != BarrelType.NONE) {
                continue;
            }

            list.add(new ConnectedContainer(barrelPos, barrelEntity.getEntity(), "Barrel"));
        }
        return list;
    }

    @Override
    public IAugmentedBarrelEntity getOutputBarrel() {
        IAugmentedBarrelEntity entity = getNeighbourByType(BarrelType.OUTPUT);
        if (entity == null) {
            entity = getNeighbourByType(BarrelType.STORAGE);
        }
        return entity;
    }

    @Override
    public IAugmentedBarrelEntity getStockBarrel() {
        IAugmentedBarrelEntity entity = getNeighbourByType(BarrelType.STOCK);
        if (entity == null) {
            entity = getNeighbourByType(BarrelType.STORAGE);
        }
        return entity;
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

    public BlockState[] getNeighbours() {
        var directions = Direction.values();
        var blockStates = new BlockState[directions.length];
        var entity = getEntity();
        var world = entity.getLevel();
        var currentPos = entity.getBlockPos();
        for (var i = 0; i < directions.length; i++) {
            var direction = directions[i];
            var newPos = currentPos.relative(direction);
            var state = world.getBlockState(newPos);
            blockStates[i] = state;
        }
        return blockStates;
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
            if (!(world.getBlockState(newPos).getBlock() instanceof BarrelBlock)) continue;
            IAugmentedBarrelEntity barrelEntity = (IAugmentedBarrelEntity) world.getBlockEntity(newPos);
            if (barrelEntity.getType() == type && barrelEntity.getOwner().equals(owner)) return barrelEntity;
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
        if (isBarrelOpen()) return true;
        IAugmentedBarrelEntity output = getNeighbourByType(BarrelType.OUTPUT);
        if (output != null && output.isBarrelOpen()) return true;
        IAugmentedBarrelEntity stock = getNeighbourByType(BarrelType.STOCK);
        return stock != null && stock.isBarrelOpen();
    }

    @Override
    public void activate(Player player, BarrelType signType) {
        if (owner != null) {
            boolean opDebugBypass = player.hasPermissions(4) && ItemStack.isSameItemSameTags(player.getMainHandItem(),
                                                                                             DebugStick.STICK
            );
            if (owner.equals(player.getUUID()) || opDebugBypass) {
                playerTroubleshoot(player);
            } else {
                Utils.sendMessage(player, "Someone already owns this");
            }
        } else {
            if (signType.isAdminType() && !player.hasPermissions(4)) {
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

                Utils.sendMessage(player,
                                  Component.literal(
                                          type.typeName() + " barrel owned by ").append(ownerEntity.getDisplayName())
                );
            } else {
                Utils.sendMessage(player, type.typeName() + " barrel owned by offline player " + owner);
            }
        }

        if (type.isShopType()) {
            var outputs = getOutputContainers();
            var stocks = getStockContainers();

            Utils.sendMessage(player, "Output:");
            if (outputs.isEmpty()) {
                Utils.sendMessage(player, "    None");
            }
            for (var output : outputs) {
                Utils.sendMessage(player, "  " + output.name() + " at " + Utils.posToString(output.position()));
            }

            Utils.sendMessage(player, "Stock:");
            if (stocks.isEmpty()) {
                Utils.sendMessage(player, "    None");
            }
            for (var stock : stocks) {
                Utils.sendMessage(player, "  " + stock.name() + " at " + Utils.posToString(stock.position()));
            }
            getShopMerchant().checkTrades(player);
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
                if (!(world.getBlockState(newPos).getBlock() instanceof BarrelBlock)) continue;
                IAugmentedBarrelEntity barrelEntity = (IAugmentedBarrelEntity) world.getBlockEntity(newPos);
                if (barrelEntity.getType().isShopType()) barrelEntity.onInventoryChange();
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
