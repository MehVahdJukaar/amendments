package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "net.minecraft.client.gui.screens.inventory.MerchantScreen$TradeOfferButton")
public abstract class TradeOfferButtonMixin extends Button {

    protected TradeOfferButtonMixin(int x, int y, int width, int height, Component message,
                                    OnPress onPress, CreateNarration createNarration) {
        super(x, y, width, height, message, onPress, createNarration);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (!ClientConfigs.TRADE_BUTTONS.get()) {
            super.renderWidget(graphics, mouseX, mouseY, partialTicks);
            return;
        }
        graphics.blitSprite(AmendmentsClient.BUTTON_SPRITES.get(this.active, this.isHoveredOrFocused()),
                this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }
}
