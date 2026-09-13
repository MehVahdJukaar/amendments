package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

//the little flame on a held torch or candle holder. billboard so it never inherits the item rotation
class HeldFlame {

    //TODO: fix for animated particles
    static void submit(PoseStack poseStack, SubmitNodeCollector collector, Identifier texture,
                       Vec3 offset, float size, float ageInTicks) {
        float period = 20;
        float t = (ageInTicks % period) / period;
        float scale = (1.0F - t * t * 0.4F) * size / 16f;

        Matrix4f mat = new Matrix4f();
        Quaternionf cameraRot = Minecraft.getInstance().gameRenderer.getMainCamera().rotation();

        poseStack.translate(offset.x, offset.y, offset.z);
        mat.setTranslation(poseStack.last().pose().getTranslation(new Vector3f()));
        mat.rotate(cameraRot);

        poseStack.last().pose().set(mat);
        poseStack.scale(-scale, scale, -scale);

        collector.submitCustomGeometry(poseStack, RenderTypes.text(texture), HeldFlame::quad);
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer builder) {
        vert(pose, builder, -0.5f, 0.5f, 1, 0);
        vert(pose, builder, 0.5f, 0.5f, 0, 0);
        vert(pose, builder, 0.5f, -0.5f, 0, 1);
        vert(pose, builder, -0.5f, -0.5f, 1, 1);
    }

    private static void vert(PoseStack.Pose pose, VertexConsumer builder, float x, float y, float u, float v) {
        builder.addVertex(pose, x, y, 0);
        builder.setColor(-1);
        builder.setUv(u, v);
        builder.setOverlay(OverlayTexture.NO_OVERLAY);
        builder.setLight(LightCoordsUtil.FULL_BRIGHT);
        builder.setNormal(pose, 0, 0, -1);
    }
}
