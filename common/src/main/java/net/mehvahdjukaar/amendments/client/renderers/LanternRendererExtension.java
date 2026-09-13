package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.item.IFirstPersonSpecialItemRenderer;
import net.mehvahdjukaar.moonlight.api.item.IThirdPersonAnimationProvider;
import net.mehvahdjukaar.moonlight.api.item.IThirdPersonSpecialItemRenderer;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;

//import net.mehvahdjukaar.amendments.integration.ThinAirCompat;

public class LanternRendererExtension implements IThirdPersonAnimationProvider, IThirdPersonSpecialItemRenderer, IFirstPersonSpecialItemRenderer {

    @Override
    public Result poseArm(ItemStack stack, HumanoidModel<?> model, HumanoidRenderState state, HumanoidArm humanoidArm) {
        var arm = humanoidArm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;
        boolean up = ClientConfigs.LANTERN_HOLDING_UP.get();
        if (ClientConfigs.HOLDING_ANIMATION_FIXED.get()) {
            arm.xRot = up ? -1.9f : (humanoidArm == HumanoidArm.RIGHT ? -1 : 1);
        } else {
            float v = up ? -1.9f : -1.2f;
            arm.xRot = Mth.clamp(MthUtils.wrapRad(v + model.head.xRot), -2.4f, -0.5f);
        }
        return Result.HANDLED;
    }

    @Override
    public void renderThirdPersonItem(HumanoidModel<AvatarRenderState> parentModel, AvatarRenderState state, ItemStack stack,
                                      HumanoidArm arm, PoseStack poseStack, SubmitNodeCollector collector, int light) {
        if (stack.isEmpty()) return;

        poseStack.pushPose();

        boolean left = arm == HumanoidArm.LEFT;

        //I don't even know why this needs to be here.
        // to offset shoulder joint IDK
        float shoulderOffset = 1 / 32F;
        poseStack.translate(-shoulderOffset, 0, 0);
        //technically translates to shoulder
        parentModel.translateToHand(state, arm, poseStack);

        //moveto hand
        poseStack.translate(shoulderOffset + (left ? 1 : -1) / 16.0F, 20 / 32f, 1 / 16f);

        if (left) {
            poseStack.mulPose(Axis.YP.rotationDegrees(parentModel.leftArm.zRot * Mth.RAD_TO_DEG));
            poseStack.mulPose(Axis.XP.rotationDegrees(-parentModel.leftArm.xRot * Mth.RAD_TO_DEG));
        } else {
            poseStack.mulPose(Axis.YP.rotationDegrees(parentModel.rightArm.zRot * Mth.RAD_TO_DEG));
            poseStack.mulPose(Axis.XP.rotationDegrees(-parentModel.rightArm.xRot * Mth.RAD_TO_DEG));
        }

        float scale = (float) (double) ClientConfigs.LANTERN_HOLDING_SIZE.get();

        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(RotHlpr.Z180);
        poseStack.translate(0, -3 / 16f, 0);

        HeldBlockModel.submit(lanternState(stack), poseStack, collector, light);

        poseStack.popPose();
    }

    @Override
    public boolean renderFirstPersonItem(AbstractClientPlayer player, ItemStack itemStack, InteractionHand hand, HumanoidArm arm,
                                         PoseStack poseStack, float partialTicks, float pitch, float attackAnim, float equipAnim,
                                         SubmitNodeCollector collector, int light, ItemInHandRenderer renderer) {

        float lanternScale = 16 / 16f;
        boolean left = arm == HumanoidArm.LEFT;
        float f = left ? -1.0F : 1.0F;

        poseStack.pushPose();

        poseStack.translate(-0.025 * f, 0.125, 0);

        poseStack.mulPose(Axis.ZP.rotationDegrees(f * 10.0F));

        renderer.renderPlayerArm(poseStack, collector, light, equipAnim, attackAnim, arm);

        // the two translations aren't the same but are darn close.
        // I tried doing math but failed...
        // this is so bad...
        poseStack.translate(-0.5 - 0.25 * f, 0.15, -0.463);
        poseStack.translate(0.066 * f, -0.033, 0.024);

        Quaternionf rotationDiff;
        if (left) {
            rotationDiff = new Quaternionf(2.077E-1, 6.488E-1, -4.433E-1, 5.825E-1);
        } else {
            rotationDiff = new Quaternionf(2.077E-1, -6.488E-1, 4.433E-1, 5.825E-1);
        }

        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(rotationDiff);
        poseStack.translate(-0.5, -0.5, -0.5);

        poseStack.scale(lanternScale, lanternScale, lanternScale);

        poseStack.translate(0.5, 0.5, 0.5);
        HeldBlockModel.submit(lanternState(itemStack), poseStack, collector, light);

        poseStack.popPose();
        return true;
    }

    private static BlockState lanternState(ItemStack itemStack) {
        BlockState state = ((BlockItem) itemStack.getItem()).getBlock().defaultBlockState();
        if (state.hasProperty(LanternBlock.HANGING)) state = state.setValue(LanternBlock.HANGING, false);
        //if (CompatHandler.THIN_AIR) {
        //    var newState = ThinAirCompat.maybeSetAirQuality(state, entity.getEyePosition(), entity.level());
        //    if (newState != null) {
        //        state = newState;
        //    }
        //}
        return state;
    }
}
