package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.mehvahdjukaar.amendments.client.WallLanternModelsManager;
import net.mehvahdjukaar.amendments.common.LanternRegistry;
import net.mehvahdjukaar.amendments.common.block.WallLanternBlock;
import net.mehvahdjukaar.amendments.common.tile.WallLanternBlockTile;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.amendments.integration.CompatHandler;
import net.mehvahdjukaar.amendments.integration.ShimmerCompat;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;


public class WallLanternBlockTileRenderer implements BlockEntityRenderer<WallLanternBlockTile> {
    protected final BlockRenderDispatcher blockRenderer;

    public WallLanternBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public boolean shouldRender(WallLanternBlockTile blockEntity, Vec3 cameraPos) {
        return blockEntity.shouldRenderFancy(cameraPos);
    }

    public void renderLantern(WallLanternBlockTile tile, BlockState lanternState, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn,
                              int combinedLightIn, int combinedOverlayIn, boolean ceiling) {
        poseStack.pushPose();

        Direction facing = tile.getBlockState().getValue(WallLanternBlock.FACING);

        // Position and sway are applied in the facing frame so the lantern hangs off the right wall
        // and swings about the correct world axis.
        poseStack.translate(0.5, 0.875, 0.5);
        poseStack.mulPose(RotHlpr.rot(facing));
        float angle = tile.amendments$getAnimation().getAngle(partialTicks);
        poseStack.mulPose(Axis.ZP.rotationDegrees(angle));
        poseStack.translate(-0.5, -0.75 - tile.getAttachmentOffset(), -0.375);

        boolean entityShading = ClientConfigs.LANTERN_ENTITY_SHADING.get();
        LanternRegistry.LanternType type = tile.getOwnBlock().type;
        Level level = tile.getLevel();
        BlockPos pos = tile.getBlockPos();

        if (CompatHandler.SHIMMER) {
            ShimmerCompat.renderWithBloom(poseStack, (p, b) ->
                    renderModel(type, lanternState, facing, entityShading, p, b, combinedLightIn, combinedOverlayIn, level, pos));
        } else {
            renderModel(type, lanternState, facing, entityShading, poseStack, bufferIn, combinedLightIn, combinedOverlayIn, level, pos);
        }

        poseStack.popPose();
    }

    private void renderModel(LanternRegistry.LanternType type, BlockState lanternState, Direction facing, boolean entityShading,
                             PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, Level level, BlockPos pos) {
        // Facing-aware lantern models bake the blockstate rotation into their quads, so make the model
        // match the wall it's on.
        if (lanternState.hasProperty(HorizontalDirectionalBlock.FACING)) {
            lanternState = lanternState.setValue(HorizontalDirectionalBlock.FACING, facing);
        }

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        // Cancel the pose's facing rotation (applied above for position/sway) on the model itself.
        // Facing-less models end up axis-aligned, so block-baked per-face shading stays correct; facing-
        // aware models keep their own baked blockstate rotation, so they still face the wall without being
        // rotated twice. This also compensates the baked rotation for the item renderer.
        poseStack.mulPose(new Quaternionf(RotHlpr.rot(facing)).conjugate());
        poseStack.translate(-0.5, -0.5, -0.5);

        BakedModel model = WallLanternModelsManager.getLanternModel(blockRenderer.getBlockModelShaper(), type, lanternState);
        if (entityShading) {
            // Item/entity rendering shades from the (pose-transformed) normals instead of the block
            // model's baked per-face shade, so rotated (facing-aware) lanterns shade correctly.
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            RenderType renderType = ItemBlockRenderTypes.getRenderType(lanternState, true);
            VertexConsumer vc = buffer.getBuffer(renderType);
            itemRenderer.renderModelLists(model, ItemStack.EMPTY, light, overlay, poseStack, vc);
        } else {
            RenderUtil.renderBlock(model, 0, poseStack, buffer, lanternState, level, pos, blockRenderer);
        }
        poseStack.popPose();
    }

    @Override
    public void render(WallLanternBlockTile tile, float partialTicks, PoseStack matrixStackIn,
                       MultiBufferSource bufferIn, int combinedLightIn,
                       int combinedOverlayIn) {
        this.renderLantern(tile, tile.getLanternState(), partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, false);
    }

}
