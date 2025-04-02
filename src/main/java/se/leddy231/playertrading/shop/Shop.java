package se.leddy231.playertrading.shop;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.Nullable;
import se.leddy231.playertrading.DebugStick;
import se.leddy231.playertrading.PlayerTrading;
import se.leddy231.playertrading.ShopKey;
import se.leddy231.playertrading.Utils;
import se.leddy231.playertrading.interfaces.IBarrelEntity;
import se.leddy231.playertrading.interfaces.ISkullEntity;

import java.util.*;
import java.util.function.Predicate;

public class Shop {
    private static final String OWNER_TAG = "shop_owner";
    private static final String TYPE_TAG = "shop_type";

    public static final String CONFIG_TAG = "shop_config"; //Careful, this is used by the datafixer!

    private static final String CONDITIONS = "shop_conditions";
    private static final Codec<Map<SlotRange, ResourceKey<LootItemCondition>>> CONDITIONS_CODEC = Codec.unboundedMap(
            ShopSlotRanges.CODEC,
            ResourceKey.codec(Registries.PREDICATE)
    );

    public final ShopMerchant merchant;
    public final SkullBlockEntity entity;
    public final ShopConfigContainer configContainer;
    public ShopType shopType = ShopType.SHOP;
    public UUID owner;

    //I use LinkedHashMap in case you want to add functions to add remove conditions without reading/writing data later, and to preserve the order inserted.
    //Forcing resource keys makes sure you won't need to worry about the datafixer (you just need to fix all datapacks when updating).
    private final Map<SlotRange, ResourceKey<LootItemCondition>> conditions = new LinkedHashMap<>();

    private final Int2ObjectMap<Predicate<LootContext>> conditionsCache = new Int2ObjectOpenHashMap<>();
    private boolean conditionsCacheParsed = false;

    public Shop(UUID owner, SkullBlockEntity entity) {
        this.owner = owner;
        this.entity = entity;
        this.configContainer = new ShopConfigContainer(this);
        this.merchant = new ShopMerchant(this);
    }

    public static @Nullable Shop loadFromTag(CompoundTag tag, SkullBlockEntity entity, HolderLookup.Provider provider) {
        if (!tag.hasUUID(OWNER_TAG)) {
            return null;
        }

        var owner = tag.getUUID(OWNER_TAG);
        var shop = new Shop(owner, entity);

        if (tag.contains(TYPE_TAG)) {
            shop.shopType = ShopType.fromInt(tag.getInt(TYPE_TAG));
        }

        if (tag.contains(CONFIG_TAG)) {
            var subTag = tag.getCompound(CONFIG_TAG);
            ContainerHelper.loadAllItems(subTag, shop.configContainer.items, provider);
        }

        shop.conditions.clear(); //data remove will work since we clear here.
        if (tag.contains(CONDITIONS)) {
            //TODO Use the new 1.21.5 syntax for putting codecs neatly (tag.get(<name>, <codec>)).
            CONDITIONS_CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag.get(CONDITIONS)).resultOrPartial(
                    PlayerTrading.LOGGER::error
            ).ifPresent(
		            shop.conditions::putAll
            );
        }
        shop.updateConditionsCache();

        return shop;
    }

    public void saveAsTag(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putUUID(OWNER_TAG, owner);
        tag.putInt(TYPE_TAG, shopType.toInt());
        tag.put(CONFIG_TAG, ContainerHelper.saveAllItems(new CompoundTag(), configContainer.items, provider));

        var ops = provider.createSerializationContext(NbtOps.INSTANCE);
        //TODO Use the new 1.21.5 syntax for putting codecs neatly (tag.put(<name>, <codec>, <value>)).
        if (!conditions.isEmpty()) {
            CONDITIONS_CODEC.encodeStart(ops, conditions).resultOrPartial(
                    PlayerTrading.LOGGER::error
            ).ifPresent(
                    conditions -> tag.put(CONDITIONS, conditions)
            );
        }
    }

    public int maxNumberOfTrades() {
        return configContainer.getContainerSize() / 3;
    }

    public boolean hasAccessPermission(Player player) {
        if (player.getUUID().equals(owner)) {
            return true;
        }
        var usedItem = player.getMainHandItem();
        if (player.hasPermissions(4) && DebugStick.isDebugStick(usedItem)) {
            return true;
        }
        if (ShopKey.isKeyForUUID(usedItem, owner)) {
            return true;
        }
        return false;
    }

    public InteractionResult onSkullUse(Player player) {
        var isOwner = player.getUUID().equals(owner);
        var usedItem = player.getMainHandItem();

        if (isOwner && ShopKey.canMakeIntoKey(usedItem)) {
            ShopKey.makeIntoKeyForPlayer(usedItem, player);
            return InteractionResult.SUCCESS;
        }

        if (player.hasPermissions(4) && DebugStick.isMakeAdminStick(usedItem)) {
            shopType = ShopType.ADMIN;
            Utils.sendMessage(
                    player,
                    Component.translatableWithFallback(
                            "message.playertrading.convert_to_admin_shop", "Made into admin shop"
                    )
            );
            return InteractionResult.SUCCESS;
        }

        if (player.hasPermissions(4) && DebugStick.isMakeSingleUseStick(usedItem)) {
            shopType = ShopType.SINGLEUSE;
            merchant.hasTraded = false; //reset just in case
            Utils.sendMessage(
                    player,
                    Component.translatableWithFallback(
                            "message.playertrading.convert_to_single_use", "Made into single use shop"
                    )
            );
            return InteractionResult.SUCCESS;
        }

        if (configContainer.menuOpen || IBarrelEntity.isOpen(barrelEntity())) {
            Utils.sendMessage(
                    player,
                    Component.translatableWithFallback(
                            "message.playertrading.being_edited",
                            "Shop is currently being edited"
                    )
            );
            return InteractionResult.SUCCESS;
        }

        if (hasAccessPermission(player)) {
            merchant.forceCloseShop();
            if (!player.isShiftKeyDown()) {
                player.openMenu(configContainer);
                return InteractionResult.SUCCESS;
            }
            checkTrades(player);
        }
        merchant.openShop(player);
        return InteractionResult.SUCCESS;
    }

    public InteractionResult onBarrelUse(Player player) {
        if (configContainer.menuOpen || IBarrelEntity.isOpen(barrelEntity())) {
            Utils.sendMessage(
                    player,
                    Component.translatableWithFallback(
                            "message.playertrading.being_edited",
                            "Shop is currently being edited"
                    )
            );
            return InteractionResult.SUCCESS;
        }
        if (hasAccessPermission(player)) {
            merchant.forceCloseShop();
            return null;
        }
        merchant.openShop(player);
        return InteractionResult.SUCCESS;
    }

    @Nullable
    public HopperBlockEntity getOutputHopper() {
        var barrel = barrelEntity();
        var hopperPos = barrel.getBlockPos().relative(Direction.DOWN);
        var hopperEntity = barrel.getLevel().getBlockEntity(hopperPos);
        if (hopperEntity instanceof HopperBlockEntity e) {
            return e;
        }
        return null;
    }

    public List<HopperBlockEntity> getStockHoppers() {
        var barrel = barrelEntity();
        var output = new ArrayList<HopperBlockEntity>();
        for (var diretion : Direction.values()) {
            if (diretion == Direction.DOWN) {
                continue;
            }

            var hopperPos = barrel.getBlockPos().relative(diretion);
            var hopperState = barrel.getLevel().getBlockState(hopperPos);
            if (!(hopperState.getBlock() instanceof HopperBlock)) {
                continue;
            }

            var facing = hopperState.getValue(HopperBlock.FACING);
            if (!facing.getOpposite().equals(diretion)) {
                continue;
            }

            var hopperEntity = (HopperBlockEntity) barrel.getLevel().getBlockEntity(hopperPos);
            output.add(hopperEntity);
        }
        return output;
    }

    public ShopTradeOffer validateOffer(ShopTradeOffer offer) {
        if (shopType == ShopType.SINGLEUSE && merchant.hasTraded) {
            return offer.asInvalid(Component.translatableWithFallback(
                    "message.playertrading.invalid_reason.has_expired",
                    "This trade has expired"
            ));
        }

        if (shopType.isAdminType()) {
            return offer.asValid();
        }

        var barrel = barrelEntity();
        var outputHopper = getOutputHopper();
        var stockHoppers = getStockHoppers();
        if (barrel == null) {
            return offer.asInvalid(Component.translatableWithFallback(
                    "message.playertrading.invalid_reason.no_barrel",
                    "Shop not attached to a barrel"
            ));
        }

        var outputSlots = Utils.emptySlotsInContainer(barrel);
        if (outputHopper != null) {
            outputSlots += Utils.emptySlotsInContainer(outputHopper);
        }


        boolean firstFitsInOutputs = outputSlots >= 1;
        boolean secondFitsInOutputs = offer.getSecond().isEmpty() || outputSlots >= 2;
        boolean canPullFromStock = stockHoppers.stream().anyMatch(c -> Utils.canPullFromInventory(offer.getResult(), c))
                || Utils.canPullFromInventory(offer.getResult(), barrel);

        if (!firstFitsInOutputs) {
            return offer.asInvalid(Component.translatableWithFallback(
                    "message.playertrading.invalid_reason.insufficient_payment_space.1",
                    "the first payment item(s) does not fit in the output container(s)"
            ));
        }
        if (!secondFitsInOutputs) {
            return offer.asInvalid(Component.translatableWithFallback(
                    "message.playertrading.invalid_reason.insufficient_payment_space.2",
                    "the second payment item(s) does not fit in the output container(s)"
            ));
        }
        if (!canPullFromStock) {
            return offer.asInvalid(Component.translatableWithFallback(
                    "message.playertrading.invalid_reason.no_result",
                    "the result item(s) are not in the stock container(s)"
            ));
        }
        return offer.asValid();
    }

    public void performTrade(ShopTradeOffer offer) {

        if (shopType == ShopType.SINGLEUSE) {
            return;
        }

        var currentOffer = configContainer.getOfferFromIndex(offer.offerIndex);
        var shopBarrel = barrelEntity();
        var outputHopper = getOutputHopper();
        var stockHoppers = getStockHoppers();

        ItemStack first = offer.getFirst().copy();
        ItemStack second = offer.getSecond().copy();
        ItemStack result = offer.getResult().copy();
        boolean firstValid = ItemStack.isSameItem(currentOffer.getFirst(), first);
        boolean secondValid = ItemStack.isSameItem(currentOffer.getSecond(), second);
        boolean resultValid = ItemStack.isSameItem(currentOffer.getResult(), result);
        if (!firstValid) {
            PlayerTrading.LOGGER.error("First items of a trade mismatched!");
        }
        if (!secondValid) {
            PlayerTrading.LOGGER.error("Second items of a trade mismatched!");
        }
        if (!resultValid) {
            PlayerTrading.LOGGER.error("Result items of a trade mismatched!");
        }


        boolean firstSuccess =
                Utils.tryPutInInventory(first, outputHopper) || Utils.tryPutInInventory(first, shopBarrel);

        boolean secondSuccess =
                Utils.tryPutInInventory(second, outputHopper) || Utils.tryPutInInventory(second, shopBarrel);

        boolean resultSuccess = false;
        for (var stock : stockHoppers) {
            if (Utils.tryPullFromInventory(result, stock)) {
                resultSuccess = true;
                break;
            }
        }
        resultSuccess = resultSuccess || Utils.tryPullFromInventory(result, shopBarrel);
        if (shopType.isAdminType()) {
            return;
        }
        if (!(firstSuccess || secondSuccess || resultSuccess)) {
            PlayerTrading.LOGGER.info("Crafting failure " + firstSuccess + " " + secondSuccess + " " + resultSuccess);
        }
    }

    @Nullable
    private BarrelBlockEntity barrelEntity() {
        var pos = ISkullEntity.attachedToPosition(entity);
        var barrelEntity = entity.getLevel().getBlockEntity(pos);
        if (barrelEntity instanceof BarrelBlockEntity e) {
            return e;
        }
        return null;
    }

    public void checkTrades(Player player) {
        var outputHopper = getOutputHopper();
        if (outputHopper != null) {
            Utils.sendMessage(
                    player,
                    Component.translatableWithFallback(
                            "message.playertrading.visible_output_hopper",
                            "Shop sees prioritized output hopper at " + Utils.posToString(outputHopper.getBlockPos()),
                            Utils.posToString(outputHopper.getBlockPos())
                    )
            );
        }

        var stockHoppers = getStockHoppers();
        for (var hopper : stockHoppers) {
            Utils.sendMessage(
                    player,
                    Component.translatableWithFallback(
                            "message.playertrading.visible_stock_hopper",
                            "Shop sees prioritized stock hopper at " + Utils.posToString(hopper.getBlockPos()),
                            Utils.posToString(hopper.getBlockPos())
                    )
            );
        }

        MerchantOffers offers = merchant.getOffers();
        if (offers.isEmpty()) {
            Utils.sendMessage(
                    player,
                    Component.translatableWithFallback(
                            "message.playertrading.no_trades",
                            "No trades set up"
                    )
            );
            return;
        }
        boolean allValid = true;
        for (MerchantOffer offer : offers) {
            ShopTradeOffer shopOffer = (ShopTradeOffer) offer;
            if (shopOffer.valid) continue;

            allValid = false;
            int slot = shopOffer.offerIndex + 1;
            Utils.sendMessage(
                    player,
                    Component.translatableWithFallback(
                            "message.playertrading.specific_offer_invalid",
                            "Offer at slot " + slot + "-" + (slot + 2) + " is invalid because "
                                    + shopOffer.invalidReason,
                            slot, (slot + 2), shopOffer.invalidReason
                    )
            );
        }
        if (allValid) {
            Utils.sendMessage(
                    player,
                    Component.translatableWithFallback(
                            "message.playertrading.all_trades_valid",
                            "All trades are valid"
                    )
            );
        }
    }


    public void onContainerChanges() {
        merchant.refreshTrades();
    }

    public void onShopClose() {
        if (shopType == ShopType.SINGLEUSE && merchant.hasTraded) {
            selfDestruct();
        }
    }

    public void selfDestruct() {
        var barrel = barrelEntity();
        if (barrel != null) {
            barrel.clearContent();
            entity.getLevel().destroyBlock(barrel.getBlockPos(), false);
        }
        entity.getLevel().destroyBlock(entity.getBlockPos(), false);
    }

    private void updateConditionsCache() {
        if (entity.getLevel() != null && entity.getLevel().getServer() != null) {
            conditionsCache.clear();
            var predicates = entity.getLevel().getServer().reloadableRegistries().lookup().lookupOrThrow(Registries.PREDICATE);
            for (var c : conditions.entrySet()) {
                var condition = predicates.get(c.getValue()).map(Holder::value);
                condition.ifPresent(lootItemCondition -> c.getKey().slots().forEach(
                        slot -> conditionsCache.compute(slot, (s, existing) -> {
                            if (existing == null) {
                                return lootItemCondition;
                            } else {
                                return existing.and(lootItemCondition);
                            }
                        })
                ));
            }
            conditionsCacheParsed = true; //When loading world might not be assigned.
        }
    }

    public boolean hasCondition(int tradeSlot) {
        if (!conditionsCacheParsed) {
            updateConditionsCache(); //When loading world might not be assigned.
        }
        return conditionsCache.containsKey(tradeSlot);
    }

    public Predicate<LootContext> getCondition(int tradeSlot) {
        if (!conditionsCacheParsed) {
            updateConditionsCache(); //When loading world might not be assigned.
        }
        return conditionsCache.get(tradeSlot);
    }
}
