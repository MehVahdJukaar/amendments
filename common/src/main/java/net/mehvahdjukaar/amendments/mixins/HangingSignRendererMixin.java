package net.mehvahdjukaar.amendments.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.mehvahdjukaar.amendments.client.ModMaterials;
import net.mehvahdjukaar.amendments.client.renderers.HangingSignRendererExtension;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.amendments.integration.CompatHandler;
import net.mehvahdjukaar.amendments.integration.SuppCompat;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(HangingSignRenderer.class)
public abstract class HangingSignRendererMixin extends SignRenderer {

    @Shadow
    @Final
    private Map<WoodType, HangingSignRenderer.HangingSignModel> hangingSignModels;

    @Shadow
    abstract Material getSignMaterial(WoodType woodType);

    @Unique
    private List<ModelPart> amendments$barModel;
    @Unique
    private ModelPart amendments$chains;

    protected HangingSignRendererMixin(BlockEntityRendererProvider.Context context) {
        super(context);
    }


    @Inject(method = "render(Lnet/minecraft/world/level/block/entity/SignBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At("HEAD"), cancellable = true)
    public void renderEnhancedSign(SignBlockEntity tile, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource,
                                   int packedLight, int packedOverlay, CallbackInfo ci) {
        if (ClientConfigs.SIGN_ATTACHMENT.get() || ClientConfigs.SWINGING_SIGNS.get()) {
            BlockState blockState = tile.getBlockState();
            WoodType woodType = SignBlock.getWoodType(blockState.getBlock());
            //normal model
            HangingSignRenderer.HangingSignModel model = this.hangingSignModels.get(woodType);

            boolean translucent = woodType.name().equals("pirat");
            HangingSignRendererExtension.render(tile, partialTick, poseStack, bufferSource, packedLight, packedOverlay,
                    blockState, model, amendments$barModel, amendments$chains,
                    this.getSignMaterial(woodType),
                    ModMaterials.HANGING_SIGN_EXTENSIONS.get().get(woodType),
                    this, CompatHandler.SUPPLEMENTARIES ? SuppCompat.getSignColorMult() : 1,
                    translucent);

            ci.cancel();
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void initEnhancedSign(BlockEntityRendererProvider.Context context, CallbackInfo ci) {
        if (PlatHelper.isModLoadingValid()) {
            ModelPart model = context.bakeLayer(AmendmentsClient.HANGING_SIGN_EXTENSION);
            this.amendments$barModel = List.of(model.getChild("extension_6"),
                    model.getChild("extension_5"),
                    model.getChild("extension_4"),
                    model.getChild("extension_3"));
            this.amendments$chains = context.bakeLayer(AmendmentsClient.HANGING_SIGN_EXTENSION_CHAINS);
        }
    }

}
