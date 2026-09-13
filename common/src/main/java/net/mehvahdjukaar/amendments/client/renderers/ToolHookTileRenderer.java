package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.amendments.common.tile.ToolHookBlockTile;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ToolHookTileRenderer implements BlockEntityRenderer<ToolHookBlockTile, ToolHookRenderState> {

    private final ItemModelResolver itemModelResolver;

    public ToolHookTileRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public ToolHookRenderState createRenderState() {
        return new ToolHookRenderState();
    }

    @Override
    public boolean shouldRender(ToolHookBlockTile blockEntity, Vec3 cameraPos) {
        return blockEntity.shouldRenderFancy();
    }

    @Override
    public void extractRenderState(ToolHookBlockTile blockEntity, ToolHookRenderState state, float partialTicks,
                                   Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        ItemStack item = blockEntity.getDisplayedItem();
        this.itemModelResolver.updateForTopItem(state.item, item, ItemDisplayContext.GUI,
                blockEntity.getLevel(), null, (int) blockEntity.getBlockPos().asLong());
        state.facing = blockEntity.getBlockState().getValue(TripWireHookBlock.FACING);
        state.digger = item.has(DataComponents.TOOL);
    }

    @Override
    public void submit(ToolHookRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.item.isEmpty()) return;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        float scale = 12 / 16f;
        float x = state.digger ? 1 / 16f : 0;

        poseStack.mulPose(RotHlpr.rot(state.facing));
        poseStack.mulPose(Axis.ZP.rotationDegrees(225));
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-x, 0, 1.4f / (16f * scale));

        state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}
