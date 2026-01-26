package net.mehvahdjukaar.amendments.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.amendments.client.renderers.SignRendererExtension;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;
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
    @Unique
    private static boolean amendments$rendersPixelConsistent;
    //screw this
    //if you find an incompatibility pls report

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
    private void amendments$captureYaw(PoseStack poseStack, float yaw, BlockState blockState, CallbackInfo ci) {
        amendments$signYaw = yaw;
    }

    @Inject(method = "renderSignText", at = @At("HEAD"))
    private void amendments$captureFace(BlockPos blockPos, SignText signText, PoseStack poseStack, MultiBufferSource multiBufferSource,
                                        int i, int j, int k, boolean face, CallbackInfo ci) {
        amendments$front = face;
    }

    @Inject(method = "renderSignWithText", at = @At("TAIL"))
    private void amendments$resetYaw(SignBlockEntity signBlockEntity, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, BlockState blockState, SignBlock signBlock, WoodType woodType, Model model, CallbackInfo ci) {
        amendments$signYaw = null;
    }

    @Inject(method = "render(Lnet/minecraft/world/level/block/entity/SignBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At("RETURN"))
    private void amendments$resetPixelConsistent(SignBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, CallbackInfo ci) {
        amendments$rendersPixelConsistent = false;
    }


    @Inject(method = "render(Lnet/minecraft/world/level/block/entity/SignBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V", at = @At("HEAD"), cancellable = true)
    private void amendments$setPixelConsistent(SignBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, CallbackInfo ci) {
        amendments$rendersPixelConsistent = ClientConfigs.isPixelConsistentSign(blockEntity.getBlockState());
        if (amendments$rendersPixelConsistent) {
            SignText front = blockEntity.getFrontText();
            SignText back = blockEntity.getBackText();
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && !front.hasMessage(player) && !back.hasMessage(player)) {
                ci.cancel();
            }
        }
    }


    @Inject(method = "renderSignModel", at = @At("HEAD"), cancellable = true)
    private void amendments$renderSignModel(PoseStack poseStack, int packedLight, int packedOverlay, Model model, VertexConsumer vertexConsumer, CallbackInfo ci) {
        if (amendments$rendersPixelConsistent) {
            ci.cancel();
        }
    }

    @Unique
    private static final Vec3 OLD_OFFSET = new Vec3(0.0, 0.3333333432674408, 0.046666666865348816);

    @ModifyReturnValue(method = "getTextOffset", at = @At("RETURN"))
    private Vec3 amendments$signTextOffset(Vec3 scale) {
        if (amendments$rendersPixelConsistent && scale.equals(OLD_OFFSET)) {
            return SignRendererExtension.TEXT_OFFSET;
        }
        return scale;
    }

    //yes all this below isnt even used anymore since we dont render the model anymore. TODO: delete?

    /*
    @Inject(method = "createSignLayer", at = @At("HEAD"), cancellable = true)
    private static void amendments$makePixelConsistentModel(CallbackInfoReturnable<LayerDefinition> cir) {
        if (ClientConfigs.PIXEL_CONSISTENT_SIGNS.get()) {
            MeshDefinition meshDefinition = new MeshDefinition();
            PartDefinition partDefinition = meshDefinition.getRoot();
            partDefinition.addOrReplaceChild("sign", CubeListBuilder.create()
                    .texOffs(0, 0)
                    .addBox(-8, -16 + 12, -1.0F, 16.0F, 9, 2.0F), PartPose.ZERO);
            partDefinition.addOrReplaceChild("stick", CubeListBuilder.create()
                    .texOffs(0, 14)
                    .addBox(-1.0F, -7.0F + 12, -1.0F, 2.0F, 7, 2.0F), PartPose.ZERO);
            cir.setReturnValue(LayerDefinition.create(meshDefinition, 64, 32));
        }
    }*/

    @ModifyReturnValue(method = "getSignModelRenderScale", at = @At("RETURN"))
    private float amendments$signScale(float scale) {
        if (amendments$rendersPixelConsistent && scale == 0.6666667F) {
            return 1;
        }
        return scale;
    }

    //actually still needed
    //TODO: wall signs have a weird y scale, fix that somehow
    @Inject(method = "translateSign",
            at = @At(value = "TAIL"))
    private void amendments$signTranslate(PoseStack poseStack, float yRot, BlockState state, CallbackInfo ci) {
        if (amendments$rendersPixelConsistent && !(state.getBlock() instanceof StandingSignBlock)) {
            SignRendererExtension.translateWall(poseStack);
        }
    }

}
