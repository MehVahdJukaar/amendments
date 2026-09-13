package net.mehvahdjukaar.amendments.client.renderers;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import org.jetbrains.annotations.Nullable;

public class DoubleSkullRenderState extends SkullWithWaxRenderState {
    public @Nullable BlockEntityRenderState upperSkull;
    public float upperYaw;
}
