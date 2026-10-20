package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.amendments.integration.SuppCompat;
import net.mehvahdjukaar.moonlight.api.item.IFirstPersonSpecialItemRenderer;
import net.mehvahdjukaar.moonlight.api.item.IThirdPersonAnimationProvider;
import net.mehvahdjukaar.moonlight.api.item.IThirdPersonSpecialItemRenderer;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class CandleHolderRendererExtension implements IThirdPersonAnimationProvider, IThirdPersonSpecialItemRenderer,
        IFirstPersonSpecialItemRenderer {

    private final BlockState offState;
    private final BlockState litState;
    private final Vec3 candleParticleOffset;
    private final ResourceLocation flameTexture;

    public CandleHolderRendererExtension(BlockItem blockItem) {
        offState = blockItem.getBlock().defaultBlockState();
        litState = offState.setValue(CandleBlock.LIT, true);
        candleParticleOffset = SuppCompat.getCandleHolderParticleOffset(offState);
        flameTexture = HeldItemRenderUtils.getFlameTexture(Utils.getID(blockItem));
    }

    @Override
    public <T extends LivingEntity> boolean poseRightArm(ItemStack itemStack, HumanoidModel<T> model, T t, HumanoidArm arm) {
        // model.rightArm.yRot = Mth.clamp(MthUtils.wrapRad(0F + model.head.yRot), -0.5f, 1);
        // model.rightArm.xRot = Mth.clamp(MthUtils.wrapRad(-1.4f + model.head.xRot), -2.4f, -0.2f);
        model.rightArm.xRot = (float) -Math.toRadians(20 + 20);
        return true;
    }

    @Override
    public <T extends LivingEntity> boolean poseLeftArm(ItemStack itemStack, HumanoidModel<T> model, T t, HumanoidArm arm) {
        // model.leftArm.yRot = Mth.clamp(MthUtils.wrapRad(0F + model.head.yRot), -1f, 0.5);
        // model.leftArm.xRot = Mth.clamp(MthUtils.wrapRad(-1.4f + model.head.xRot), -2.4f, -0.2f);
        model.leftArm.xRot = (float) -Math.toRadians(20 + 20);
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

            HeldItemRenderUtils.renderBlockModelAsItem(stack, getHeldState(entity), poseStack, bufferSource, light, left);

            if (!entity.isInWater()) {
                HeldItemRenderUtils.renderFlame(entity, poseStack, bufferSource, flameTexture, candleParticleOffset,
                        2 / 16f, HeldItemRenderUtils.getCameraBillboardRotation());
            }

            poseStack.popPose();
        }
    }

    private BlockState getHeldState(LivingEntity entity) {
        return entity.isInWater() ? offState : litState;
    }

    @Override
    public boolean renderFirstPersonItem(AbstractClientPlayer player, ItemStack stack, InteractionHand interactionHand, HumanoidArm arm,
                                         PoseStack poseStack, float partialTicks, float pitch, float attackAnim, float equipAnim,
                                         MultiBufferSource buffer, int light, ItemInHandRenderer renderer) {

        boolean left = arm == HumanoidArm.LEFT;

        poseStack.pushPose();

        //this should have been a special item renderer... if we dont render arm or item in weird places
        HeldItemRenderUtils.applyFirstPersonHandTransform(poseStack, renderer, arm, attackAnim, equipAnim);

        poseStack.translate(0, 0.3125, 0);

        float scale = (float) (double) ClientConfigs.CANDLE_HOLDING_SIZE.get();

        poseStack.scale(-scale, scale, -scale);

        HeldItemRenderUtils.renderBlockModelAsItem(stack, getHeldState(player), poseStack, buffer, light, left);

        if (!player.isInWater()) {
            poseStack.translate((left ? -1 : 1) * 0.03, 0, -0.04f);
            HeldItemRenderUtils.renderFlame(player, poseStack, buffer, flameTexture, candleParticleOffset,
                    2 / 16f, HeldItemRenderUtils.getHandBillboardRotation());
        }

        poseStack.popPose();
        return true;
    }
}
