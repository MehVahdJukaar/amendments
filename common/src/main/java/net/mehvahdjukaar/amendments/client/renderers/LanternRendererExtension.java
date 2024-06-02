package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.amendments.integration.CompatHandler;
import net.mehvahdjukaar.amendments.integration.ThinAirCompat;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.item.IFirstPersonSpecialItemRenderer;
import net.mehvahdjukaar.moonlight.api.item.IThirdPersonAnimationProvider;
import net.mehvahdjukaar.moonlight.api.item.IThirdPersonSpecialItemRenderer;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;

public class LanternRendererExtension implements IThirdPersonAnimationProvider, IThirdPersonSpecialItemRenderer, IFirstPersonSpecialItemRenderer {

    @Override
    public <T extends LivingEntity> boolean poseRightArm(ItemStack itemStack, HumanoidModel<T> model, T t, HumanoidArm arm) {
        if (!ClientConfigs.LANTERN_HOLDING.get()) return false;
        //model.rightArm.yRot = Mth.clamp(MthUtils.wrapRad(0F + model.head.yRot), -0.5f, 1);
        model.rightArm.xRot = Mth.clamp(MthUtils.wrapRad(-1.2f + model.head.xRot), -2.4f, -0.5f);
        return true;
    }

    @Override
    public <T extends LivingEntity> boolean poseLeftArm(ItemStack itemStack, HumanoidModel<T> model, T t, HumanoidArm arm) {
        if (!ClientConfigs.LANTERN_HOLDING.get()) return false;
        //model.leftArm.yRot = Mth.clamp(MthUtils.wrapRad(0F + model.head.yRot), -1f, 0.5);
        model.leftArm.xRot = Mth.clamp(MthUtils.wrapRad(-1.2f + model.head.xRot), -2.4f, -0.5f);
        return true;
    }


    @Override
    public <T extends Player, M extends EntityModel<T> & ArmedModel & HeadedModel> void renderThirdPersonItem(
            M parentModel, LivingEntity entity, ItemStack stack, HumanoidArm humanoidArm,
            PoseStack poseStack, MultiBufferSource bufferSource, int light) {

        if (!stack.isEmpty() && ClientConfigs.LANTERN_HOLDING.get()) {

            poseStack.pushPose();

            boolean leftHand = humanoidArm == HumanoidArm.LEFT;

            //I don't even know why this needs to be here.
            // to offset shoulder joint IDK
            float shoulderOffset = 1 / 32F;
            poseStack.translate(-shoulderOffset, 0, 0);
            //technically translates to shoulder
            parentModel.translateToHand(humanoidArm, poseStack);

            //moveto hand
            poseStack.translate(shoulderOffset + (leftHand ? 1 : -1) / 16.0F, 20 / 32f, 1 / 16f);


            HumanoidModel<?> model = ((HumanoidModel<?>) parentModel);

            if (leftHand) {
                poseStack.mulPose(Axis.YP.rotationDegrees(model.leftArm.zRot * Mth.RAD_TO_DEG));
                poseStack.mulPose(Axis.XP.rotationDegrees(-model.leftArm.xRot * Mth.RAD_TO_DEG));
            } else {
                poseStack.mulPose(Axis.YP.rotationDegrees(model.rightArm.zRot * Mth.RAD_TO_DEG));
                poseStack.mulPose(Axis.XP.rotationDegrees(-model.rightArm.xRot * Mth.RAD_TO_DEG));
            }

            float scale = (float) (double) ClientConfigs.LANTERN_HOLDING_SIZE.get();

            poseStack.scale(scale, scale, scale);
            poseStack.mulPose(RotHlpr.Z180);
            poseStack.translate(0, -3 / 16f, 0);

            renderLanternModel(entity, stack, poseStack, bufferSource, light);

            //ItemDisplayContext transform = leftHand ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
            //Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer().renderItem(entity, stack, transform,
            //        leftHand, poseStack, bufferSource, light);

            poseStack.popPose();
        }
    }

    @Override
    public boolean renderFirstPersonItem(AbstractClientPlayer player, ItemStack itemStack, InteractionHand hand, HumanoidArm arm,
                                         PoseStack poseStack, float partialTicks, float pitch, float attackAnim, float equipAnim,
                                         MultiBufferSource buffer, int light, ItemInHandRenderer renderer) {

        if (!player.isInvisible()) {

            float lanternScale = 16 / 16f;
            float f = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;

            //Quaternionf oldRotation = poseStack.last().pose().getUnnormalizedRotation(new Quaternionf());


            poseStack.pushPose();

            poseStack.translate(-0.025 * f, 0.125, 0);


            poseStack.mulPose(Axis.ZP.rotationDegrees(f * 10.0F));
            //poseStack.mulPose(Axis.ZP.rotationDegrees(20));

            renderer.renderPlayerArm(poseStack, buffer, light, equipAnim, attackAnim, arm);

            //Quaternionf newRotation = poseStack.last().pose().getUnnormalizedRotation(new Quaternionf());


            Quaternionf rotationDiff;// = new Quaternionf(newRotation).conjugate().mul(oldRotation);
            // restore old rotation

            // the two translations aren't the same but are darn close.
            // I tried doing math but failed...
            // this is so bad...

            float t = 0 * (player.tickCount / 40f) % 1;

            poseStack.translate(-0.5 - 0.25 * f, 0.15, -0.463);
            //poseStack.translate(AmendmentsClient.x*f, AmendmentsClient.y, AmendmentsClient.z);
            poseStack.translate(0.066 * f, -0.033, 0.024);


            if (arm == HumanoidArm.RIGHT) {

                rotationDiff = new Quaternionf(2.077E-1, -6.488E-1, 4.433E-1, 5.825E-1);
                poseStack.translate(0.5, 0.5, 0.5);
                poseStack.mulPose(rotationDiff);
                poseStack.translate(-0.5, -0.5, -0.5);

            } else {

                rotationDiff = new Quaternionf(2.077E-1, 6.488E-1, -4.433E-1, 5.825E-1);
                poseStack.translate(0.5, 0.5, 0.5);
                poseStack.mulPose(rotationDiff);
                poseStack.translate(-0.5, -0.5, -0.5);
            }

            poseStack.scale(lanternScale, lanternScale, lanternScale);

            poseStack.translate(0.5, 0.5, 0.5);
            renderLanternModel(player, itemStack, poseStack, buffer, light);

            poseStack.popPose();

            /*

            // exactly the same as in renderPlayerArm. tbh we could have merged them

            // equip anim
            poseStack.translate(f * 0.64000005F, 0.5 - 0.6F + equipAnim * -0.6F, -0.71999997F);

            // attack anim

            {

                float attackSqrt = Mth.sqrt(attackAnim);

                float attackSin = -0.3F * Mth.sin(attackSqrt * Mth.PI);
                float dx = -0.5F * attackSin;
                float dy = 0.4F * Mth.sin(attackSqrt * Mth.TWO_PI);
                float dz = -0.3F * Mth.sin(attackAnim * Mth.PI);
                poseStack.translate(f * dx, dy - 0.3F * attackSin, dz);
                poseStack.mulPose(Axis.XP.rotationDegrees(attackSin * -45.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(f * attackSin * -30.0F));
            }

            poseStack.scale(lanternScale, lanternScale, lanternScale);

            // bs translation
            poseStack.translate(-0.5F + 0.15 * f, -0.9F, -0.9);

            itemStack = Items.SOUL_LANTERN.getDefaultInstance();

            renderLanternModel(itemStack, poseStack, buffer, light);

            */

            return true;
        }
        return false;
    }

    private static void renderLanternModel(LivingEntity entity, ItemStack itemStack, PoseStack poseStack, MultiBufferSource buffer, int light) {
        Minecraft mc = Minecraft.getInstance();
        ItemRenderer itemRenderer = mc.getItemRenderer();
        BlockState state = ((BlockItem) itemStack.getItem()).getBlock().defaultBlockState();
        if (CompatHandler.THIN_AIR) {
            var newState = ThinAirCompat.maybeSetAirQuality(state, entity.getEyePosition(), entity.level());
            if (newState != null) {
                state = newState;
            }
        }
        if (state.hasProperty(LanternBlock.HANGING)) state = state.setValue(LanternBlock.HANGING, false);
        var model = mc.getBlockRenderer().getBlockModel(state);
        itemRenderer.render(itemStack, ItemDisplayContext.NONE, false, poseStack,
                buffer, light, OverlayTexture.NO_OVERLAY, model);

        //  RenderUtil.renderBlock(0, poseStack, buffer, state, player.level(),
        //        BlockPos.ZERO, Minecraft.getInstance().getBlockRenderer());
    }


}

