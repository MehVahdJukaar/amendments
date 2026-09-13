package net.mehvahdjukaar.amendments.client.renderers;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

public class CeilingBannerRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public boolean attached;
    public float phase;
    public DyeColor baseColor = DyeColor.WHITE;
    public BannerPatternLayers patterns = BannerPatternLayers.EMPTY;
}
