package net.mehvahdjukaar.amendments.mixins.fabric;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import foundationgames.enhancedblockentities.EnhancedBlockEntities;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

//Forces EBE to change the blocks we do change
@Pseudo
@Mixin(value = EnhancedBlockEntities.class, remap = false)
public abstract class CompatEBEMixin {

    @ModifyExpressionValue(method = "load", at =
    @At(value = "FIELD", target = "Lfoundationgames/enhancedblockentities/config/EBEConfig;renderEnhancedSigns:Z"))
    private static boolean amendments$keepOurSigns(boolean original) {
        return original && !ClientConfigs.hasCustomHangingSignRenderer() && !ClientConfigs.PIXEL_CONSISTENT_SIGNS.get();
    }

    @ModifyExpressionValue(method = "load", at =
    @At(value = "FIELD", target = "Lfoundationgames/enhancedblockentities/config/EBEConfig;renderEnhancedBells:Z"))
    private static boolean amendments$keepOurBells(boolean original) {
        return original && !ClientConfigs.BELL_CONNECTION.get();
    }
}
