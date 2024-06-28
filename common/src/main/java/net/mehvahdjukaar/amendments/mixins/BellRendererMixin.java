package net.mehvahdjukaar.amendments.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.mehvahdjukaar.amendments.common.IBellConnection;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BellRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BellRenderer.class)
public abstract class BellRendererMixin {

    @Inject(method = "render*", at = @At("HEAD"))
    public void render(BellBlockEntity tile, float partialTicks, PoseStack matrixStackIn,
                       MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, CallbackInfo info) {
        if (tile instanceof IBellConnection connections && ClientConfigs.BELL_CONNECTION.get()) {
            ResourceLocation model = switch (connections.amendments$getConnection()) {
                case ROPE -> AmendmentsClient.BELL_ROPE;
                case CHAIN -> AmendmentsClient.BELL_CHAIN;
                default -> null;
            };
            int light = LevelRenderer.getLightColor(tile.getLevel(), tile.getBlockPos().below());
            if (model != null) {
                RenderUtil.renderModel(
                        model, matrixStackIn, bufferIn,
                        Minecraft.getInstance().getBlockRenderer(),
                        light, combinedOverlayIn, true);
            }
        }
    }
}
