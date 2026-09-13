package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.amendments.integration.SuppCompat;
import net.mehvahdjukaar.moonlight.api.item.IFirstPersonSpecialItemRenderer;
import net.mehvahdjukaar.moonlight.api.item.IThirdPersonAnimationProvider;
import net.mehvahdjukaar.moonlight.api.item.IThirdPersonSpecialItemRenderer;
import net.mehvahdjukaar.moonlight.api.util.Utils;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class CandleHolderRendererExtension implements IThirdPersonAnimationProvider, IThirdPersonSpecialItemRenderer,
        IFirstPersonSpecialItemRenderer {

    private static final Identifier FLAME_TEXTURE =
            Identifier.withDefaultNamespace("textures/particle/flame.png");
    private static final Identifier SOUL_FLAME_TEXTURE =
            Identifier.withDefaultNamespace("textures/particle/soul_fire_flame.png");
    private static final Identifier ENDER_FLAME_TEXTURE =
            Identifier.fromNamespaceAndPath("endergetic",
                    "textures/particle/ender_fire_flame.png");
    private static final Identifier CUPRIC_FLAME_TEXTURE =
            Identifier.fromNamespaceAndPath("caverns_and_chasms",
                    "textures/particle/cupric_fire_flame.png");
    private static final Identifier REDSTONE_FLAME_TEXTURE =
            Identifier.withDefaultNamespace("textures/particle/generic_6.png");

    private final BlockState offState;
    private final BlockState litState;
    private final Vec3 candleParticleOffset;
    private final Identifier flameTexture;

    public CandleHolderRendererExtension(BlockItem blockItem) {
        offState = blockItem.getBlock().defaultBlockState();
        litState = offState.setValue(CandleBlock.LIT, true);
        candleParticleOffset = SuppCompat.getCandleHolderParticleOffset(offState);

        Identifier id = Utils.getID(blockItem);
        String path = id.getPath();
        if (path.endsWith("soul") || path.startsWith("soul")) {
            this.flameTexture = SOUL_FLAME_TEXTURE;
        } else if (path.endsWith("ender") || path.startsWith("ender")) {
            this.flameTexture = ENDER_FLAME_TEXTURE;
        } else if (path.endsWith("cupric") || path.startsWith("cupric")) {
            this.flameTexture = CUPRIC_FLAME_TEXTURE;
        } else if (path.endsWith("redstone") || path.startsWith("redstone")) {
            this.flameTexture = REDSTONE_FLAME_TEXTURE;
        } else {
            this.flameTexture = FLAME_TEXTURE;
        }
    }

    @Override
    public Result poseArm(ItemStack stack, HumanoidModel<?> model, HumanoidRenderState state, HumanoidArm arm) {
        if (arm == HumanoidArm.RIGHT) {
            model.rightArm.xRot = (float) -Math.toRadians(20 + 20);
        } else {
            model.leftArm.xRot = (float) -Math.toRadians(20 + 20);
        }
        return Result.HANDLED;
    }

    @Override
    public void renderThirdPersonItem(HumanoidModel<AvatarRenderState> parentModel, AvatarRenderState state, ItemStack stack,
                                      HumanoidArm arm, PoseStack poseStack, SubmitNodeCollector collector, int light) {
        if (stack.isEmpty()) return;

        // same as renderHandWithItem.
        // This could have been replaced by a model replace using block model for hand

        //basically just translates to hand
        poseStack.pushPose();
        parentModel.translateToHand(state, arm, poseStack);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        boolean left = arm == HumanoidArm.LEFT;
        poseStack.translate((float) (left ? -1 : 1) / 16.0F, 0.125F, -0.625F);

        poseStack.scale(2, 2, 2);

        //same as thin item model
        ItemTransform transform = new ItemTransform(
                new Vector3f(75 - 20, -180, 0),
                new Vector3f(0, 2.5f - 0.5f, 2f - 0.75f).mul(1 / 16f),
                new Vector3f(0.375f, 0.375f, 0.375f));

        transform.apply(left, poseStack.last());

        HeldBlockModel.submit(state.isInWater ? offState : litState, poseStack, collector, light);

        if (!state.isInWater) {
            HeldFlame.submit(poseStack, collector, this.flameTexture, candleParticleOffset, 2, state.ageInTicks);
        }

        poseStack.popPose();
    }

    @Override
    public boolean renderFirstPersonItem(AbstractClientPlayer player, ItemStack stack, InteractionHand interactionHand, HumanoidArm arm,
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

        poseStack.translate(0, 0.3125, 0);

        float scale = (float) (double) ClientConfigs.CANDLE_HOLDING_SIZE.get();

        poseStack.scale(-scale, scale, -scale);

        boolean inWater = player.isInWater();
        HeldBlockModel.submit(inWater ? offState : litState, poseStack, collector, light);

        if (!inWater) {
            poseStack.translate(f * 0.03, 0, -0.04f);
            HeldFlame.submit(poseStack, collector, this.flameTexture, candleParticleOffset, 2,
                    player.tickCount + partialTicks);
        }

        poseStack.popPose();
        return true;
    }
}
