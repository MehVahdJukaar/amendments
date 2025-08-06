package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LightLayer;

import java.util.function.Function;

public class Fireball3DRenderer<E extends Entity> extends ThrownProjectile3DRenderer<E> {

    private final ModelPart cube;
    private final ModelPart cubeEmissive;
    private final ModelPart overlay;
    private final ResourceLocation overlayTexture;
    private final Function<ResourceLocation, RenderType> renderTypeFunction;

    public Fireball3DRenderer(EntityRendererProvider.Context context, float scale,
                              ResourceLocation texture,
                              ResourceLocation overlayTexture,
                              ModelLayerLocation modelLocation,
                              boolean hasNoShade) {
        super(context, scale, texture);
        this.overlayTexture = overlayTexture;
        this.renderTypeFunction = hasNoShade ? RenderType::unlit : RenderType::entityCutout;
        ModelPart model = context.bakeLayer(modelLocation);
        this.cube = model.getChild("cube");
        this.cubeEmissive = model.getChild("cube_emissive");
        this.overlay = model.getChild("overlay");
    }

    @Override
    protected int getBlockLightLevel(E entity, BlockPos pos) {
        // otherwise its always 15 since its on fire
        return entity.level().getBrightness(LightLayer.BLOCK, pos);
    }

    @Override
    public void renderBall(E entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        ResourceLocation mainTexture = getTextureLocation(entity);
        RenderType mainRedderType = renderTypeFunction.apply(mainTexture);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(mainRedderType);
        cube.render(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
        cubeEmissive.render(poseStack, vertexConsumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

        float f = (float) entity.tickCount + partialTick;
        RenderType fireRenderType = RenderType.energySwirl(overlayTexture,
                this.xOffset(f) % 1.0F, f * 0.01F % 1.0F);
        VertexConsumer outlineVertexConsumer = bufferSource.getBuffer(fireRenderType);

        overlay.render(poseStack, outlineVertexConsumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
    }

    private float xOffset(float tickCount) {
        return tickCount * 0.01F;
    }



}
