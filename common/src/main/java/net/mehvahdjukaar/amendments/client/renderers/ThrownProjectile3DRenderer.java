package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.amendments.common.entity.IVisualTransformationProvider;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public abstract class ThrownProjectile3DRenderer<E extends Entity, S extends ThrownProjectile3DRenderState>
        extends EntityRenderer<E, S> {

    private final Identifier texture;
    private final float scale;

    public ThrownProjectile3DRenderer(EntityRendererProvider.Context context, float scale, Identifier texture) {
        super(context);
        this.scale = scale;
        this.texture = texture;
    }

    public Identifier getTexture() {
        return texture;
    }

    @Override
    public void extractRenderState(E entity, S state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yRot = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
        state.xRot = Mth.rotLerp(partialTicks, entity.xRotO, entity.getXRot());
        state.visualTransformation = entity instanceof IVisualTransformationProvider vp ?
                vp.amendments$getVisualTransformation(partialTicks) : null;
    }

    @Override
    public void submit(S state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        //freshly shot balls sit inside the shooters face for a tick or two
        if (state.ageInTicks < 2 && state.distanceToCameraSq < 12.25) return;
        super.submit(state, poseStack, collector, camera);

        poseStack.pushPose();
        poseStack.translate(0, state.boundingBoxHeight / 2f, 0);
        poseStack.scale(scale, scale, scale);

        poseStack.mulPose(Axis.YN.rotationDegrees(180.0F - state.yRot));
        poseStack.mulPose(Axis.XN.rotationDegrees(-state.xRot));

        if (state.visualTransformation != null) {
            poseStack.mulPose(state.visualTransformation);
        }

        submitBall(state, poseStack, collector, camera);
        poseStack.popPose();
    }

    protected abstract void submitBall(S state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera);

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
                        .texOffs(0, size*2)
                        .addBox(-(r + 1), -(r + 1), -(r + 1), (size + 2), (size + 2), (size + 2)),
                PartPose.offset(0, 0, 0));

        return LayerDefinition.create(mesh, 64, 64);
    }
}
