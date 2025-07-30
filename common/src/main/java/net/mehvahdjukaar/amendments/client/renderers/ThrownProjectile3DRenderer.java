package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.amendments.common.entity.IVisualRotationProvider;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.joml.Quaternionf;

public abstract class ThrownProjectile3DRenderer<E extends Entity> extends EntityRenderer<E> {

    private final ResourceLocation texture;
    private final float scale;


    public ThrownProjectile3DRenderer(EntityRendererProvider.Context context, float scale, ResourceLocation texture) {
        super(context);
        this.scale = scale;
        this.texture = texture;
    }

    @Override
    public void render(E entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        if (entity.tickCount >= 2 || !(this.entityRenderDispatcher.camera.getEntity().distanceToSqr(entity) < 12.25)) {
            super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
            poseStack.pushPose();
            poseStack.translate(0, entity.getBbHeight() / 2f, 0);
            poseStack.scale(scale, scale, scale);

            if (entity instanceof IVisualRotationProvider vp) {
                Quaternionf rotation = vp.amendments$getVisualRotation(partialTick);
                poseStack.mulPose(rotation);
            }

            renderBall(entity, partialTick, poseStack, bufferSource, packedLight);
            poseStack.popPose();
        }
    }

    public abstract void renderBall(E entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight);

    @Override
    public ResourceLocation getTextureLocation(E entity) {
        return texture;
    }


}
