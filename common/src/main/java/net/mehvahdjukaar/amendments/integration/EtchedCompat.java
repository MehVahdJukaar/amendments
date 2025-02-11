package net.mehvahdjukaar.amendments.integration;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import gg.moonflower.etched.common.component.DiscAppearanceComponent;
import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;

public class EtchedCompat {

    public static void drawDisc(ItemStack item, PoseStack poseStack, MultiBufferSource bufferSource,
                                int lu, int lv) {
        DiscAppearanceComponent app = (DiscAppearanceComponent)
                item.getOrDefault(CompatObjects.DISC_APPEARANCE.get(), DiscAppearanceComponent.DEFAULT);
        int color = app.discColor();
        if (color != -11447983) {
            VertexConsumer builder = AmendmentsClient.TINTED_RECORD.buffer(bufferSource, RenderType::entityCutout);
            drawColoredQuad(poseStack, builder, lu, lv, color);
        } else {
            VertexConsumer builder = AmendmentsClient.DEFAULT_RECORD.buffer(bufferSource, RenderType::entityCutout);
            VertexUtil.addQuad(builder, poseStack, -0.5f, -0.5f, 0.5f, 0.5f, lu, lv);
        }

        var pattern = app.pattern();
        if (pattern.isColorable()) {
            VertexConsumer builder = AmendmentsClient.RECORD_PATTERNS.get(pattern.ordinal()).buffer(bufferSource, RenderType::entityCutout);
            int primaryColor = app.labelPrimaryColor();
            if (primaryColor == 0) primaryColor = -1;
            drawColoredQuad(poseStack, builder, lu, lv, primaryColor);
            builder = AmendmentsClient.RECORD_PATTERNS_OVERLAY.get(pattern.ordinal()).buffer(bufferSource, RenderType::entityCutout);
            int secondaryColor = app.labelSecondaryColor();
            if (secondaryColor == 0) secondaryColor = -1;
            drawColoredQuad(poseStack, builder, lu, lv, secondaryColor);
        }
    }

    private static void drawColoredQuad(PoseStack poseStack, VertexConsumer builder, int lu, int lv, int color) {
        int r = FastColor.ARGB32.red(color);
        int g = FastColor.ARGB32.green(color);
        int b = FastColor.ARGB32.blue(color);
        VertexUtil.addQuad(builder, poseStack, -0.5f, -0.5f, 0.5f, 0.5f,
                r, g, b, 255, lu, lv);
    }
}
