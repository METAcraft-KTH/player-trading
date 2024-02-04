package se.leddy231.playertrading.shop;

import net.minecraft.network.chat.Component;

public enum ShopType {
    SHOP(
            0,
            Component.translatableWithFallback("shoptype.playertrading.shop", "Shop")
    ),
    ADMIN(
            1,
            Component.translatableWithFallback("shoptype.playertrading.admin_shop", "Admin Shop")
    ),
    SINGLEUSE(
            2,
            Component.translatableWithFallback("shoptype.playertrading.single_use", "Single Use")
    );

    private final int id;
    private final Component typeName;

    ShopType(int id, Component typeName) {
        this.id = id;
        this.typeName = typeName;
    }

    public static ShopType fromInt(int id) {
        for (ShopType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }

    public int toInt() {
        return id;
    }

    public Component typeName() {
        return typeName;
    }

    public boolean isAdminType() {
        return this == ADMIN || this == SINGLEUSE;
    }
}
