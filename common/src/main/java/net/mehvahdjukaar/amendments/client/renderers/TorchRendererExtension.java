package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.amendments.integration.CompatObjects;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.api.item.IFirstPersonSpecialItemRenderer;
import net.mehvahdjukaar.moonlight.api.item.IThirdPersonAnimationProvider;
import net.mehvahdjukaar.moonlight.api.item.IThirdPersonSpecialItemRenderer;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class TorchRendererExtension implements IThirdPersonAnimationProvider, IThirdPersonSpecialItemRenderer, IFirstPersonSpecialItemRenderer {

    private static final Vec3 TORCH_PARTICLE_OFFSET = new Vec3(0, 0.16, 0);

    private final BlockState heldState;
    private final ResourceLocation flameTexture;

    public TorchRendererExtension(Item item) {
        Item modelItem = item == CompatObjects.SCONCE_LEVER.get() ? CompatObjects.SCONCE.get() : item;
        BlockState state = ((BlockItem) modelItem).getBlock().defaultBlockState();
        if (state.hasProperty(BlockStateProperties.LIT)) {
            state = state.setValue(BlockStateProperties.LIT, true);
        }
        this.heldState = state;

        this.flameTexture = HeldItemRenderUtils.getFlameTexture(Utils.getID(item));
    }

    @Override
    public <T extends LivingEntity> boolean poseRightArm(ItemStack itemStack, HumanoidModel<T> model, T t, HumanoidArm arm) {
        //model.rightArm.yRot = Mth.clamp(MthUtils.wrapRad(0F + model.head.yRot), -0.5f, 1);
        if (ClientConfigs.HOLDING_ANIMATION_FIXED.get()) {
            model.rightArm.xRot = -1.3f;
        } else {
            model.rightArm.xRot = Mth.clamp(MthUtils.wrapRad(-1.4f + model.head.xRot), -2.4f, -0.2f);
        }
        return true;
    }

    @Override
    public <T extends LivingEntity> boolean poseLeftArm(ItemStack itemStack, HumanoidModel<T> model, T t, HumanoidArm arm) {
        //model.leftArm.yRot = Mth.clamp(MthUtils.wrapRad(0F + model.head.yRot), -1f, 0.5);
        if (ClientConfigs.HOLDING_ANIMATION_FIXED.get()) {
            model.leftArm.xRot = -1.3f;
        } else {
            model.leftArm.xRot = Mth.clamp(MthUtils.wrapRad(-1.4f + model.head.xRot), -2.4f, -0.2f);
        }
        return true;
    }


    @Override
    public <T extends Player, M extends EntityModel<T> & ArmedModel & HeadedModel> void renderThirdPersonItem(
            M parentModel, LivingEntity entity, ItemStack stack, HumanoidArm humanoidArm,
            PoseStack poseStack, MultiBufferSource bufferSource, int light) {

        if (!stack.isEmpty()) {
            // same as renderHandWithItem.

            // This could have been replaced by a model replace using block model for hand
            poseStack.pushPose();
            parentModel.translateToHand(humanoidArm, poseStack);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            boolean left = humanoidArm == HumanoidArm.LEFT;
            poseStack.translate((float) (left ? -1 : 1) / 16.0F, 0.125F, -0.625F);

            poseStack.translate(0, 3 / 16f, 2 / 16f);

            renderTorchModel(stack, poseStack, bufferSource, light, left);

            if (hasFlame(entity)) {
                renderFlame(entity, poseStack, bufferSource, HeldItemRenderUtils.getCameraBillboardRotation());
            }

            poseStack.popPose();
        }
    }

    private void renderTorchModel(ItemStack itemStack, PoseStack poseStack,
                                  MultiBufferSource buffer, int light, boolean left) {
        HeldItemRenderUtils.renderBlockModelAsItem(itemStack, heldState, poseStack, buffer, light, left);
    }

    protected boolean hasFlame(LivingEntity entity) {
        return ClientConfigs.TORCH_HOLDING_FLAME.get() && !entity.isInWater();
    }

    protected void renderFlame(LivingEntity entity, PoseStack poseStack, MultiBufferSource buffer,
                               Quaternionf billboardRot) {
        HeldItemRenderUtils.renderFlame(entity, poseStack, buffer, flameTexture,
                TORCH_PARTICLE_OFFSET, 4 / 16f, billboardRot);
    }


    @Override
    public boolean renderFirstPersonItem(AbstractClientPlayer player, ItemStack stack, InteractionHand hand, HumanoidArm arm,
                                         PoseStack poseStack, float partialTicks, float pitch, float attackAnim, float equipAnim,
                                         MultiBufferSource buffer, int light, ItemInHandRenderer renderer) {

        boolean left = arm == HumanoidArm.LEFT;
        float f = left ? -1.0F : 1.0F;
        poseStack.pushPose();

        //this should have been a special item renderer... if we dont render arm or item in weird places
        HeldItemRenderUtils.applyFirstPersonHandTransform(poseStack, renderer, arm, attackAnim, equipAnim);


        //same as generated item model
        ItemTransform transform = new ItemTransform(
                new Vector3f(0, -90, 25),
                new Vector3f(0, 2.5f - 0.5f, 2f - 0.75f).mul(1 / 16f),
                new Vector3f(0.68f, 0.68f, 0.68f));
        transform.apply(left, poseStack);

        //IDK why it's not in same position as other 2d items
        poseStack.translate(f * 0.5 / 16f, 1.65 / 16f, -1 / 16f);

        float scale = (float) (double) ClientConfigs.TORCH_HOLDING_SIZE.get();

        poseStack.scale(scale, scale, scale);

        renderTorchModel(stack, poseStack, buffer, light, left);

        if (hasFlame(player)) {
            renderFlame(player, poseStack, buffer, HeldItemRenderUtils.getHandBillboardRotation());
        }

        poseStack.popPose();
        return true;
    }
}
