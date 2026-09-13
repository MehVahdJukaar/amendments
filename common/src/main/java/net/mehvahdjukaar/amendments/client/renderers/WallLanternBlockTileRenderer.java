package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.amendments.client.WallLanternModelsManager;
import net.mehvahdjukaar.amendments.common.block.WallLanternBlock;
import net.mehvahdjukaar.amendments.common.tile.WallLanternBlockTile;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

public class WallLanternBlockTileRenderer implements BlockEntityRenderer<WallLanternBlockTile, WallLanternRenderState> {

    private final RandomSource random = RandomSource.create();

    public WallLanternBlockTileRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public WallLanternRenderState createRenderState() {
        return new WallLanternRenderState();
    }

    @Override
    public boolean shouldRender(WallLanternBlockTile blockEntity, Vec3 cameraPos) {
        return blockEntity.shouldRenderFancy(cameraPos);
    }

    @Override
    public void extractRenderState(WallLanternBlockTile tile, WallLanternRenderState state, float partialTicks,
                                   Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        Direction facing = tile.getBlockState().getValue(WallLanternBlock.FACING);
        state.facing = facing;
        state.angle = tile.amendments$getAnimation().getAngle(partialTicks);
        state.attachmentOffset = tile.getAttachmentOffset();

        BlockState lanternState = tile.getLanternState();
        if (lanternState.hasProperty(HorizontalDirectionalBlock.FACING)) {
            lanternState = lanternState.setValue(HorizontalDirectionalBlock.FACING, facing);
        }
        state.lanternState = lanternState;
        state.model = WallLanternModelsManager.getLanternModel(tile.getOwnBlock().type, lanternState);
    }

    @Override
    public void submit(WallLanternRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.model == null) return;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.875, 0.5);
        poseStack.mulPose(RotHlpr.rot(state.facing));
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.angle));
        poseStack.translate(-0.5, -0.75 - state.attachmentOffset, -0.375);

        //the model is baked for the wall block, undo its facing so the swing happens around the chain
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(new Quaternionf(RotHlpr.rot(state.facing)).conjugate());
        poseStack.translate(-0.5, -0.5, -0.5);

        RenderUtil.submitBlockModel(poseStack, collector, state.model, Minecraft.getInstance().level,
                state.blockPos, state.lanternState, this.random, true, state.lightCoords, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
        poseStack.popPose();
    }
}
