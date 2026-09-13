package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.amendments.integration.CompatObjects;
import net.mehvahdjukaar.moonlight.api.item.IFirstPersonSpecialItemRenderer;
import net.mehvahdjukaar.moonlight.api.item.IThirdPersonAnimationProvider;
import net.mehvahdjukaar.moonlight.api.item.IThirdPersonSpecialItemRenderer;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class TorchRendererExtension implements IThirdPersonAnimationProvider, IThirdPersonSpecialItemRenderer, IFirstPersonSpecialItemRenderer {

    private static final Identifier FLAME_TEXTURE =
            Identifier.withDefaultNamespace("textures/particle/flame.png");
    private static final Identifier SOUL_FLAME_TEXTURE =
            Identifier.withDefaultNamespace("textures/particle/soul_fire_flame.png");
    private static final Identifier REDSTONE_FLAME_TEXTURE =
            Identifier.withDefaultNamespace("textures/particle/generic_6.png");

    //top-center of the torch model, in centered block coords (tweak Y if the flame sits off)
    private static final Vec3 TORCH_PARTICLE_OFFSET = new Vec3(0, 0.16, 0);

    @Override
    public Result poseArm(ItemStack stack, HumanoidModel<?> model, HumanoidRenderState state, HumanoidArm humanoidArm) {
        var arm = humanoidArm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;
        if (ClientConfigs.HOLDING_ANIMATION_FIXED.get()) {
            arm.xRot = -1.3f;
        } else {
            arm.xRot = Mth.clamp(MthUtils.wrapRad(-1.4f + model.head.xRot), -2.4f, -0.2f);
        }
        return Result.HANDLED;
    }

    @Override
    public void renderThirdPersonItem(HumanoidModel<AvatarRenderState> parentModel, AvatarRenderState state, ItemStack stack,
                                      HumanoidArm arm, PoseStack poseStack, SubmitNodeCollector collector, int light) {
        if (stack.isEmpty()) return;

        // same as renderHandWithItem.
        // This could have been replaced by a model replace using block model for hand
        poseStack.pushPose();
        parentModel.translateToHand(state, arm, poseStack);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        boolean left = arm == HumanoidArm.LEFT;
        poseStack.translate((float) (left ? -1 : 1) / 16.0F, 0.125F, -0.625F);

        poseStack.translate(0, 3 / 16f, 2 / 16f);

        HeldBlockModel.submit(torchState(stack), poseStack, collector, light);

        if (ClientConfigs.TORCH_HOLDING_FLAME.get() && !state.isInWater) {
            HeldFlame.submit(poseStack, collector, getFlameTexture(stack), TORCH_PARTICLE_OFFSET, 4, state.ageInTicks);
        }

        poseStack.popPose();
    }

    @Override
    public boolean renderFirstPersonItem(AbstractClientPlayer player, ItemStack stack, InteractionHand hand, HumanoidArm arm,
                                         PoseStack poseStack, float partialTicks, float pitch, float attackAnim, float equipAnim,
                                         SubmitNodeCollector collector, int light, ItemInHandRenderer renderer) {

        boolean left = arm == HumanoidArm.LEFT;
        float f = left ? -1.0F : 1.0F;
        poseStack.pushPose();

        //this should have been a special item renderer... if we dont render arm or item in weird places

        float n = -0.4F * Mth.sin(Mth.sqrt(attackAnim) * 3.1415927F);
        float m = 0.2F * Mth.sin(Mth.sqrt(attackAnim) * 6.2831855F);
        float h = -0.2F * Mth.sin(attackAnim * 3.1415927F);

        poseStack.translate(f * n, m, h);
        renderer.applyItemArmTransform(poseStack, arm, equipAnim);
        renderer.applyItemArmAttackTransform(poseStack, arm, attackAnim);

        //same as generated item model
        ItemTransform transform = new ItemTransform(
                new Vector3f(0, -90, 25),
                new Vector3f(0, 2.5f - 0.5f, 2f - 0.75f).mul(1 / 16f),
                new Vector3f(0.68f, 0.68f, 0.68f));
        transform.apply(left, poseStack.last());

        //IDK why it's not in same position as other 2d items
        poseStack.translate(f * 0.5 / 16f, 1.65 / 16f, -1 / 16f);

        float scale = (float) (double) ClientConfigs.TORCH_HOLDING_SIZE.get();

        poseStack.scale(scale, scale, scale);

        HeldBlockModel.submit(torchState(stack), poseStack, collector, light);

        if (ClientConfigs.TORCH_HOLDING_FLAME.get() && !player.isInWater()) {
            HeldFlame.submit(poseStack, collector, getFlameTexture(stack), TORCH_PARTICLE_OFFSET, 4,
                    player.tickCount + partialTicks);
        }

        poseStack.popPose();
        return true;
    }

    private static BlockState torchState(ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (item == CompatObjects.SCONCE_LEVER.get()) {
            item = CompatObjects.SCONCE.get();
        }
        return ((BlockItem) item).getBlock().defaultBlockState();
    }

    private static Identifier getFlameTexture(ItemStack stack) {
        String path = Utils.getID(stack.getItem()).getPath();
        if (path.contains("soul")) return SOUL_FLAME_TEXTURE;
        if (path.contains("redstone")) return REDSTONE_FLAME_TEXTURE;
        return FLAME_TEXTURE;
    }
}
