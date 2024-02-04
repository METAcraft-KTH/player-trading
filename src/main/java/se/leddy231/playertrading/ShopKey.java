package se.leddy231.playertrading;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.UUID;

public class ShopKey {

    public static final String KEY_TAG = "shop_key";
    public static final String CUSTOM_MODEL_DATA_TAG = "CustomModelData";

    public static boolean canMakeIntoKey(ItemStack stack) {
        return stack.is(Items.GOLD_INGOT) && stack.getCount() == 1 && !stack.hasTag();
    }

    public static void makeIntoKeyForPlayer(ItemStack stack, Player player) {
        String key_uuid = player.getUUID().toString();
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(KEY_TAG, key_uuid);
        tag.putInt(CUSTOM_MODEL_DATA_TAG, 231);

        Component name = player.getName();
        stack.setHoverName(Component.translatableWithFallback(
                "item.playertrading.shop_key",
                name.getString() + "'s Shop Key",
                name
        ).withStyle(style -> style.withItalic(false)));
    }

    public static boolean isKey(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if (nbt == null) {
            return false;
        }
        return stack.is(Items.GOLD_INGOT) && nbt.contains(KEY_TAG);
    }

    public static boolean isKeyForUUID(ItemStack stack, UUID uuid) {
        CompoundTag nbt = stack.getTag();
        if (nbt == null) {
            return false;
        }
        String key_uuid = nbt.getString(KEY_TAG);
        return stack.is(Items.GOLD_INGOT) && key_uuid.equals(uuid.toString());
    }
}
