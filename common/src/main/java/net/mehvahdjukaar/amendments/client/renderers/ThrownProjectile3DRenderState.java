package net.mehvahdjukaar.amendments.client.renderers;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class ThrownProjectile3DRenderState extends EntityRenderState {
    public float yRot;
    public float xRot;
    public @Nullable Matrix4f visualTransformation;
}
