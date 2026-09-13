package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

import java.util.function.Function;

public class Small3DBallRenderer extends ThrownProjectile3DRenderer<Entity, ThrownProjectile3DRenderState> {
    private final ModelPart model;
    private final Function<Identifier, RenderType> renderType;

    public Small3DBallRenderer(EntityRendererProvider.Context context, float scale, Identifier texture, boolean translucent) {
        super(context, scale, texture);
        this.model = context.bakeLayer(AmendmentsClient.SMALL_THROWN_BALL);
        this.renderType = translucent ? RenderTypes::entityTranslucent : RenderTypes::entityCutout;
    }

    @Override
    public ThrownProjectile3DRenderState createRenderState() {
        return new ThrownProjectile3DRenderState();
    }

    @Override
    protected void submitBall(ThrownProjectile3DRenderState state, PoseStack poseStack,
                              SubmitNodeCollector collector, CameraRenderState camera) {
        collector.submitModelPart(this.model, poseStack, this.renderType.apply(this.getTexture()),
                state.lightCoords, OverlayTexture.NO_OVERLAY, null);
    }
}
