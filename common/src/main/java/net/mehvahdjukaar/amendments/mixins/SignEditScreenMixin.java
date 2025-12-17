package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.mehvahdjukaar.amendments.client.renderers.SignRendererExtension;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SignEditScreen.class)
public class SignEditScreenMixin {

    @Inject(method = "renderSignBackground",
            at = @At("HEAD"), cancellable = true)
    public void amendments$renderSignBlockModel(GuiGraphics guiGraphics, BlockState state, CallbackInfo ci) {
        if (ClientConfigs.isPixelConsistentSign(state)) {
            Block block = state.getBlock();
            boolean b = block instanceof WallSignBlock;
            if (b || block instanceof StandingSignBlock) {
                SignRendererExtension.renderSignBlockModelInGui(guiGraphics, b, state, false);
                ci.cancel();
            }
        }
    }
}
