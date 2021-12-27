package se.leddy231.playertrading;

import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.util.math.Vec3i;
import se.leddy231.playertrading.interfaces.IExpansionBarrelEntity;

public class ShopExpansion {
    public Vec3i offset;
    public IExpansionBarrelEntity entity;

    public ShopExpansion(Vec3i offset, IExpansionBarrelEntity entity) {
        this.offset = offset;
        this.entity = entity;
    }

    public BarrelBlockEntity getBarrel() {
        return (BarrelBlockEntity) entity;
    }
}
