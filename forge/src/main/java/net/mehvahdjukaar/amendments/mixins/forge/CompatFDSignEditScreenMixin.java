package net.mehvahdjukaar.amendments.mixins.forge;

import net.mehvahdjukaar.amendments.client.renderers.SignRendererExtension;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.client.gui.CanvasSignEditScreen;

@Pseudo
@Mixin(CanvasSignEditScreen.class)
public abstract class CompatFDSignEditScreenMixin extends AbstractSignEditScreen {


    public CompatFDSignEditScreenMixin(SignBlockEntity sign, boolean isFrontText, boolean isFiltered) {
        super(sign, isFrontText, isFiltered);
    }

    @Inject(method = "renderSignBackground",
            at = @At("HEAD"), cancellable = true)
    public void amendments$renderSignBlockModelFD(GuiGraphics guiGraphics, BlockState state, CallbackInfo ci) {
        if (ClientConfigs.PIXEL_CONSISTENT_SIGNS.get()) {
            Block block = state.getBlock();
            boolean b = block instanceof WallSignBlock;
            if (b || block instanceof StandingSignBlock) {
                SignRendererExtension.renderSignBlockModelInGui(guiGraphics, b, state, !this.isFrontText);
                ci.cancel();
            }
        }
    }
}
