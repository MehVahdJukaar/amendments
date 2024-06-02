package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.item.IThirdPersonAnimationProvider;
import net.mehvahdjukaar.moonlight.api.item.IThirdPersonSpecialItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

public class CandleHolderRendererExtension implements IThirdPersonAnimationProvider, IThirdPersonSpecialItemRenderer {

    @Override
    public <T extends LivingEntity> boolean poseRightArm(ItemStack itemStack, HumanoidModel<T> model, T t, HumanoidArm arm) {
        //model.rightArm.yRot = Mth.clamp(MthUtils.wrapRad(0F + model.head.yRot), -0.5f, 1);
        // model.rightArm.xRot = Mth.clamp(MthUtils.wrapRad(-1.4f + model.head.xRot), -2.4f, -0.2f);
        model.rightArm.xRot = (float) -Math.toRadians(20+20);
        return true;
    }

    @Override
    public <T extends LivingEntity> boolean poseLeftArm(ItemStack itemStack, HumanoidModel<T> model, T t, HumanoidArm arm) {
        //model.leftArm.yRot = Mth.clamp(MthUtils.wrapRad(0F + model.head.yRot), -1f, 0.5);
        //model.leftArm.xRot = Mth.clamp(MthUtils.wrapRad(-1.4f + model.head.xRot), -2.4f, -0.2f);
        model.leftArm.xRot = (float) -Math.toRadians(20+20);
        return true;
    }


    @Override
    public <T extends Player, M extends EntityModel<T> & ArmedModel & HeadedModel> void renderThirdPersonItem(
            M parentModel, LivingEntity entity, ItemStack stack, HumanoidArm humanoidArm,
            PoseStack poseStack, MultiBufferSource bufferSource, int light) {

        if (!stack.isEmpty()) {
            // same as renderHandWithItem.

            // This could have been replaced by a model replace using block model for hand

            //basically just translates to hand
            poseStack.pushPose();
            parentModel.translateToHand(humanoidArm, poseStack);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            boolean left = humanoidArm == HumanoidArm.LEFT;
            poseStack.translate((float) (left ? -1 : 1) / 16.0F, 0.125F, -0.625F);



            poseStack.scale(2, 2, 2);

            //same as thin item model
            ItemTransform transform = new ItemTransform(
                    new Vector3f(75 - 20, -180, 0),
                    new Vector3f(0, 2.5f - 0.5f, 2f - 0.75f).mul(1 / 16f),
                    new Vector3f(0.375f, 0.375f, 0.375f));


            transform.apply(left, poseStack);

            renderLanternModel(entity, stack, poseStack, bufferSource, light, left);
            poseStack.popPose();

            //FlameParticle f = new FlameParticle(entity.level(), entity.getX(), entity.getY(), entity.getZ(), 0, 0, 0);
        }
    }

    private static void renderLanternModel(LivingEntity entity, ItemStack itemStack, PoseStack poseStack,
                                           MultiBufferSource buffer, int light, boolean left) {
        Minecraft mc = Minecraft.getInstance();
        ItemRenderer itemRenderer = mc.getItemRenderer();
        BlockState state = ((BlockItem) itemStack.getItem()).getBlock().defaultBlockState();

        var model = mc.getBlockRenderer().getBlockModel(state);

        itemRenderer.render(itemStack, ItemDisplayContext.NONE, left, poseStack,
                buffer, light, OverlayTexture.NO_OVERLAY, model);
    }


}

