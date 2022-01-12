package se.leddy231.playertrading;

public enum BarrelType {
    NONE(0, "None", null),
    SHOP(1, "Shop", "{shop}"),
    OUTPUT(2, "Output", "{output}"),
    STOCK(3, "Stock", "{stock}"),
    PERMANENT(4, "Permanent shop", "{permanent shop}"),
    ADMIN(5, "Admin shop", "{admin shop}"),
    STORAGE(6, "Storage", "{storage}"),
    SINGLEUSE(7, "Single use", "{single use}");

    private int id;
    private String typeName;
    private String signTag;

    private BarrelType(int id, String typeName, String signTag) {
        this.id = id;
        this.typeName = typeName;
        this.signTag = signTag;
    }

    public int toInt() {
        return id;
    }

    public String typeName() {
        return typeName;
    }

    public boolean isShopType() {
        return this == SHOP || this == PERMANENT || this == ADMIN || this == SINGLEUSE;
    }

    public boolean isExpansionType() {
        return this == OUTPUT || this == STOCK || this == STORAGE;
    }

    public boolean isAdminType() {
        return this == ADMIN  || this == SINGLEUSE;
    }

    public static BarrelType fromInt(int id) {
        for (BarrelType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }

    public static BarrelType fromSignTag(String tag) {
        tag = tag.strip().toLowerCase();
        for (BarrelType type : values()) {
            if (type.signTag != null && type.signTag.equals(tag)) {
                return type;
            }
        }
        return NONE;
    }
}
