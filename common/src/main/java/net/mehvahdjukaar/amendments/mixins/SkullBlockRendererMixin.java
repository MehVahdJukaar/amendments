package net.mehvahdjukaar.amendments.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.mehvahdjukaar.supplementaries.common.block.blocks.EndermanSkullBlock;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.dragon.DragonHeadModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkullBlockRenderer.class)
public class SkullBlockRendererMixin {

    @ForgeOverride
    public AABB getRenderBoundingBox(BlockEntity tile) {
        return new AABB(tile.getBlockPos()).inflate(0.1);
    }

    @Unique
    private static final ResourceLocation DRAGON_EYES = ResourceLocation.withDefaultNamespace("textures/entity/enderdragon/dragon_eyes.png");

    @Inject(method = "renderSkull", at = @At(value = "INVOKE", shift = At.Shift.AFTER,
            target = "Lnet/minecraft/client/model/SkullModelBase;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"))
    private static void amendments$addDragonEyes(Direction direction, float yRot, float mouthAnimation,
                                                 PoseStack poseStack, MultiBufferSource bufferSource,
                                                 int packedLight, SkullModelBase model,
                                                 RenderType renderType, CallbackInfo ci) {

        if (model instanceof DragonHeadModel) {
            var vertexConsumer = bufferSource.getBuffer(RenderType.eyes(DRAGON_EYES));
            poseStack.pushPose();
            model.renderToBuffer(poseStack, vertexConsumer, LightTexture.FULL_SKY, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();

        }
    }

    @ModifyReturnValue(method = "getRenderType", at = @At("RETURN"))
    private static RenderType amendments$modifyDragonHeadRenderType(RenderType original,
                                                                         @Local ResourceLocation texture,
                                                                         @Local(argsOnly = true) SkullBlock.Type type) {
        if (type == SkullBlock.Types.DRAGON) {
            return RenderType.entityCutoutNoCull(texture);
        }
        return original;
    }
}
