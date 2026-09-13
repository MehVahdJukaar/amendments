package net.mehvahdjukaar.amendments.client.renderers;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class SkullWithWaxRenderState extends BlockEntityRenderState {
    public @Nullable BlockEntityRenderState skull;
    public float yaw;
    public boolean wall;
    public @Nullable Identifier waxTexture;
}
