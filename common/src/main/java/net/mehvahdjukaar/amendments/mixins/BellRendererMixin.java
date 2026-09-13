package net.mehvahdjukaar.amendments.mixins;

import net.minecraft.client.renderer.blockentity.BellRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BellRenderer.class)
public abstract class BellRendererMixin {

    //bell rope/chain were standalone models, moonlight doesnt wrap that api yet so there is no
    //model to draw. bring this back once ClientHelper gets standalone model registration
    //@Inject(method = "render*", at = @At("HEAD"))
    //public void render(BellBlockEntity tile, float partialTicks, PoseStack matrixStackIn,
    //                   MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, CallbackInfo info) {
    //    if (tile instanceof IBellConnection connections && ClientConfigs.BELL_CONNECTION.get()) {
    //        Identifier model = switch (connections.amendments$getConnection()) {
    //            case ROPE -> AmendmentsClient.BELL_ROPE;
    //            case CHAIN -> AmendmentsClient.BELL_CHAIN;
    //            default -> null;
    //        };
    //        int light = LevelRenderer.getLightColor(tile.getLevel(), tile.getBlockPos().below());
    //        if (model != null) {
    //            RenderUtil.renderModel(
    //                    RenderUtil.getStandaloneModelLocation(model), matrixStackIn, bufferIn,
    //                    Minecraft.getInstance().getBlockRenderer(),
    //                    light, combinedOverlayIn, true);
    //        }
    //    }
    //}
}
