package net.mehvahdjukaar.amendments.client.renderers;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;

public class ToolHookRenderState extends BlockEntityRenderState {
    public final ItemStackRenderState item = new ItemStackRenderState();
    public Direction facing = Direction.NORTH;
    public boolean digger;
}
