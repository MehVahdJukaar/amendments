package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.amendments.common.tile.ToolHookBlockTile;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.phys.Vec3;

public class ToolHookTileRenderer implements BlockEntityRenderer<ToolHookBlockTile> {

    private final ItemRenderer itemRenderer;

    public ToolHookTileRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public boolean shouldRender(ToolHookBlockTile blockEntity, Vec3 cameraPos) {
        return blockEntity.shouldRenderFancy(cameraPos);
    }

    @Override
    public void render(ToolHookBlockTile blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack item = blockEntity.getDisplayedItem();

        poseStack.translate(0.5, 0.5, 0.5);

        float scale = 12 / 16f;

        float x = item.getItem() instanceof DiggerItem ? 1 / 16f : 0;

        poseStack.mulPose(RotHlpr.rot(blockEntity.getBlockState().getValue(TripWireHookBlock.FACING)));
        poseStack.mulPose(Axis.ZP.rotationDegrees(225));
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-x, 0, 1.4f / (16f * scale));

        var itemModel = itemRenderer.getItemModelShaper().getItemModel(item);

        itemRenderer.render(item, ItemDisplayContext.GUI, false,
                poseStack, bufferSource, packedLight, packedOverlay, itemModel);
    }

}
