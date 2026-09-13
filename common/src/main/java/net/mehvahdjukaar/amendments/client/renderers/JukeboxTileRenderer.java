package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.mehvahdjukaar.amendments.common.IBetterJukebox;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

//import net.mehvahdjukaar.amendments.integration.EtchedCompat;

public class JukeboxTileRenderer implements BlockEntityRenderer<JukeboxBlockEntity, JukeboxRenderState> {

    private final SpriteGetter sprites;

    public JukeboxTileRenderer(BlockEntityRendererProvider.Context context) {
        this.sprites = context.sprites();
    }

    @Override
    public JukeboxRenderState createRenderState() {
        return new JukeboxRenderState();
    }

    @Override
    public void extractRenderState(JukeboxBlockEntity blockEntity, JukeboxRenderState state, float partialTicks,
                                   Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        var item = blockEntity.getTheItem();
        if (item.isEmpty() || !blockEntity.getBlockState().getValue(JukeboxBlock.HAS_RECORD)) {
            state.record = null;
            return;
        }
        state.record = AmendmentsClient.getRecordMaterial(item.getItem());
        state.rotation = ((IBetterJukebox) blockEntity).amendments$getRotation(partialTicks);
        //the disc sticks out of the block so it takes the light from above
        state.upLightCoords = blockEntity.getLevel() == null ? state.lightCoords :
                LevelRenderer.getLightCoords(blockEntity.getLevel(), blockEntity.getBlockPos().above());
    }

    @Override
    public void submit(JukeboxRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.record == null) return;

        poseStack.pushPose();
        poseStack.translate(0.5, 15.25 / 16f, 0.5);
        if (ClientConfigs.JUKEBOX_SPIN.get()) {
            poseStack.mulPose(Axis.YP.rotationDegrees(state.rotation));
        }
        poseStack.mulPose(RotHlpr.X90);

        // etched shit
        //if (item.getItem() == CompatObjects.ETCHED_DISC.get()) {
        //    EtchedCompat.drawDisc(item, poseStack, bufferSource, lu, lv);
        //} else {
        TextureAtlasSprite sprite = this.sprites.get(state.record);
        int light = state.upLightCoords;
        collector.submitCustomGeometry(poseStack, state.record.renderType(RenderTypes::entityCutout),
                (pose, buffer) -> addDisc(pose, sprite.wrap(buffer), light));
        //}
        poseStack.popPose();
    }

    private static void addDisc(PoseStack.Pose pose, VertexConsumer builder, int light) {
        vert(pose, builder, -0.5f, 0.5f, 1, 0, light);
        vert(pose, builder, 0.5f, 0.5f, 0, 0, light);
        vert(pose, builder, 0.5f, -0.5f, 0, 1, light);
        vert(pose, builder, -0.5f, -0.5f, 1, 1, light);
    }

    private static void vert(PoseStack.Pose pose, VertexConsumer builder, float x, float y, float u, float v, int light) {
        builder.addVertex(pose, x, y, 0);
        builder.setColor(-1);
        builder.setUv(u, v);
        builder.setOverlay(OverlayTexture.NO_OVERLAY);
        builder.setLight(light);
        builder.setNormal(pose, 0, 0, -1);
    }
}
