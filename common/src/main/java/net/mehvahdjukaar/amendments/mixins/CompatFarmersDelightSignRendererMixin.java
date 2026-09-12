package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.client.renderers.ExtendedSignRenderState;
import net.mehvahdjukaar.amendments.client.renderers.SignRendererExtension;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.client.renderer.StandingCanvasSignRenderer;
import vectorwing.farmersdelight.client.renderer.state.StandingCanvasSignRenderState;

@Pseudo
@Mixin(StandingCanvasSignRenderer.class)
public abstract class CompatFarmersDelightSignRendererMixin {

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void amendments$pixelConsistent(SignBlockEntity blockEntity, StandingCanvasSignRenderState state,
                                            float partialTicks, Vec3 cameraPosition,
                                            @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress,
                                            CallbackInfo ci) {
        if (ClientConfigs.PIXEL_CONSISTENT_SIGNS.get()) {
            SignRendererExtension.makePixelConsistent(state, blockEntity.getBlockState());
        } else {
            ((ExtendedSignRenderState) state).amendments$setPixelConsistent(false);
        }
    }
}
