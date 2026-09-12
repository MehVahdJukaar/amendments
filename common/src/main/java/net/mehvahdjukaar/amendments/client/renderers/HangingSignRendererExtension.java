package net.mehvahdjukaar.amendments.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.mehvahdjukaar.amendments.common.ExtendedHangingSign;
import net.mehvahdjukaar.amendments.common.tile.HangingSignTileExtension;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.integration.CompatHandler;
import net.mehvahdjukaar.amendments.integration.SuppCompat;
import net.mehvahdjukaar.amendments.integration.SuppCompatClient;
import net.mehvahdjukaar.moonlight.api.client.util.LOD;
import net.mehvahdjukaar.moonlight.api.client.util.RotHlpr;
import net.mehvahdjukaar.moonlight.api.client.util.TextUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.LightCoordsUtil;
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

    // Vanilla HangingSignRenderer.TEXT_OFFSET. We use this constant directly instead of querying
    // renderer.getTextOffset(): the @Invoker accessor doesn't reliably virtual-dispatch to FD's deeply
    // nested HangingCanvasSignRenderer override and falls back to the standing-sign offset, throwing the
    // text up off the sign. Every hanging sign uses the same model shape, so a fixed offset is correct here.
    private static final Vector3f TEXT_OFFSET = new Vector3f(0, -0.32F, 0.073F);

    private static final float TEXT_SCALE = 0.0140625F;


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


    public static void extract(SignBlockEntity tile, SignRenderState state, float partialTicks,
                               ItemModelResolver itemModels, SpriteId extensionSprite) {
        HangingSignRenderData data = ((ExtendedSignRenderState) state).amendments$getData();
        data.enhanced = false;
        if (!(tile instanceof ExtendedHangingSign hs)) return;
        if (!ClientConfigs.SIGN_ATTACHMENT.get() && !ClientConfigs.SWINGING_SIGNS.get() &&
                !CommonConfigs.HANGING_SIGN_ITEM.get()) return;

        HangingSignTileExtension extension = hs.amendments$getExtension();
        BlockState blockState = tile.getBlockState();

        data.enhanced = true;
        data.wallSign = !(blockState.getBlock() instanceof CeilingHangingSignBlock);
        data.attached = !data.wallSign && blockState.hasProperty(BlockStateProperties.ATTACHED) &&
                blockState.getValue(BlockStateProperties.ATTACHED);
        data.yaw = data.attached ?
                -RotationSegment.convertToDegrees(blockState.getValue(CeilingHangingSignBlock.ROTATION)) :
                getSignAngle(blockState, data.wallSign);
        data.swing = extension.canSwing() ? extension.getClientAnimation().getAngle(partialTicks) : 0;
        data.translucent = state.woodType.name().equals("rats:pirat");
        data.veryNear = LOD.at(tile).isVeryNear();
        data.colorMult = CompatHandler.SUPPLEMENTARIES ? SuppCompat.getSignColorMult() : 1;
        data.extensionSprite = extensionSprite;

        if (ClientConfigs.SIGN_ATTACHMENT.get()) {
            data.left = extension.getLeftAttachment();
            data.right = extension.getRightAttachment();
        } else {
            data.left = null;
            data.right = null;
        }

        Level level = tile.getLevel();
        data.frontFlag = extractDisplayed(data.frontItem, extension.getFrontItem(), level, itemModels);
        data.backFlag = extractDisplayed(data.backItem, extension.getBackItem(), level, itemModels);
    }

    private static SpriteId extractDisplayed(ItemStackRenderState out, ItemStack item, Level level,
                                             ItemModelResolver itemModels) {
        out.clear();
        if (item.isEmpty()) return null;
        //TODO: add back. BannerPatternItem is gone in 26.1 and supplementaries isnt ported yet
        //if (CompatHandler.SUPPLEMENTARIES && item.getItem() instanceof BannerPatternItem banner) {
        //    return SuppCompatClient.getFlagMaterial(level, banner);
        //}
        itemModels.updateForTopItem(out, item, ItemDisplayContext.FIXED, level, null, 0);
        return null;
    }


    public static void submit(SignRenderState state, HangingSignRenderData data, PoseStack poseStack,
                              SubmitNodeCollector collector, Model.Simple model, SpriteId signSprite,
                              List<ModelPart> barModel, ModelPart chains, SpriteGetter sprites) {
        int light = state.lightCoords;
        int overlay = OverlayTexture.NO_OVERLAY;
        RenderType signType = signSprite.renderType(data.translucent ? RenderTypes::entityTranslucent : model.renderType());
        var signAtlas = sprites.get(signSprite);

        ModelPart root = model.root();
        ModelPart board = root.getChild("board");
        ModelPart plank = root.hasChild("plank") ? root.getChild("plank") : null;
        ModelPart hangers = root.hasChild("normalChains") ? root.getChild("normalChains") :
                root.hasChild("vChains") ? root.getChild("vChains") : null;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.875, 0.5);

        Quaternionf yaw = Axis.YP.rotationDegrees(data.yaw);
        poseStack.mulPose(yaw);
        poseStack.scale(1, -1, -1);

        poseStack.pushPose();

        Quaternionf pitch = new Quaternionf();
        if (data.swing != 0) {
            pitch = !data.wallSign && data.attached ? Axis.YP.rotationDegrees(data.swing) : Axis.XP.rotationDegrees(data.swing);
            if (!data.wallSign) poseStack.translate(0, -0.125, 0);
            poseStack.mulPose(pitch);
            if (!data.wallSign) poseStack.translate(0, 0.125, 0);
        }

        Vector3f norm = Direction.SOUTH.step().rotate(pitch).rotate(yaw);

        poseStack.pushPose();
        poseStack.translate(0, 0.25, 0);
        collector.submitModelPart(board, poseStack, signType, light, overlay, signAtlas);
        if (data.wallSign) {
            //shorter chains so the sign can hang off a post
            collector.submitModelPart(chains, poseStack, signType, light, overlay, signAtlas);
        } else if (hangers != null) {
            collector.submitModelPart(hangers, poseStack, signType, light, overlay, signAtlas);
        }
        poseStack.popPose();

        poseStack.scale(1, -1, -1);

        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        boolean filtered = mc.isTextFilteringEnabled();

        poseStack.pushPose();
        submitSide(state, data, poseStack, collector, light, overlay, true, norm, font, filtered);
        poseStack.popPose();

        poseStack.pushPose();
        submitSide(state, data, poseStack, collector, light, overlay, false, new Vector3f(norm).mul(-1), font, filtered);
        poseStack.popPose();

        poseStack.popPose();

        poseStack.translate(0, 0.25, 0);

        if (plank != null) {
            collector.submitModelPart(plank, poseStack, signType, light, overlay, signAtlas);
        }

        if (data.extensionSprite != null && (data.left != null || data.right != null)) {
            RenderType barType = data.extensionSprite.renderType(model.renderType());
            var barAtlas = sprites.get(data.extensionSprite);
            if (data.left != null) {
                poseStack.pushPose();
                poseStack.translate(1, 0, 0);
                collector.submitModelPart(barModel.get(data.left.ordinal()), poseStack, barType, light, overlay, barAtlas);
                poseStack.popPose();
            }
            if (data.right != null) {
                poseStack.pushPose();
                poseStack.mulPose(RotHlpr.Y180);
                poseStack.translate(1, 0, 0);
                collector.submitModelPart(barModel.get(data.right.ordinal()), poseStack, barType, light, overlay, barAtlas);
                poseStack.popPose();
            }
        }

        poseStack.popPose();
    }

    private static void submitSide(SignRenderState state, HangingSignRenderData data, PoseStack poseStack,
                                   SubmitNodeCollector collector, int light, int overlay, boolean front,
                                   Vector3f norm, Font font, boolean filtered) {
        SignText text = front ? state.frontText : state.backText;
        SpriteId flag = front ? data.frontFlag : data.backFlag;
        ItemStackRenderState item = front ? data.frontItem : data.backItem;

        if (flag != null) {
            if (!front) poseStack.mulPose(RotHlpr.Y180);
            if (text != null) submitFlag(text, poseStack, collector, light, flag);
        } else if (!item.isEmpty()) {
            if (front) poseStack.mulPose(RotHlpr.Y180);
            submitItem(item, poseStack, collector, light, overlay);
        } else if (text != null) {
            translateText(poseStack, front);
            submitText(text, font, poseStack, collector, light, norm, data, filtered,
                    state.textLineHeight, state.maxTextLineWidth);
        }
    }

    private static void translateText(PoseStack poseStack, boolean front) {
        if (!front) poseStack.mulPose(RotHlpr.Y180);
        poseStack.translate(TEXT_OFFSET.x, TEXT_OFFSET.y, TEXT_OFFSET.z);
        poseStack.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);
    }

    private static float getSignAngle(BlockState state, boolean attachedToWall) {
        return attachedToWall ? -(state.getValue(WallSignBlock.FACING)).toYRot() :
                -((state.getValue(CeilingHangingSignBlock.ROTATION) * 360) / 16.0F);
    }

    private static void submitText(SignText signText, Font font, PoseStack poseStack, SubmitNodeCollector collector,
                                   int light, Vector3f normal, HangingSignRenderData data, boolean filtered,
                                   int lineHeight, int lineWidth) {
        TextUtil.RenderProperties properties = TextUtil.renderProperties(signText.getColor(),
                signText.hasGlowingText(), data.colorMult, light, Style.EMPTY, normal, () -> data.veryNear);

        FormattedCharSequence[] lines = signText.getRenderMessages(filtered, component -> {
            List<FormattedCharSequence> list = font.split(component, lineWidth);
            return list.isEmpty() ? FormattedCharSequence.EMPTY : list.get(0);
        });

        int color = properties.outline() ? properties.textColor() : properties.darkenedColor();
        int outline = properties.outline() ? properties.darkenedColor() : 0;
        int midPoint = 4 * lineHeight / 2;
        for (int i = 0; i < lines.length; i++) {
            FormattedCharSequence line = lines[i];
            if (line == null) continue;
            float x = -font.width(line) / 2f;
            collector.submitText(poseStack, x, i * lineHeight - midPoint, line, false,
                    Font.DisplayMode.POLYGON_OFFSET, properties.light(), color, 0, outline);
        }
    }

    private static void submitFlag(SignText sign, PoseStack poseStack, SubmitNodeCollector collector,
                                   int packedLight, SpriteId flag) {
        poseStack.pushPose();
        poseStack.translate(0, -9 / 16f, 1 / 16f + 0.001);

        float scale = ClientConfigs.getItemPixelScale() / 14f;
        poseStack.scale(scale, -scale, -1);

        int color = sign.getColor().getTextureDiffuseColor();
        int r = ARGB.red(color);
        int g = ARGB.green(color);
        int b = ARGB.blue(color);
        int light = sign.hasGlowingText() ? LightCoordsUtil.FULL_BRIGHT : packedLight;
        int lu = light & 0xffff;
        int lv = light >> 16 & 0xffff;

        collector.submitCustomGeometry(poseStack, flag.renderType(tex -> RenderTypes.entityCutout(tex, false)),
                (pose, consumer) -> addFlagQuad(consumer, pose, r, g, b, lu, lv));

        poseStack.popPose();
    }

    private static void addFlagQuad(VertexConsumer consumer, PoseStack.Pose pose, int r, int g, int b, int lu, int lv) {
        float x0 = -0.4375F, y0 = -0.4375F, x1 = 0.4375F, y1 = 0.4375F;
        float u0 = 0.5f + 0.09375f, v0 = 1 - 0.0625f, u1 = 0.15625f, v1 = 0.0625f;
        flagVertex(consumer, pose, x0, y1, u1, v0, r, g, b, lu, lv);
        flagVertex(consumer, pose, x1, y1, u0, v0, r, g, b, lu, lv);
        flagVertex(consumer, pose, x1, y0, u0, v1, r, g, b, lu, lv);
        flagVertex(consumer, pose, x0, y0, u1, v1, r, g, b, lu, lv);
    }

    private static void flagVertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y,
                                   float u, float v, int r, int g, int b, int lu, int lv) {
        //not chained because of MC263524
        consumer.addVertex(pose.pose(), x, y, 0);
        consumer.setColor(r, g, b, 255);
        consumer.setUv(u, v);
        consumer.setUv1(0, 10);
        consumer.setUv2(lu, lv);
        consumer.setNormal(pose, 0, 0, -1);
    }

    private static void submitItem(ItemStackRenderState item, PoseStack poseStack, SubmitNodeCollector collector,
                                   int light, int overlay) {
        poseStack.pushPose();
        boolean flat = item.getModelBoundingBox().getZsize() <= 1 / 16f;
        poseStack.translate(0, -9 / 16f, -(flat ? 5 / 64f : 7 / 64f));

        float scale = ClientConfigs.getItemPixelScale() / 16f;
        poseStack.scale(scale, scale, scale);

        item.submit(poseStack, collector, light, overlay, 0);
        poseStack.popPose();
    }
}
