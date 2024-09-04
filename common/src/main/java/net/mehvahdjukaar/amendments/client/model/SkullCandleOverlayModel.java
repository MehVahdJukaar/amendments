package net.mehvahdjukaar.amendments.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class SkullCandleOverlayModel extends SkullModelBase {
    private final ModelPart root;
    protected final ModelPart head;

    public SkullCandleOverlayModel(ModelPart modelPart) {
        this.root = modelPart;
        this.head = modelPart.getChild("head");
    }

    public static LayerDefinition createMesh() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 32, 16);
    }

    @Override
    public void setupAnim(float f, float g, float h) {
        this.head.yRot = g * (Mth.DEG_TO_RAD);
        this.head.xRot = h * (Mth.DEG_TO_RAD);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}
