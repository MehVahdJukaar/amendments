package net.mehvahdjukaar.amendments.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.amendments.client.renderers.ExtendedSignRenderState;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

//fixes plain text shade
@Mixin(AbstractSignRenderer.class)
public abstract class SignRendererMixin {

    @ModifyExpressionValue(method = "submitSignText",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/AbstractSignRenderer;getDarkColor(Lnet/minecraft/world/level/block/entity/SignText;)I"))
    private int amendments$shadeText(int original, SignRenderState state, PoseStack poseStack,
                                     SubmitNodeCollector collector, SignText signText) {
        int color = signText.getColor().getTextColor();
        if (color == DyeColor.BLACK.getTextColor() && signText.hasGlowingText()) return original;
        var transformation = signText == state.backText ? state.transformations.backText() : state.transformations.frontText();
        Vector3f normal = transformation.getMatrix().transformDirection(new Vector3f(0, 0, 1)).normalize();
        return ColorUtils.multiply(color, 0.4f * ClientConfigs.getSignColorMult() * ColorUtils.getShading(normal));
    }

    @WrapWithCondition(method = "submitSignWithText",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/AbstractSignRenderer;submitSign(Lcom/mojang/blaze3d/vertex/PoseStack;ILnet/minecraft/world/level/block/state/properties/WoodType;Lnet/minecraft/client/model/Model$Simple;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V"))
    private boolean amendments$skipSignModel(AbstractSignRenderer<?> instance, PoseStack poseStack, int lightCoords,
                                             WoodType type, Model.Simple signModel,
                                             ModelFeatureRenderer.CrumblingOverlay breakProgress,
                                             SubmitNodeCollector collector,
                                             @Local(argsOnly = true) SignRenderState state) {
        return !((ExtendedSignRenderState) state).amendments$isPixelConsistent();
    }
}
