package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
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

public class FireballRenderer<E extends Entity> extends ThrownProjectile3DRenderer<E> {

    private final ModelPart meteorRock;
    private final ModelPart meteorEmissive;
    private final ModelPart fireLayer;
    private final ResourceLocation fireTexture;
    private final Function<ResourceLocation, RenderType> renderTypeFunction;

    public FireballRenderer(EntityRendererProvider.Context context, float scale,
                            ResourceLocation texture,
                            ResourceLocation overlayTexture,
                            ModelLayerLocation modelLocation,
                            boolean hasNoShade) {
        super(context, scale, texture);
        this.fireTexture = overlayTexture;
        this.renderTypeFunction = hasNoShade ? RenderType::text : RenderType::entityCutout;
        ModelPart model = context.bakeLayer(modelLocation);
        this.meteorRock = model.getChild("cube");
        this.meteorEmissive = model.getChild("cube_emissive");
        this.fireLayer = model.getChild("overlay");
    }

    @Override
    protected int getBlockLightLevel(E entity, BlockPos pos) {
        // otherwise its always 15 since its on fire
        return entity.level().getBrightness(LightLayer.BLOCK, pos);
    }

    public void renderBall(E entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        ResourceLocation meteorTexture = getTextureLocation(entity);
        RenderType meteroRenderType = renderTypeFunction.apply(meteorTexture);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(meteroRenderType);
        meteorRock.render(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
        meteorEmissive.render(poseStack, vertexConsumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

        float f = (float) entity.tickCount + partialTick;
        RenderType fireRenderType = RenderType.energySwirl(fireTexture,
                this.xOffset(f) % 1.0F, f * 0.01F % 1.0F);
        VertexConsumer outlineVertexConsumer = bufferSource.getBuffer(fireRenderType);

        fireLayer.render(poseStack, outlineVertexConsumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
    }

    private float xOffset(float tickCount) {
        return tickCount * 0.01F;
    }

    public static LayerDefinition createMesh(int size) {
        int r = size / 2;
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("cube", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-r, -r, -r, size, size, size),
                PartPose.offset(0, 0, 0));

        root.addOrReplaceChild("cube_emissive", CubeListBuilder.create()
                        .texOffs(32, 0)
                        .addBox(-r, -r, -r, size, size, size),
                PartPose.offset(0, 0, 0));

        root.addOrReplaceChild("overlay", CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(-(r + 1), -(r + 1), -(r + 1), (size + 2), (size + 2), (size + 2)),
                PartPose.offset(0, 0, 0));

        return LayerDefinition.create(mesh, 64, 64);
    }


}
