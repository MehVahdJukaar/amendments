package net.mehvahdjukaar.amendments.mixins.forge;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import vectorwing.farmersdelight.client.gui.CanvasSignEditScreen;

@Pseudo
@Mixin(CanvasSignEditScreen.class)
public class SignEditScreenMixinFD {

    @WrapOperation(method = "renderSignBackground",
            remap = true,
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V"))
    public void amendments$renderSignBackground(PoseStack instance, float x, float y, float z, Operation<Void> original) {
        if (ClientConfigs.PIXEL_CONSISTENT_SIGNS.get()) {
            float a = (62.500004F * 3 / 2f);
            instance.translate(0.0D, -7 / 16f * a, -0.125D * a);
            instance.scale(3 / 2f, 3 / 2f, 3 / 2f);
        }
        original.call(instance, x, y, z);
    }
}
