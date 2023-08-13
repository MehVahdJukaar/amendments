package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.amendments.client.WallLanternModelsManager;
import net.mehvahdjukaar.amendments.common.block.WallLanternBlock;
import net.mehvahdjukaar.amendments.common.tile.WallLanternBlockTile;
import net.mehvahdjukaar.amendments.integration.CompatHandler;
import net.mehvahdjukaar.amendments.integration.ShimmerCompat;
import net.mehvahdjukaar.moonlight.api.client.util.LOD;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;


public class WallLanternBlockTileRenderer implements BlockEntityRenderer<WallLanternBlockTile> {
    protected final BlockRenderDispatcher blockRenderer;
    private final Camera camera;

    public WallLanternBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = context.getBlockRenderDispatcher();
        this.camera = Minecraft.getInstance().gameRenderer.getMainCamera();
    }


    public void renderLantern(WallLanternBlockTile tile, BlockState lanternState, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn,
                              int combinedLightIn, int combinedOverlayIn, boolean ceiling) {
        poseStack.pushPose();
        // rotate towards direction
        poseStack.translate(0.5, 0.875, 0.5);
        poseStack.mulPose(RotHlpr.rot(tile.getBlockState().getValue(WallLanternBlock.FACING)));

        float angle = tile.animation.getAngle(partialTicks);

        // animation
        poseStack.mulPose(Axis.ZP.rotationDegrees(angle));
        poseStack.translate(-0.5, -0.75 - tile.getAttachmentOffset(), -0.375);

        BakedModel model = WallLanternModelsManager.getModel(
                blockRenderer.getBlockModelShaper(), lanternState);
        // render block
        if (CompatHandler.SHIMMER) {
            ShimmerCompat.renderWithBloom(poseStack, (p, b) ->
                    RenderUtil.renderBlock(model, 0, p, b, lanternState, tile.getLevel(), tile.getBlockPos(), blockRenderer));
        } else {
            RenderUtil.renderBlock(model, 0, poseStack, bufferIn, lanternState, tile.getLevel(), tile.getBlockPos(), blockRenderer);
        }

        poseStack.popPose();
    }


    @Override
    public void render(WallLanternBlockTile tile, float partialTicks, PoseStack matrixStackIn,
                       MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        if (tile.shouldRenderFancy()) {
            this.renderLantern(tile, tile.getHeldBlock(), partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, false);
        }

        LOD lod = new LOD(camera, tile.getBlockPos());

        tile.setFancyRenderer(lod.isNear());
    }
}