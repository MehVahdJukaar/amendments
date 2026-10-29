package net.mehvahdjukaar.amendments.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.amendments.client.HotbarSelectionAnimation;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Shadow
    @Final
    private static ResourceLocation HOTBAR_SELECTION_SPRITE;

    @WrapOperation(method = "renderItemHotbar", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"))
    private void amendments$animate(GuiGraphics graphics, ResourceLocation sprite, int x, int y, int width, int height,
                                    Operation<Void> original, @Local Player player, @Local(argsOnly = true) DeltaTracker deltaTracker) {
        double speed = ClientConfigs.HOTBAR_SLOT_ANIMATION_SPEED.get();
        if (sprite != HOTBAR_SELECTION_SPRITE || speed <= 0) {
            original.call(graphics, sprite, x, y, width, height);
            return;
        }
        int selected = player.getInventory().selected;
        float animatedSlot = HotbarSelectionAnimation.getHotbarSlotAnimation(selected, speed, deltaTracker.getRealtimeDeltaTicks());
        float offset = (animatedSlot - selected) * 20;
        boolean wrappingOverEdge = animatedSlot > 8;

        int barLeft = x - selected * 20;
        if (wrappingOverEdge) graphics.enableScissor(barLeft, y, barLeft + 184, y + height);
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(offset, 0, HotbarSelectionAnimation.isSelectionOnBackground() ? 0 : 270);
        original.call(graphics, sprite, x, y, width, height);
        if (wrappingOverEdge) {
            pose.translate(-180, 0, 0);
            original.call(graphics, sprite, x, y, width, height);
        }
        pose.popPose();
        if (wrappingOverEdge) graphics.disableScissor();
    }
}
