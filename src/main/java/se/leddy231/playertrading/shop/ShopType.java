package se.leddy231.playertrading.shop;

public enum ShopType {
    SHOP(0, "Shop"), ADMIN(1, "Admin shop"), SINGLEUSE(2, "Single use");

    private final int id;
    private final String typeName;

    ShopType(int id, String typeName) {
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

    public String typeName() {
        return typeName;
    }

    public boolean isAdminType() {
        return this == ADMIN || this == SINGLEUSE;
    }
}
