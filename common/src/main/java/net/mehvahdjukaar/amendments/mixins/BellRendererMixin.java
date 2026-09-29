package net.mehvahdjukaar.amendments.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.mehvahdjukaar.amendments.client.renderers.ExtendedBellRenderState;
import net.mehvahdjukaar.amendments.common.IBellConnection;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BellRenderer;
import net.minecraft.client.renderer.blockentity.state.BellRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BellRenderer.class)
public abstract class BellRendererMixin {

    @Unique
    private final RandomSource amendments$random = RandomSource.create();

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    public void amendments$extractConnection(BellBlockEntity tile, BellRenderState state, float partialTicks,
                                             Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress,
                                             CallbackInfo ci) {
        BlockStateModel model = null;
        if (tile instanceof IBellConnection connection && ClientConfigs.BELL_CONNECTION.get()) {
            Identifier id = switch (connection.amendments$getConnection()) {
                case ROPE -> AmendmentsClient.BELL_ROPE;
                case CHAIN -> AmendmentsClient.BELL_CHAIN;
                case NONE -> null;
            };
            if (id != null) model = ClientHelper.getStandaloneModel(id);
        }
        ((ExtendedBellRenderState) state).amendments$setConnectionModel(model);
    }

    @Inject(method = "submit", at = @At("HEAD"))
    public void amendments$submitConnection(BellRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                                            CameraRenderState camera, CallbackInfo ci) {
        BlockStateModel model = ((ExtendedBellRenderState) state).amendments$getConnectionModel();
        if (model == null) return;
        RenderUtil.submitBlockModel(poseStack, collector, model, Minecraft.getInstance().level, state.blockPos,
                null, amendments$random, true, state.lightCoords, OverlayTexture.NO_OVERLAY);
    }
}
