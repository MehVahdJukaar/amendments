package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.mehvahdjukaar.amendments.common.IBetterJukebox;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;

public class JukeboxTileRenderer implements BlockEntityRenderer<JukeboxBlockEntity> {

    public JukeboxTileRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(JukeboxBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        var item = blockEntity.getFirstItem();
        if (!item.isEmpty() && blockEntity.getBlockState().getValue(JukeboxBlock.HAS_RECORD)) {
            poseStack.translate(0.5, 15.25 / 16f, 0.5);

            poseStack.mulPose(Axis.YP.rotationDegrees(((IBetterJukebox) blockEntity).getRotation(partialTick)));
            poseStack.mulPose(RotHlpr.X90);


            Material material = AmendmentsClient.getRecordMaterial(item.getItem());
            var builder = material.buffer(bufferSource, RenderType::entityCutout);
            int upLight = LevelRenderer.getLightColor(blockEntity.getLevel(), blockEntity.getBlockPos().above(2));
            int lu = upLight & '\uffff';
            int lv = upLight >> 16 & '\uffff';
            VertexUtil.addQuad(builder, poseStack, -0.5f, -0.5f, 0.5f, 0.5f, lu, lv);
        }
    }

}
