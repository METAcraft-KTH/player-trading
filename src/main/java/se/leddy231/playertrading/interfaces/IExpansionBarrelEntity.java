package se.leddy231.playertrading.interfaces;

import java.util.UUID;

import net.minecraft.entity.player.PlayerEntity;

public interface IExpansionBarrelEntity {
    public enum BarrelType {
        NONE(0),
        SHOP(1),
        OUTPUT(2),
        STOCK(3);

        private int id;

        private BarrelType(int id) {
            this.id = id;
        }

        public int toInt() {
            return id;
        }

        public boolean isExpansionType() {
            return this != NONE && this != SHOP;
        }

        public String typeName() {
            switch(this) {
                case NONE:
                    return "None";
                case SHOP:
                    return "Shop";
                case OUTPUT:
                    return "Ouput";
                case STOCK:
                    return "Stock";
            }
            return "None";
        }

        public static BarrelType fromInt(int id) {
            for (BarrelType type : values()) {
                if (type.toInt() == id) {
                    return type;
                }
            }
            return null;
        }

        public static BarrelType fromSignTag(String tag) {
            switch(tag.toLowerCase().strip()) {
                case "{shop}":
                    return SHOP;
                case "{output}":
                    return OUTPUT;
                case "{stock}":
                    return STOCK;
            }
            return NONE;
        }
    }

    public abstract BarrelType getType();

    public abstract UUID getOwner();

    public abstract IShopBarrelEntity getShop();

    public abstract void tryCreate(PlayerEntity player, BarrelType type);
}
