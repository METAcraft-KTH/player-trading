package se.leddy231.playertrading.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.w3c.dom.Text;
import se.leddy231.playertrading.interfaces.IAugmentedBarrelEntity;
import se.leddy231.playertrading.BarrelType;

@Mixin(SignBlockEntity.class)
public class SignEntityMixin {

    @Inject(at = @At("RETURN"), method = "executeClickCommandsIfPresent")
    public void createShop(
            Player player,
            Level level,
            BlockPos pos,
            boolean frontText,
            CallbackInfoReturnable<Boolean> cir
    ) {
        SignBlockEntity signEntity = (SignBlockEntity) (Object) this;
        Component text = signEntity.getFrontText().getMessage(0,false);
        // Has shop tag on sign
        BarrelType type = BarrelType.fromSignTag(text.getString());
        if (type == BarrelType.NONE)
            return;

        Level world = signEntity.getLevel();
        BlockState signState = signEntity.getBlockState();
        Block signBlock = signState.getBlock();
        // Is a wall sign (and not on a stick in the ground)
        if (!(signBlock instanceof WallSignBlock))
            return;
        Direction dir = signState.getValue(WallSignBlock.FACING).getOpposite();
        pos = pos.relative(dir);
        BlockState barrelState = world.getBlockState(pos);
        // Sign is attached to a barrel
        if (!(barrelState.getBlock() instanceof BarrelBlock))
            return;
        IAugmentedBarrelEntity barrelEntity = (IAugmentedBarrelEntity) world.getBlockEntity(pos);
        barrelEntity.activate(player, type);
    }
}
