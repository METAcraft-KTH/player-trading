package se.leddy231.playertrading.records;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;

public record ConnectedContainer(BlockPos position, Container container, String name) {
}
