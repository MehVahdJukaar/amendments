package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.amendments.common.block.CeilingBannerBlock;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.banner.BannerFlagModel;
import net.minecraft.client.model.object.banner.BannerModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class CeilingBannerBlockTileRenderer implements BlockEntityRenderer<BannerBlockEntity, CeilingBannerRenderState> {

    //the flag part sits at -44 on the standing model, we want it at -32 so it hangs from the bar
    private static final float FLAG_DROP = 12 / 16f;

    private final SpriteGetter sprites;
    private final BannerModel model;
    private final BannerFlagModel flagModel;

    public CeilingBannerBlockTileRenderer(BlockEntityRendererProvider.Context context) {
        this.sprites = context.sprites();
        this.model = new BannerModel(context.bakeLayer(ModelLayers.STANDING_BANNER));
        this.flagModel = new BannerFlagModel(context.bakeLayer(ModelLayers.STANDING_BANNER_FLAG));
    }

    @Override
    public CeilingBannerRenderState createRenderState() {
        return new CeilingBannerRenderState();
    }

    @Override
    public void extractRenderState(BannerBlockEntity tile, CeilingBannerRenderState state, float partialTicks,
                                   Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        BlockState blockState = tile.getBlockState();
        state.attached = blockState.getValue(CeilingBannerBlock.ATTACHED);
        state.facing = blockState.getValue(CeilingBannerBlock.FACING);
        state.baseColor = tile.getBaseColor();
        state.patterns = tile.getPatterns();

        long gameTime = tile.getLevel() != null ? tile.getLevel().getGameTime() : 0L;
        BlockPos pos = tile.getBlockPos();
        state.phase = ((float) Math.floorMod(pos.getX() * 7 + pos.getY() * 9 + pos.getZ() * 13 + gameTime, 100L) + partialTicks) / 100.0F;
    }

    @Override
    public void submit(CeilingBannerRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        if (state.attached) {
            poseStack.translate(0, 0.625, 0);
        }
        poseStack.translate(0.5D, -0.3125 - 0.0208333333333, 0.5D); //1/32 * 2/3
        poseStack.mulPose(RotHlpr.rot(state.facing));
        poseStack.scale(-0.6666667F, -0.6666667F, 0.6666667F);

        int light = state.lightCoords;
        collector.submitModel(this.model, Unit.INSTANCE, poseStack, light, OverlayTexture.NO_OVERLAY, -1,
                Sheets.BANNER_BASE, this.sprites, 0, state.breakProgress);

        poseStack.pushPose();
        poseStack.translate(0, FLAG_DROP, 0);
        collector.submitModel(this.flagModel, state.phase, poseStack, light, OverlayTexture.NO_OVERLAY, -1,
                Sheets.BANNER_BASE, this.sprites, 0, state.breakProgress);
        BannerRenderer.submitPatterns(this.sprites, poseStack, collector, light, OverlayTexture.NO_OVERLAY,
                this.flagModel, state.phase, true, state.baseColor, state.patterns, state.breakProgress);
        poseStack.popPose();

        poseStack.popPose();
    }
}
