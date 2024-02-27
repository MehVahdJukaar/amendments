package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.mehvahdjukaar.amendments.common.IBetterJukebox;
import net.mehvahdjukaar.amendments.common.tile.ToolHookBlockTile;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

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

        itemRenderer.renderStatic(item, ItemDisplayContext.NONE, packedLight, packedOverlay,
                poseStack, bufferSource, blockEntity.getLevel(), 0);
    }

}
