package se.leddy231.playertrading.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BarrelBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import se.leddy231.playertrading.interfaces.IAugmentedBarrelEntity;
import se.leddy231.playertrading.BarrelType;

@Mixin(SignBlockEntity.class)
public class SignEntityMixin {

    @Inject(at = @At("RETURN"), method = "onActivate")
    public void createShop(ServerPlayerEntity player, final CallbackInfoReturnable<Boolean> callback) {
        SignBlockEntity signEntity = (SignBlockEntity) (Object) this;
        Text text = signEntity.getTextOnRow(0, false);
        // Has shop tag on sign
        BarrelType type = BarrelType.fromSignTag(text.asString());
        if (type == BarrelType.NONE)
            return;

        World world = signEntity.getWorld();
        BlockState signState = world.getBlockState(signEntity.getPos());
        Block signBlock = signState.getBlock();
        // Is a wall sign (and not on a stick in the ground)
        if (!(signBlock instanceof WallSignBlock))
            return;

        BlockPos pos = signEntity.getPos();
        Direction dir = signState.get(WallSignBlock.FACING).getOpposite();
        pos = pos.offset(dir);
        BlockState barrelState = signEntity.getWorld().getBlockState(pos);
        // Sign is attached to a barrel
        if (!(barrelState.getBlock() instanceof BarrelBlock))
            return;
        IAugmentedBarrelEntity barrelEntity = (IAugmentedBarrelEntity) world.getBlockEntity(pos);
        barrelEntity.activate(player, type);
    }
}
