package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Properties;

//same class in all 3 EBE forks. editing the props turns it off everywhere EBE reads the flag, not just in load
@Pseudo
@Mixin(targets = "foundationgames.enhancedblockentities.config.EBEConfig", remap = false)
public abstract class CompatEBEMixin {

    @Inject(method = "readFrom", at = @At("HEAD"))
    private void amendments$keepOurSignsAndBells(Properties properties, CallbackInfo ci) {
        if (ClientConfigs.PIXEL_CONSISTENT_SIGNS.get() || ClientConfigs.hasCustomHangingSignRenderer()) {
            properties.setProperty("render_enhanced_signs", "false");
        }
        if (ClientConfigs.BELL_CONNECTION.get()) {
            properties.setProperty("render_enhanced_bells", "false");
        }
    }
}
