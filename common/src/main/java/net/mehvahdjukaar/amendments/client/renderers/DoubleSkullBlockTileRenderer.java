package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.amendments.common.tile.DoubleSkullBlockTile;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class DoubleSkullBlockTileRenderer extends SkullWithWaxTileRenderer<DoubleSkullBlockTile, DoubleSkullRenderState> {

    public DoubleSkullBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public DoubleSkullRenderState createRenderState() {
        return new DoubleSkullRenderState();
    }

    @Override
    public void extractRenderState(DoubleSkullBlockTile tile, DoubleSkullRenderState state, float partialTicks,
                                   Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        BlockEntity up = tile.getSkullTileUp();
        state.upperSkull = extractInner(up, partialTicks, breakProgress);
        state.upperYaw = up == null ? 0 : -22.5F * (float) up.getBlockState().getValue(SkullBlock.ROTATION);
    }

    @Override
    public void submit(DoubleSkullRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);

        if (state.upperSkull != null) {
            poseStack.translate(0, 0.5, 0);
            this.dispatcher.submit(state.upperSkull, poseStack, collector, camera);
            this.submitWax(poseStack, collector, state.lightCoords, state.waxTexture, state.upperYaw);
        }
    }
}
