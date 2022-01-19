package se.leddy231.playertrading;

import java.util.UUID;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;

public class ShopKey {

    public static final String KEY_TAG = "shop_key";
    public static final String CUSTOM_MODEL_DATA_TAG = "CustomModelData";
    
    public static void makeIntoKeyForPlayer(ItemStack stack, PlayerEntity player) {
        String key_uuid = player.getUuid().toString();
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putString(KEY_TAG, key_uuid);
        nbt.putInt(CUSTOM_MODEL_DATA_TAG, 231);

        String name = player.getName().asString();
        stack.setCustomName(new LiteralText(name + "'s Shop key"));
    }

    public static boolean isKey(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) {
            return false;
        }
        return stack.isOf(Items.GOLD_INGOT) && nbt.contains(KEY_TAG);
    }

    public static boolean isKeyForUUID(ItemStack stack, UUID uuid) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) {
            return false;
        }
        String key_uuid  = nbt.getString(KEY_TAG);
        return stack.isOf(Items.GOLD_INGOT) && key_uuid.equals(uuid.toString());
    }
}
