package net.mehvahdjukaar.amendments.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import vectorwing.farmersdelight.client.gui.CanvasSignEditScreen;

@Pseudo
@Mixin(targets = {"net.minecraft.client.gui.screens.inventory.SignEditScreen", "vectorwing.farmersdelight.client.gui.CanvasSignEditScreen"})
public class SignEditScreenMixin {

    @WrapOperation(method = "renderSignBackground", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V"))
    public void amendments$renderSignBackground(PoseStack instance, float x, float y, float z, Operation<Void> original) {
        if (ClientConfigs.PIXEL_CONSISTENT_SIGNS.get()) {
            float s = 62.500004F * 3 / 2;
            instance.scale(s, s, s);
            instance.translate(0.0D, -7/16f, -0.125D);
        } else {
            original.call(instance, x, y, z);
        }
    }
}
