package net.mehvahdjukaar.amendments.mixins.fabric;

import foundationgames.enhancedblockentities.EnhancedBlockEntityRegistry;
import foundationgames.enhancedblockentities.client.render.BlockEntityRenderCondition;
import foundationgames.enhancedblockentities.client.render.BlockEntityRendererOverride;
import net.mehvahdjukaar.amendments.integration.EBECompat;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = EnhancedBlockEntityRegistry.class, remap = false)
public abstract class CompatEBEMixin {

    @Inject(method = "register", at = @At("HEAD"), cancellable = true)
    private static void amendments$keepOurRenderers(Block block, BlockEntityType<?> type, BlockEntityRenderCondition condition,
                                                    BlockEntityRendererOverride override, CallbackInfo ci) {
        if (EBECompat.rendersOurselves(block)) ci.cancel();
    }
}
