package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.mehvahdjukaar.amendments.client.WallLanternModelsManager;
import net.mehvahdjukaar.amendments.common.block.WallLanternBlock;
import net.mehvahdjukaar.amendments.common.tile.WallLanternBlockTile;
import net.mehvahdjukaar.amendments.integration.CompatHandler;
import net.mehvahdjukaar.amendments.integration.ShimmerCompat;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;


public class WallLanternBlockTileRenderer implements BlockEntityRenderer<WallLanternBlockTile> {
    protected final BlockRenderDispatcher blockRenderer;

    public WallLanternBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public boolean shouldRender(WallLanternBlockTile blockEntity, Vec3 cameraPos) {
        return blockEntity.shouldRenderFancy(cameraPos) ;
    }

    public void renderLantern(WallLanternBlockTile tile, BlockState lanternState, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn,
                              int combinedLightIn, int combinedOverlayIn, boolean ceiling) {
        poseStack.pushPose();
        // rotate towards direction



        poseStack.translate(0.5, 0.875, 0.5);
        poseStack.mulPose(RotHlpr.rot(tile.getBlockState().getValue(WallLanternBlock.FACING)));


        float angle = tile.amendments$getAnimation().getAngle(partialTicks);

        // animation
        poseStack.mulPose(Axis.ZP.rotationDegrees(angle));
        poseStack.translate(-0.5, -0.75 - tile.getAttachmentOffset(), -0.375);

        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(RotHlpr.Y90);
        poseStack.translate(-0.5, -0.5, -0.5);

        BakedModel model = WallLanternModelsManager.getModel(
                blockRenderer.getBlockModelShaper(), lanternState);
        // render block
        Level level = tile.getLevel();
        BlockPos pos = tile.getBlockPos();
        if (CompatHandler.SHIMMER) {
            ShimmerCompat.renderWithBloom(poseStack, (p, b) ->
                    RenderUtil.renderBlock(model, 0, p, b, lanternState, level, pos, blockRenderer));
        } else {
            //TODO:
            if (false && AmendmentsClient.hasFixedNormals()) {
                //this has better shading for diagonal planes but below is same as in block model
                var vertexConsumer = bufferIn.getBuffer(ItemBlockRenderTypes.getRenderType(
                        Items.OBSIDIAN.getDefaultInstance(), true
                ));
                /*
                blockRenderer.renderBatched(model,
                        ItemStack.EMPTY,
                        combinedLightIn, combinedOverlayIn, poseStack, vertexConsumer);*/
            } else RenderUtil.renderBlock(model, 0, poseStack, bufferIn, lanternState, level, pos, blockRenderer);
        }

        poseStack.popPose();
    }


    @Override
    public void render(WallLanternBlockTile tile, float partialTicks, PoseStack matrixStackIn,
                       MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        this.renderLantern(tile, tile.getHeldBlock(), partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, false);
    }

}