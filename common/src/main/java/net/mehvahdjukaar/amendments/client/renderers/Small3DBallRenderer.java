package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.function.Function;

public class Small3DBallRenderer extends ThrownProjectile3DRenderer<Entity> {
    private final ModelPart model;
    private final Function<ResourceLocation, RenderType> renderType;

    public Small3DBallRenderer(EntityRendererProvider.Context context, float scale, ResourceLocation texture, boolean translucent) {
        super(context, scale, texture);
        this.model = context.bakeLayer(AmendmentsClient.SMALL_THROWN_BALL);
        this.renderType = translucent ? RenderType::entityTranslucent : RenderType::entityCutoutNoCull;
    }

    @Override
    public void renderBall(Entity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        this.model.render(poseStack,
                bufferSource.getBuffer(renderType.apply(this.getTextureLocation(entity))),
                packedLight, OverlayTexture.NO_OVERLAY);
    }
}
