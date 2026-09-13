package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.amendments.client.ModRenderTypes;
import net.mehvahdjukaar.amendments.common.entity.IExtinguishableFireball;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LightLayer;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class Fireball3DRenderer<E extends Entity> extends ThrownProjectile3DRenderer<E, Fireball3DRenderState> {

    private final ModelPart cube;
    private final ModelPart cubeEmissive;
    private final ModelPart overlay;
    private final Identifier overlayTexture;
    @Nullable
    private final Identifier noOverlayTexture;
    private final Function<Identifier, RenderType> renderTypeFunction;

    public Fireball3DRenderer(EntityRendererProvider.Context context, float scale,
                              Identifier texture,
                              Identifier overlayTexture,
                              @Nullable Identifier offTexture,
                              ModelLayerLocation modelLocation,
                              boolean hasNoShade) {
        super(context, scale, texture);
        this.overlayTexture = overlayTexture;
        this.noOverlayTexture = offTexture;
        this.renderTypeFunction = hasNoShade ? ModRenderTypes.ENTITY_LIT : RenderTypes::entityCutout;
        ModelPart model = context.bakeLayer(modelLocation);
        this.cube = model.getChild("cube");
        this.cubeEmissive = model.getChild("cube_emissive");
        this.overlay = model.getChild("overlay");
    }

    @Override
    public Fireball3DRenderState createRenderState() {
        return new Fireball3DRenderState();
    }

    @Override
    protected int getBlockLightLevel(E entity, BlockPos pos) {
        if (!isLit(entity) && noOverlayTexture != null) return super.getBlockLightLevel(entity, pos);
        // otherwise its always 15 since its on fire
        return entity.level().getBrightness(LightLayer.BLOCK, pos);
    }

    @Override
    public void extractRenderState(E entity, Fireball3DRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.lit = isLit(entity);
    }

    @Override
    protected void submitBall(Fireball3DRenderState state, PoseStack poseStack,
                              SubmitNodeCollector collector, CameraRenderState camera) {
        boolean hasOverlay = state.lit || noOverlayTexture == null;
        Identifier mainTexture = hasOverlay ? this.getTexture() : noOverlayTexture;
        RenderType mainRenderType = renderTypeFunction.apply(mainTexture);
        collector.submitModelPart(cube, poseStack, mainRenderType, state.lightCoords, OverlayTexture.NO_OVERLAY, null);
        collector.submitModelPart(cubeEmissive, poseStack, mainRenderType, LightCoordsUtil.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY, null);

        if (hasOverlay) {
            float f = state.ageInTicks;
            RenderType fireRenderType = RenderTypes.energySwirl(overlayTexture,
                    this.fireXOffset(f) % 1.0F, f * 0.01F % 1.0F);
            collector.submitModelPart(overlay, poseStack, fireRenderType, LightCoordsUtil.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY, null);
        }
    }

    // dont read fire ticks directly, mods that retexture fireballs zero those out
    private boolean isLit(E entity) {
        if (entity instanceof IExtinguishableFireball fireball) return !fireball.amendments$isExtinguished();
        return entity.getRemainingFireTicks() > 0;
    }

    private float fireXOffset(float tickCount) {
        return tickCount * 0.01F;
    }
}
