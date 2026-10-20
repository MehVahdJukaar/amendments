package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class HeldItemRenderUtils {

    private static final ResourceLocation FLAME_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/particle/flame.png");
    private static final ResourceLocation SOUL_FLAME_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/particle/soul_fire_flame.png");
    private static final ResourceLocation ENDER_FLAME_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("endergetic",
                    "textures/particle/ender_fire_flame.png");
    private static final ResourceLocation CUPRIC_FLAME_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("caverns_and_chasms",
                    "textures/particle/cupric_fire_flame.png");
    private static final ResourceLocation REDSTONE_FLAME_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/particle/generic_6.png");

    public static ResourceLocation getFlameTexture(ResourceLocation itemId) {
        for (String word : itemId.getPath().split("_")) {
            ResourceLocation texture = switch (word) {
                case "soul" -> SOUL_FLAME_TEXTURE;
                case "ender" -> ENDER_FLAME_TEXTURE;
                case "cupric" -> CUPRIC_FLAME_TEXTURE;
                case "redstone" -> REDSTONE_FLAME_TEXTURE;
                default -> null;
            };
            if (texture != null) return texture;
        }
        return FLAME_TEXTURE;
    }

    public static Quaternionf getCameraBillboardRotation() {
        return Minecraft.getInstance().gameRenderer.getMainCamera().rotation();
    }

    public static Quaternionf getHandBillboardRotation() {
        return RenderSystem.getModelViewMatrix().getNormalizedRotation(new Quaternionf()).conjugate();
    }

    //TODO: improve for animated textures
    public static void renderFlame(LivingEntity entity, PoseStack poseStack, MultiBufferSource bufferSource,
                                   ResourceLocation texture, Vec3 offset, float size, Quaternionf billboardRot) {
        VertexConsumer builder = bufferSource.getBuffer(RenderType.text(texture));

        int lu = LightTexture.FULL_BRIGHT & '\uffff';
        int lv = LightTexture.FULL_BRIGHT >> 16 & '\uffff';

        int r, g, b, a;
        a = r = g = b = 255;
        /*
        if (stack.is(Items.REDSTONE_TORCH)) {
            var c = DustParticleOptions.REDSTONE_PARTICLE_COLOR;
            r = (int) (c.x * 255);
            g = (int) (c.y * 255);
            b = (int) (c.z * 255);
        }*/

        float period = 20;
        float t = ((entity.tickCount + Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false)) % period) / period;
        float scale = (1.0F - t * t * 0.4F) * size;

        poseStack.translate(offset.x, offset.y, offset.z);
        Matrix4f mat = new Matrix4f();
        mat.setTranslation(poseStack.last().pose().getTranslation(new Vector3f()));
        mat.rotate(billboardRot);

        poseStack.last().pose().set(mat);
        poseStack.scale(-scale, scale, -scale);

        VertexUtil.addQuad(builder, poseStack, -0.5f, -0.5f, 0.5f, 0.5f,
                r, g, b, a, lu, lv);
    }

    public static void applyFirstPersonHandTransform(PoseStack poseStack, ItemInHandRenderer renderer, HumanoidArm arm,
                                                     float attackAnim, float equipAnim) {
        float f = arm == HumanoidArm.LEFT ? -1.0F : 1.0F;
        float n = -0.4F * Mth.sin(Mth.sqrt(attackAnim) * 3.1415927F);
        float m = 0.2F * Mth.sin(Mth.sqrt(attackAnim) * 6.2831855F);
        float h = -0.2F * Mth.sin(attackAnim * 3.1415927F);

        poseStack.translate(f * n, m, h);
        renderer.applyItemArmTransform(poseStack, arm, equipAnim);
        renderer.applyItemArmAttackTransform(poseStack, arm, attackAnim);
    }

    public static void renderBlockModelAsItem(ItemStack stack, BlockState state, PoseStack poseStack,
                                              MultiBufferSource buffer, int light, boolean left) {
        Minecraft mc = Minecraft.getInstance();
        BakedModel model = mc.getBlockRenderer().getBlockModel(state);
        mc.getItemRenderer().render(stack, ItemDisplayContext.NONE, left, poseStack,
                buffer, light, OverlayTexture.NO_OVERLAY, model);
    }
}
