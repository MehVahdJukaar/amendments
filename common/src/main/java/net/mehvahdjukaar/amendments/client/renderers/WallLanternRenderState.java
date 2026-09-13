package net.mehvahdjukaar.amendments.client.renderers;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class WallLanternRenderState extends BlockEntityRenderState {
    public @Nullable BlockStateModel model;
    public BlockState lanternState;
    public Direction facing = Direction.NORTH;
    public float angle;
    public double attachmentOffset;
}
