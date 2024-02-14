package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.item.IFirstPersonAnimationProvider;
import net.mehvahdjukaar.moonlight.api.item.IThirdPersonAnimationProvider;
import net.mehvahdjukaar.moonlight.api.item.IThirdPersonSpecialItemRenderer;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class LanternRendererExtension implements IThirdPersonAnimationProvider, IThirdPersonSpecialItemRenderer, IFirstPersonAnimationProvider {

    @Override
    public <T extends LivingEntity> boolean poseRightArm(ItemStack itemStack, HumanoidModel<T> model, T t, HumanoidArm arm) {
        if (!ClientConfigs.LANTERN_HOLDING.get()) return false;
        //model.rightArm.yRot = Mth.clamp(MthUtils.wrapRad(-0F + model.head.yRot), -0.5f, 1);
        model.rightArm.xRot = Mth.clamp(MthUtils.wrapRad(-1.2f + model.head.xRot), -2, -0.5f);
        return true;
    }

    @Override
    public <T extends LivingEntity> boolean poseLeftArm(ItemStack itemStack, HumanoidModel<T> model, T t, HumanoidArm arm) {
        if (!ClientConfigs.LANTERN_HOLDING.get()) return false;
        model.leftArm.xRot = Mth.clamp(MthUtils.wrapRad(-1.2f + model.head.xRot), -2, -0.5f);
        return false;
    }

    @Override
    public boolean renderFirstPersonItem(LivingEntity entity, ItemStack stack, InteractionHand hand, PoseStack poseStack,
                                         float partialTicks, MultiBufferSource buffer, int light) {
        if (entity instanceof LocalPlayer player) {
            boolean bl = hand == InteractionHand.MAIN_HAND;
            HumanoidArm side = bl ? player.getMainArm() : player.getMainArm().getOpposite();
            float f = side == HumanoidArm.RIGHT ? 1.0F : -1.0F;

            var renderer = Minecraft.getInstance().getEntityRenderDispatcher();


            poseStack.translate(f * 0.125F, -0.125F, 0.0F);
            if (!player.isInvisible()) {
                poseStack.pushPose();
                poseStack.mulPose(Axis.ZP.rotationDegrees(f * 10.0F));


                RenderSystem.setShaderTexture(0, player.getSkinTextureLocation());
                PlayerRenderer playerRenderer = (PlayerRenderer) renderer.getRenderer(player);
                poseStack.pushPose();
                poseStack.mulPose(Axis.YP.rotationDegrees(92.0F));
                poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(f * -41.0F));
                poseStack.translate(f * 0.3F, -1.1F, 0.45F);
                if (side == HumanoidArm.RIGHT) {
                    playerRenderer.renderRightHand(poseStack, buffer, light, player);
                } else {
                    playerRenderer.renderLeftHand(poseStack, buffer, light, player);
                }
                poseStack.popPose();


                poseStack.popPose();

            }
            return true;
        }
        return false;
    }

    @Override
    public void animateItemFirstPerson(LivingEntity entity, ItemStack stack,
                                       InteractionHand interactionHand, PoseStack poseStack, float partialTicks, float pitch, float attackAnim, float handHeight) {
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

            poseStack.mulPose(Axis.YP.rotationDegrees(model.rightArm.zRot * Mth.RAD_TO_DEG));
            poseStack.mulPose(Axis.XP.rotationDegrees(-model.rightArm.xRot * Mth.RAD_TO_DEG));

            float scale = (float) (double) ClientConfigs.LANTERN_HOLDING_SIZE.get();

            poseStack.scale(scale, scale, scale);
            poseStack.mulPose(RotHlpr.X180);
            poseStack.translate(-0.5, -0.5, -0.5);
            poseStack.translate(0, -3 / 16f, 0);

            BlockState state = Blocks.LANTERN.defaultBlockState();
            RenderUtil.renderBlock(0, poseStack, bufferSource, state, entity.level(),
                    BlockPos.ZERO, Minecraft.getInstance().getBlockRenderer());

            ItemDisplayContext transform = leftHand ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
            //Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer().renderItem(entity, stack, transform,
            //        leftHand, poseStack, bufferSource, light);

            poseStack.popPose();
        }
    }
}

