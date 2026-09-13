package net.mehvahdjukaar.amendments.client.renderers;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.resources.model.sprite.SpriteId;
import org.jetbrains.annotations.Nullable;

public class JukeboxRenderState extends BlockEntityRenderState {
    public @Nullable SpriteId record;
    public float rotation;
    public int upLightCoords;
}
