package se.leddy231.playertrading.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.PlayerWallHeadBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.leddy231.playertrading.BarrelType;
import se.leddy231.playertrading.interfaces.IAugmentedBarrelEntity;

@Mixin(PlayerWallHeadBlock.class)
public class SkullBlockEntityMixin {

    @Inject(at = @At("HEAD"),method = "setPlacedBy")
    private void onPlace(
            Level level,
            BlockPos pos,
            BlockState state,
            LivingEntity placer,
            ItemStack stack,
            CallbackInfo ci
    ) {
        //check so the placer is a player
        if (!(placer instanceof Player)) {
            return;
        }
        var direction = state.getValue(WallSkullBlock.FACING).getOpposite();
        var barrelPos = pos.relative(direction);
        //check so the block entity exists and is a skullblock entity
        if (!(level.getBlockEntity(pos) instanceof SkullBlockEntity skullBlockEntity)) {
            return;
        }
        var ownerProfile = skullBlockEntity.getOwnerProfile();
        //check so that playerhead is valid
        if (ownerProfile == null) {
            return;
        }

        if (!(level.getBlockState(barrelPos).getBlock() instanceof BarrelBlock)) {
            return;
        }

        //compare placer uuid with uuid from playerhead
        if (!ownerProfile.getId().equals(placer.getUUID())) {
            return;
        }
        IAugmentedBarrelEntity barrelEntity = (IAugmentedBarrelEntity) level.getBlockEntity(barrelPos);
        barrelEntity.activate((Player) placer, BarrelType.NONE);
    }

}
