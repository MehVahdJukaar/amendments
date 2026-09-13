package net.mehvahdjukaar.amendments.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.object.skull.DragonHeadModel;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.blockentity.state.SkullBlockRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkullBlockRenderer.class)
public abstract class SkullBlockRendererMixin implements BlockEntityRenderer<SkullBlockEntity, SkullBlockRenderState> {

    @Unique
    private static final Identifier DRAGON_EYES = Identifier.withDefaultNamespace("textures/entity/enderdragon/dragon_eyes.png");

    @Inject(method = "submitSkull", at = @At("TAIL"))
    private static void amendments$addDragonEyes(float animationValue, PoseStack poseStack, SubmitNodeCollector collector,
                                                 int lightCoords, SkullModelBase model, RenderType renderType,
                                                 int outlineColor, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress,
                                                 CallbackInfo ci) {
        if (model instanceof DragonHeadModel) {
            SkullModelBase.State modelState = new SkullModelBase.State();
            modelState.animationPos = animationValue;
            collector.submitModel(model, modelState, poseStack, RenderTypes.eyes(DRAGON_EYES),
                    LightCoordsUtil.FULL_SKY, OverlayTexture.NO_OVERLAY, outlineColor, breakProgress);
        }
    }

    @ModifyReturnValue(method = "getSkullRenderType", at = @At("RETURN"))
    private static RenderType amendments$modifyDragonHeadRenderType(RenderType original,
                                                                    @Local(argsOnly = true) SkullBlock.Type type,
                                                                    @Local(argsOnly = true) Identifier texture) {
        if (type == SkullBlock.Types.DRAGON) {
            return RenderTypes.entityCutout(texture != null ? texture : SkullBlockRenderer.SKIN_BY_TYPE.get(type));
        }
        return original;
    }
}
