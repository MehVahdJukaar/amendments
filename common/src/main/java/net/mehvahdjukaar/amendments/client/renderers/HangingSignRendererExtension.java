package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.mehvahdjukaar.amendments.common.tile.HangingSignTileExtension;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.amendments.integration.CompatHandler;
import net.mehvahdjukaar.amendments.integration.SuppCompat;
import net.mehvahdjukaar.amendments.reg.ModBlockProperties;
import net.mehvahdjukaar.moonlight.api.client.util.LOD;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.client.util.TextUtil;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.BannerPatternItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public class HangingSignRendererExtension {


    public static LayerDefinition createMesh() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("extension_6", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(4.0F, -8.0F, -2.0F, 2.0F, 6.0F, 4.0F),
                PartPose.rotation(0.0F, 0.0F, -1.5708F));
        partDefinition.addOrReplaceChild("extension_5", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(4.0F, -8.0F, -2.0F, 2.0F, 5.0F, 4.0F),
                PartPose.rotation(0.0F, 0.0F, -1.5708F));
        partDefinition.addOrReplaceChild("extension_4", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(4.0F, -8.0F, -2.0F, 2.0F, 4.0F, 4.0F),
                PartPose.rotation(0.0F, 0.0F, -1.5708F));
        partDefinition.addOrReplaceChild("extension_3", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(4.0F, -8.0F, -2.0F, 2.0F, 3.0F, 4.0F),
                PartPose.rotation(0.0F, 0.0F, -1.5708F));

        return LayerDefinition.create(meshDefinition, 16, 16);
    }

    public static LayerDefinition createChainMesh() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        root.addOrReplaceChild("chainL1", CubeListBuilder.create().texOffs(0, 7).addBox(-1.5F, 1.0F, 0.0F, 3.0F, 5.0F, 0.0F), PartPose.offsetAndRotation(-5.0F, -6.0F, 0.0F, 0.0F, -0.7853982F, 0.0F));
        root.addOrReplaceChild("chainL2", CubeListBuilder.create().texOffs(6, 7).addBox(-1.5F, 1.0F, 0.0F, 3.0F, 5.0F, 0.0F), PartPose.offsetAndRotation(-5.0F, -6.0F, 0.0F, 0.0F, 0.7853982F, 0.0F));
        root.addOrReplaceChild("chainR1", CubeListBuilder.create().texOffs(0, 7).addBox(-1.5F, 1.0F, 0.0F, 3.0F, 5.0F, 0.0F), PartPose.offsetAndRotation(5.0F, -6.0F, 0.0F, 0.0F, -0.7853982F, 0.0F));
        root.addOrReplaceChild("chainR2", CubeListBuilder.create().texOffs(6, 7).addBox(-1.5F, 1.0F, 0.0F, 3.0F, 5.0F, 0.0F), PartPose.offsetAndRotation(5.0F, -6.0F, 0.0F, 0.0F, 0.7853982F, 0.0F));
        return LayerDefinition.create(meshDefinition, 64, 32);
    }


    public static void render(SignBlockEntity tile, HangingSignTileExtension extension,  float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource,
                              int light, int overlay, BlockState state,
                              HangingSignRenderer.HangingSignModel model, List<ModelPart> barModel, ModelPart chains,
                              Material material, Material extensionMaterial, SignRenderer renderer,
                              float colorMult, boolean translucent) { //color mult for FD. Translucent for rats

        poseStack.pushPose();

        boolean wallSign = !(state.getBlock() instanceof CeilingHangingSignBlock);
        boolean attached = !wallSign && state.hasProperty(BlockStateProperties.ATTACHED) && state.getValue(BlockStateProperties.ATTACHED);
        poseStack.translate(0.5, 0.875, 0.5);

        Quaternionf yaw;
        if (attached) {
            yaw = Axis.YP.rotationDegrees(-RotationSegment.convertToDegrees(state.getValue(CeilingHangingSignBlock.ROTATION)));
        } else {
            yaw = Axis.YP.rotationDegrees(getSignAngle(state, wallSign));
        }
        poseStack.mulPose(yaw);

        model.evaluateVisibleParts(state);
        VertexConsumer vertexConsumer = material.buffer(bufferSource, translucent ? RenderType::entityTranslucent : model::renderType);

        poseStack.scale(1, -1, -1);
        //TODO: ceiling banner rot

        boolean visible = model.plank.visible;
        boolean visibleC = model.normalChains.visible;
        model.plank.visible = false;
        if (wallSign) {
            model.normalChains.visible = false;
        }

        poseStack.pushPose();

        Quaternionf pitch = new Quaternionf();
        if (extension.canSwing()) {
            float rot = extension.getClientAnimation().getAngle(partialTicks);

            if (!wallSign && attached) {
                //y swing
                pitch = Axis.YP.rotationDegrees(rot);
            } else {
                pitch = Axis.XP.rotationDegrees(rot);
            }

            if (!wallSign) {
                poseStack.translate(0, -0.125, 0);
            }
            poseStack.mulPose(pitch);
            if (!wallSign) {
                poseStack.translate(0, 0.125, 0);
            }
        }


        Vector3f norm = Direction.SOUTH.step().rotate(pitch).rotate(yaw);


        //model
        poseStack.pushPose();

        poseStack.translate(0, 0.25, 0);


        model.root.render(poseStack, vertexConsumer, light, overlay);
        if (wallSign) {
            chains.render(poseStack, vertexConsumer, light, overlay); //shorter chains
            model.normalChains.visible = visibleC;
        }
        model.plank.visible = visible;

        poseStack.popPose();


        poseStack.scale(1, -1, -1);

        Minecraft mc = Minecraft.getInstance();
        var camera = mc.gameRenderer.getMainCamera();
        var font = mc.font;
        boolean filtered = mc.isTextFilteringEnabled();

        LOD lod = new LOD(camera, tile.getBlockPos());


        poseStack.pushPose();
        renderFront(tile, extension, poseStack, bufferSource, light, overlay, renderer, colorMult, norm, font, filtered, lod);
        poseStack.popPose();

        poseStack.pushPose();
        renderBack(tile, extension, poseStack, bufferSource, light, overlay, renderer, colorMult, norm, font, filtered, lod);
        poseStack.popPose();

        poseStack.popPose();

        poseStack.translate(0, 0.25, 0);


        //Straight stuff

        if (visible) {
            model.plank.render(poseStack, vertexConsumer, light, overlay);
        }

        ModBlockProperties.PostType right = extension.getRightAttachment();
        ModBlockProperties.PostType left = extension.getLeftAttachment();

        if (!ClientConfigs.SIGN_ATTACHMENT.get()) {
            right = null;
            left = null;
        }

        VertexConsumer vc2 = null;
        if (right != null || left != null) {
            vc2 = extensionMaterial.buffer(bufferSource, model::renderType);
        }
        if (left != null) {
            poseStack.pushPose();
            poseStack.translate(1, 0, 0);
            barModel.get(left.ordinal()).render(poseStack, vc2, light, overlay);
            poseStack.popPose();
        }
        if (right != null) {
            poseStack.pushPose();
            poseStack.mulPose(RotHlpr.Y180);
            poseStack.translate(1, 0, 0);
            barModel.get(right.ordinal()).render(poseStack, vc2, light, overlay);
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private static void renderFront(SignBlockEntity tile, HangingSignTileExtension extension, PoseStack poseStack, MultiBufferSource buffer,
                                    int light, int overlay, SignRenderer renderer, float colorMult,
                                    Vector3f norm, Font font, boolean filtered, LOD lod) {
        ItemStack item = extension.getFrontItem();

        if (item.isEmpty()) {
            renderer.translateSignText(poseStack, true, renderer.getTextOffset());
            renderSignText(tile.getFrontText(), font, poseStack, buffer, light,
                    norm, lod, filtered, tile.getTextLineHeight(), tile.getMaxTextLineWidth(),
                    colorMult);
        } else if (CompatHandler.SUPPLEMENTARIES && item.getItem() instanceof BannerPatternItem banner) {
            renderBannerPattern(tile.getFrontText(), poseStack, buffer, light, banner);
        } else {
            poseStack.mulPose(RotHlpr.Y180);
            renderItem(item, poseStack, buffer, light, overlay, tile.getLevel());
        }
    }

    private static void renderBack(SignBlockEntity tile, HangingSignTileExtension extension, PoseStack poseStack, MultiBufferSource buffer,
                                   int light, int overlay, SignRenderer renderer, float colorMult,
                                   Vector3f norm, Font font, boolean filtered, LOD lod) {
        ItemStack item = extension.getBackItem();

        if (item.isEmpty()) {
            renderer.translateSignText(poseStack, false, renderer.getTextOffset());
            renderSignText(tile.getBackText(), font, poseStack, buffer, light,
                    norm.mul(-1), lod, filtered, tile.getTextLineHeight(), tile.getMaxTextLineWidth(),
                    colorMult);
        } else if (CompatHandler.SUPPLEMENTARIES && item.getItem() instanceof BannerPatternItem banner) {
            poseStack.mulPose(RotHlpr.Y180);
            renderBannerPattern(tile.getBackText(), poseStack, buffer, light, banner);
        } else {
            renderItem(item, poseStack, buffer, light, overlay, tile.getLevel());
        }
    }


    private static float getSignAngle(BlockState state, boolean attachedToWall) {
        return attachedToWall ? -(state.getValue(WallSignBlock.FACING)).toYRot() : -((state.getValue(CeilingHangingSignBlock.ROTATION) * 360) / 16.0F);
    }

    public static void renderSignText(SignText signText, Font font, PoseStack poseStack,
                                      MultiBufferSource buffer,
                                      int light, Vector3f normal, LOD lod, boolean filtered,
                                      int lineHeight, int lineWidth,
                                      float colorMult) {
        TextUtil.RenderProperties properties = TextUtil.renderProperties(signText.getColor(),
                signText.hasGlowingText(), colorMult, light, Style.EMPTY, normal, lod::isVeryNear);

        FormattedCharSequence[] formattedCharSequences = signText.getRenderMessages(filtered, (component) -> {
            List<FormattedCharSequence> list = font.split(component, lineWidth);
            return list.isEmpty() ? FormattedCharSequence.EMPTY : list.get(0);
        });
        for (int i = 0; i < formattedCharSequences.length; i++) {
            TextUtil.renderLine(formattedCharSequences[i], font, lineHeight * i, poseStack, buffer, properties);
        }
    }

    private static void renderBannerPattern(SignText sign, PoseStack poseStack, MultiBufferSource bufferSource,
                                            int packedLight, BannerPatternItem banner) {

        Material renderMaterial = SuppCompat.getFlagMaterial(banner);
        if (renderMaterial != null) {
            poseStack.pushPose();
            poseStack.translate(0, -9 / 16f, 1 / 16f + 0.001);

            float scale = ClientConfigs.getItemPixelScale() / 14f;
            poseStack.scale(scale, -scale, -1);
            VertexConsumer consumer = renderMaterial.buffer(bufferSource, RenderType::entityNoOutline);

            int color = sign.getColor().getTextureDiffuseColor();
            int b = FastColor.ARGB32.blue(color);
            int g = FastColor.ARGB32.green(color);
            int r = FastColor.ARGB32.red(color);
            int light = packedLight;
            if (sign.hasGlowingText()) {
                light = LightTexture.FULL_BRIGHT;
            }

            int lu = light & '\uffff';
            int lv = light >> 16 & '\uffff';

            VertexUtil.addQuad(consumer, poseStack, -0.4375F, -0.4375F, 0.4375F, 0.4375F,
                    0.5f + 0.09375f, 1 - 0.0625f, 0.15625f, 0.0625f, r, g, b, 255, lu, lv);

            poseStack.popPose();
        }
    }

    public static void renderItem(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay, Level level) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        BakedModel model = itemRenderer.getModel(stack, level, null, 0);
        poseStack.pushPose();
        float z = model.isGui3d() ? 7 / 64f : 5 / 64f;
        poseStack.translate(0, -9 / 16f, -z);

        float scale = ClientConfigs.getItemPixelScale() / 16f;
        poseStack.scale(scale, scale, scale);

        itemRenderer.render(stack, ItemDisplayContext.FIXED, true, poseStack, buffer, light, overlay, model);
        poseStack.popPose();
    }

}