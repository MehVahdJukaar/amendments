package net.mehvahdjukaar.amendments.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.mehvahdjukaar.amendments.client.renderers.ExtendedSignRenderState;
import net.mehvahdjukaar.amendments.client.renderers.HangingSignRenderData;
import net.mehvahdjukaar.amendments.client.renderers.HangingSignRendererExtension;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.client.renderer.AbstractCanvasSignRenderer;
import vectorwing.farmersdelight.client.renderer.HangingCanvasSignRenderer;
import vectorwing.farmersdelight.client.renderer.state.HangingCanvasSignRenderState;

import java.util.List;

@Pseudo
@Mixin(HangingCanvasSignRenderer.class)
public abstract class CompatFarmersDelightHangingSignMixin extends AbstractCanvasSignRenderer<HangingCanvasSignRenderState> {

    @Shadow
    protected abstract Model.Simple getSignModel(HangingCanvasSignRenderState state);

    @Shadow
    protected abstract SpriteId getSignSprite(@Nullable DyeColor color);

    @Unique
    private List<ModelPart> amendments$barModel;
    @Unique
    private ModelPart amendments$chains;
    @Unique
    private SpriteGetter amendments$sprites;
    @Unique
    private ItemModelResolver amendments$itemModels;

    public CompatFarmersDelightHangingSignMixin(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void initEnhancedSign(BlockEntityRendererProvider.Context context, CallbackInfo ci) {
        this.amendments$sprites = context.sprites();
        this.amendments$itemModels = context.itemModelResolver();
        if (PlatHelper.isModLoadingValid()) {
            ModelPart model = context.bakeLayer(AmendmentsClient.HANGING_SIGN_EXTENSION);
            this.amendments$barModel = List.of(model.getChild("extension_6"),
                    model.getChild("extension_5"),
                    model.getChild("extension_4"),
                    model.getChild("extension_3"));
            this.amendments$chains = context.bakeLayer(AmendmentsClient.HANGING_SIGN_EXTENSION_CHAINS);
        }
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    public void extractEnhancedSign(SignBlockEntity blockEntity, HangingCanvasSignRenderState state, float partialTicks,
                                    Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress,
                                    CallbackInfo ci) {
        if (this.amendments$chains == null) return;
        HangingSignRendererExtension.extract(blockEntity, state, partialTicks, this.amendments$itemModels,
                AmendmentsClient.CANVAS_SIGH_MATERIAL);
    }

    @Override
    public void submit(HangingCanvasSignRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        HangingSignRenderData data = ((ExtendedSignRenderState) state).amendments$getData();
        if (data.enhanced) {
            HangingSignRendererExtension.submit(state, data, poseStack, collector,
                    this.getSignModel(state), this.getSignSprite(state.color),
                    this.amendments$barModel, this.amendments$chains, this.amendments$sprites);
        } else {
            super.submit(state, poseStack, collector, camera);
        }
    }
}
