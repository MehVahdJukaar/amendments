package net.mehvahdjukaar.amendments.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Shadow
    @Final
    private static Identifier HOTBAR_SELECTION_SPRITE;

    @WrapOperation(method = "extractItemHotbar", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"))
    private void amendments$animate(GuiGraphicsExtractor graphics, RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height,
                                    Operation<Void> original, @Local Player player,
                                    @Local(argsOnly = true) DeltaTracker deltaTracker,
                                    @Share("drawAction") LocalRef<Runnable> drawAction) {
        if (sprite != HOTBAR_SELECTION_SPRITE) {
            original.call(graphics, pipeline, sprite, x, y, width, height);
            return;
        }
        double speed = ClientConfigs.HOTBAR_SLOT_ANIMATION_SPEED.get();
        int selected = player.getInventory().getSelectedSlot();
        float animatedSlot = speed <= 0 ? selected :
                AmendmentsClient.getHotbarSlotAnimation(selected, speed, deltaTracker.getRealtimeDeltaTicks());
        //drawn after the

        //TODO: check again. quite hacky
        drawAction.set(() -> {
            float offset = (animatedSlot - selected) * 20;
            boolean wrappingOverEdge = animatedSlot > 8;

            int barLeft = x - selected * 20;
            if (wrappingOverEdge) graphics.enableScissor(barLeft, y, barLeft + 184, y + height);
            Matrix3x2fStack pose = graphics.pose();
            pose.pushMatrix();
            pose.translate(offset, 0);
            original.call(graphics, pipeline, sprite, x, y, width, height);
            if (wrappingOverEdge) {
                pose.translate(-180, 0);
                original.call(graphics, pipeline, sprite, x, y, width, height);
            }
            pose.popMatrix();
            if (wrappingOverEdge) graphics.disableScissor();
        });
    }

    @Inject(method = "extractItemHotbar", at = @At("RETURN"))
    private void amendments$drawSelectionOnTop(CallbackInfo ci, @Share("drawAction") LocalRef<Runnable> drawAction) {
        var d = drawAction.get();
        if (d == null) return;
        d.run();
        drawAction.set(null);
    }
}
