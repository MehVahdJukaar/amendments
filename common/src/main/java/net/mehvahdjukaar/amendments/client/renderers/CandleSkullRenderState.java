package net.mehvahdjukaar.amendments.client.renderers;

import net.minecraft.client.renderer.block.BlockModelRenderState;

public class CandleSkullRenderState extends SkullWithWaxRenderState {
    public final BlockModelRenderState candle = new BlockModelRenderState();
    public float candleYaw;
    public boolean hasCandle;
}
