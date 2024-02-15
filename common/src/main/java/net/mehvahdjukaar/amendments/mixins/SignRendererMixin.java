package net.mehvahdjukaar.amendments.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//fixes plain text shade
@Mixin(SignRenderer.class)
public abstract class SignRendererMixin {

    @Unique
    private static Float amendments$signYaw;
    @Unique
    private static Boolean amendments$front;

    //screw this

    /**
     * @author MehVahDjukaar
     * @reason adding color normal shading and color modifier
     */
    @Overwrite
    public static int getDarkColor(SignText signText) {
        int color = signText.getColor().getTextColor();
        if (color == DyeColor.BLACK.getTextColor() && signText.hasGlowingText()) {
            return -988212;
        } else {
            float scale = (0.4f * ClientConfigs.getSignColorMult());
            if (amendments$front != null && amendments$signYaw != null) {
                Vector3f normal = new Vector3f(0, 0, 1);
                normal.rotateY(amendments$signYaw * Mth.DEG_TO_RAD * (amendments$front ? 1 : -1));
                amendments$front = null;
                scale *= ColorUtils.getShading(normal);
            }
            return ColorUtils.multiply(color, scale);
        }
    }


    @Inject(method = "translateSign", at = @At("HEAD"))
    private void captureYaw(PoseStack poseStack, float yaw, BlockState blockState, CallbackInfo ci) {
        amendments$signYaw = yaw;
    }

    @Inject(method = "renderSignText", at = @At("HEAD"))
    private void captureFace(BlockPos blockPos, SignText signText, PoseStack poseStack, MultiBufferSource multiBufferSource,
                             int i, int j, int k, boolean face, CallbackInfo ci) {
        amendments$front = face;
    }

    @Inject(method = "renderSignWithText", at = @At("TAIL"))
    private void resetYaw(SignBlockEntity signBlockEntity, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, BlockState blockState, SignBlock signBlock, WoodType woodType, Model model, CallbackInfo ci) {
        amendments$signYaw = null;
    }
}
