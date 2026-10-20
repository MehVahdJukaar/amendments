package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Quaternionf;

public class HeldItemRenderUtils {

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

    public static Identifier getFlameTexture(Identifier itemId) {
        for (String word : itemId.getPath().split("_")) {
            Identifier texture = switch (word) {
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
}
