package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.mehvahdjukaar.amendments.AmendmentsClient;
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
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LightLayer;

import java.util.function.Function;

public class FireballRenderer3D extends EntityRenderer<Entity> {

    private final ModelPart meteorRock;
    private final ModelPart meteorEmissive;
    private final ModelPart fireLayer;
    private final ResourceLocation texture;
    private final ResourceLocation fireTexture;
    private final float scale;
    private final Function<ResourceLocation, RenderType> renderTypeFunction;

    public FireballRenderer3D(EntityRendererProvider.Context context, float scale,
                              ResourceLocation texture,
                              ResourceLocation overlayTexture,
                              ModelLayerLocation modelLocation,
                              boolean hasNoShade) {
        super(context);
        this.scale = scale;
        this.texture = texture;
        this.fireTexture = overlayTexture;
        this.renderTypeFunction = hasNoShade ? RenderType::text : RenderType::entityCutout;
        ModelPart model = context.bakeLayer(modelLocation);
        this.meteorRock = model.getChild("meteor");
        this.meteorEmissive = model.getChild("meteor_emissive");
        this.fireLayer = model.getChild("overlay");
    }

    @Override
    public void render(Entity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        if (entity.tickCount >= 2 || !(this.entityRenderDispatcher.camera.getEntity().distanceToSqr(entity) < 12.25)) {
            super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
            poseStack.pushPose();
            poseStack.translate(0, entity.getBbHeight() / 2f, 0);
            poseStack.scale(scale, scale, scale);
            float si = (float) (Math.sin(System.currentTimeMillis() / 8000.0) * 400);
            float s2 = (float) (Math.sin(System.currentTimeMillis() / 5000.0) * 700);

            poseStack.mulPose(Axis.YP.rotationDegrees(si));
            poseStack.mulPose(Axis.XP.rotationDegrees(s2));


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
            poseStack.popPose();
        }
    }

    @Override
    protected int getSkyLightLevel(Entity entity, BlockPos pos) {
        return super.getSkyLightLevel(entity, pos);
    }

    @Override
    protected int getBlockLightLevel(Entity entity, BlockPos pos) {
        // otherwise its always 15 since its on fire
        return  entity.level().getBrightness(LightLayer.BLOCK, pos);
    }

    private float xOffset(float tickCount) {
        return tickCount * 0.01F;
    }

    @Override
    public ResourceLocation getTextureLocation(Entity entity) {
        return texture;
    }

    public static LayerDefinition createMesh(int size) {
        int r = size / 2;
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("meteor", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-r, -r, -r, size, size, size),
                PartPose.offset(0, 0, 0));

        root.addOrReplaceChild("meteor_emissive", CubeListBuilder.create()
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
