package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.client.renderers.ExtendedSignRenderState;
import net.mehvahdjukaar.amendments.client.renderers.SignRendererExtension;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.minecraft.client.renderer.blockentity.StandingSignRenderer;
import net.minecraft.client.renderer.blockentity.state.StandingSignRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StandingSignRenderer.class)
public abstract class StandingSignRendererMixin {

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void amendments$pixelConsistent(SignBlockEntity blockEntity, StandingSignRenderState state, float partialTicks,
                                            Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress,
                                            CallbackInfo ci) {
        BlockState blockState = blockEntity.getBlockState();
        if (ClientConfigs.isPixelConsistentSign(blockState)) {
            SignRendererExtension.makePixelConsistent(state, blockState);
        } else {
            ((ExtendedSignRenderState) state).amendments$setPixelConsistent(false);
        }
    }
}
