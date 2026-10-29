package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public class SparklerRendererExtension extends TorchRendererExtension {
    private static final Vec3 SPARK_OFFSET = new Vec3(0, 0.285, 0);

    private final ResourceLocation sparkSprite;

    public SparklerRendererExtension(Item item) {
        super(item);
        this.sparkSprite = Utils.getID(item).withSuffix("_spark_1");
    }

    public static boolean isSparkler(Item item) {
        ResourceLocation id = Utils.getID(item);
        return id.getNamespace().equals("caverns_and_chasms") && id.getPath().endsWith("sparkler");
    }

    //the lit texture has no spark baked in like torches do
    @Override
    protected boolean hasFlame(LivingEntity entity) {
        return true;
    }

    @Override
    protected void renderFlame(LivingEntity entity, PoseStack poseStack, MultiBufferSource buffer,
                               Quaternionf billboardRot) {
        //spark particle is animated so it has to come from the atlas
        HeldItemRenderUtils.renderParticleSpriteFlame(entity, poseStack, buffer, sparkSprite,
                SPARK_OFFSET, 4 / 16f, billboardRot, 2 / 16f);
    }
}
