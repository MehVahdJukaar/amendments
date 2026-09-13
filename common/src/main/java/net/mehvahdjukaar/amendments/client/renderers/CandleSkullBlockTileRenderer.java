package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.amendments.common.block.WallCandleSkullBlock;
import net.mehvahdjukaar.amendments.common.tile.CandleSkullBlockTile;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class CandleSkullBlockTileRenderer extends SkullWithWaxTileRenderer<CandleSkullBlockTile, CandleSkullRenderState> {

    private static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();

    private final BlockModelResolver blockModelResolver;

    public CandleSkullBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.blockModelResolver = context.blockModelResolver();
    }

    @Override
    public CandleSkullRenderState createRenderState() {
        return new CandleSkullRenderState();
    }

    @Override
    public void extractRenderState(CandleSkullBlockTile tile, CandleSkullRenderState state, float partialTicks,
                                   Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);

        BlockState blockState = tile.getBlockState();
        BlockState candle = tile.getCandle();
        state.hasCandle = !candle.isAir();
        if (!state.hasCandle) return;

        candle = candle.setValue(CandleBlock.LIT, blockState.getValue(CandleBlock.LIT))
                .setValue(CandleBlock.CANDLES, blockState.getValue(CandleBlock.CANDLES));
        this.blockModelResolver.update(state.candle, candle, BLOCK_DISPLAY_CONTEXT);

        if (blockState.hasProperty(WallCandleSkullBlock.FACING)) {
            state.candleYaw = blockState.getValue(WallCandleSkullBlock.FACING).toYRot();
        } else {
            state.candleYaw = -22.5F * (float) (blockState.getValue(SkullBlock.ROTATION));
        }
    }

    @Override
    public void submit(CandleSkullRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);

        if (state.hasCandle) {
            this.submitWax(poseStack, collector, state.lightCoords, state.waxTexture, state.candleYaw);

            poseStack.pushPose();
            poseStack.translate(0, 0.5, 0);
            state.candle.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }
}
