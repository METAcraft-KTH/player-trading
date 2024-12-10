package se.leddy231.playertrading;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;

import java.util.List;
import java.util.UUID;

public class ShopKey {

    public static final String KEY_TAG = "shop_key";

    public static boolean canMakeIntoKey(ItemStack stack) {
        return stack.is(Items.GOLD_INGOT) && stack.getCount() == 1 && !stack.has(DataComponents.CUSTOM_DATA);
    }

    public static void makeIntoKeyForPlayer(ItemStack stack, Player player) {
        String key_uuid = player.getUUID().toString();

        Component name = player.getName();
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(
                List.of(231.0f), List.of(), List.of(), List.of())
        );
        stack.set(
                DataComponents.ITEM_NAME,
                Component.translatableWithFallback(
                "item.playertrading.shop_key",
                name.getString() + "'s Shop Key",
                name
        ).withStyle(style -> style.withItalic(false)));
        stack.set(
                DataComponents.CUSTOM_DATA,
                stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).update(nbt -> {
                    nbt.putString(KEY_TAG, key_uuid);
                })
        );
    }

    public static boolean isKey(ItemStack stack) {
        var nbt = stack.get(DataComponents.CUSTOM_DATA);
        if (nbt == null) {
            return false;
        }
        return stack.is(Items.GOLD_INGOT) && nbt.contains(KEY_TAG);
    }

    public static boolean isKeyForUUID(ItemStack stack, UUID uuid) {
        var nbt = stack.get(DataComponents.CUSTOM_DATA);
        if (nbt == null) {
            return false;
        }
        String key_uuid = nbt.read(Codec.STRING.fieldOf(KEY_TAG)).resultOrPartial(
                PlayerTrading.LOGGER::error
        ).orElse("");
        return stack.is(Items.GOLD_INGOT) && key_uuid.equals(uuid.toString());
    }
}
