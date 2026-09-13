package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.mehvahdjukaar.amendments.client.model.SkullCandleOverlayModel;
import net.mehvahdjukaar.amendments.common.block.WallCandleSkullBlock;
import net.mehvahdjukaar.amendments.common.tile.EnhancedSkullBlockTile;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public abstract class SkullWithWaxTileRenderer<T extends EnhancedSkullBlockTile, S extends SkullWithWaxRenderState>
        implements BlockEntityRenderer<T, S> {

    protected final BlockEntityRenderDispatcher dispatcher;
    protected final SkullCandleOverlayModel overlay;

    protected SkullWithWaxTileRenderer(BlockEntityRendererProvider.Context context) {
        this.overlay = new SkullCandleOverlayModel(context.bakeLayer(AmendmentsClient.SKULL_CANDLE_OVERLAY));
        this.dispatcher = context.blockEntityRenderDispatcher();
    }

    @Override
    public void extractRenderState(T tile, S state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        BlockEntity inner = tile.getSkullTile();
        state.skull = inner == null ? null :
                this.dispatcher.tryExtractRenderState(inner, partialTicks, breakProgress);

        BlockState blockState = tile.getBlockState();
        if (blockState.hasProperty(WallCandleSkullBlock.FACING)) {
            state.wall = true;
            state.yaw = blockState.getValue(WallCandleSkullBlock.FACING).toYRot();
        } else {
            state.wall = false;
            //let the base block master the rotation
            int innerRot = inner == null ? 0 : inner.getBlockState().getValue(SkullBlock.ROTATION);
            state.yaw = -22.5F * (blockState.getValue(SkullBlock.ROTATION) - innerRot);
        }
        state.waxTexture = tile.getWaxTexture();
    }

    @Override
    public void submit(S state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.skull == null) return;

        if (!state.wall) {
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.yaw));
            poseStack.translate(-0.5, -0.5, -0.5);
        } else {
            poseStack.translate(0, -0.25, 0);
        }

        this.dispatcher.submit(state.skull, poseStack, collector, camera);

        if (state.wall) {
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(180 - state.yaw));
            poseStack.translate(-0.5, -0.25, -0.25);
        }
        //leaves the matrix rotated for wax
    }

    protected void submitWax(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords,
                             @Nullable Identifier texture, float yaw) {
        if (texture == null) return;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.25, 0.5);

        float s = 1.077f;
        poseStack.scale(-s, -s, s);
        poseStack.translate(0, 0.25f, 0);

        SkullModelBase.State modelState = new SkullModelBase.State();
        modelState.yRot = -yaw;
        collector.submitModel(this.overlay, modelState, poseStack, RenderTypes.entityCutoutZOffset(texture),
                lightCoords, OverlayTexture.NO_OVERLAY, 0, null);

        poseStack.popPose();
    }

    protected @Nullable BlockEntityRenderState extractInner(@Nullable BlockEntity tile, float partialTicks,
                                                            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        return tile == null ? null : this.dispatcher.tryExtractRenderState(tile, partialTicks, breakProgress);
    }
}
