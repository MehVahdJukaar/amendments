package net.mehvahdjukaar.amendments.client.gui;

import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.moonlight.api.misc.ForgeOverride;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.Locale;

public class QuillButton extends AbstractWidget {


    protected static final ResourceLocation[] textures = Arrays.stream(QuillType.values())
            .map(t -> Amendments.res("textures/gui/quill/" +
                    t.name().toLowerCase(Locale.ROOT) + ".png")).toArray(ResourceLocation[]::new);

    private int type = 0;

    public QuillButton(Screen screen) {
        super(screen.width / 2 + 70, 20, 48, 144, Component.empty());
        this.refreshTooltip();
    }
    private void refreshTooltip() {
        this.setTooltip(Tooltip.create(Component.translatable("gui.amendments.quill."+getType().name().toLowerCase(Locale.ROOT))));
    }

    public QuillType getType() {
        return QuillType.values()[type];
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        int length = QuillType.values().length;
        if (Screen.hasShiftDown()) {
            this.type = (length + type - 1) % length;
        } else {
            this.type = ++type % length;
        }
        this.refreshTooltip();
    }

    @ForgeOverride
    public void onClick(double mouseX, double mouseY, int button) {
        type += button==0 ? 1 : -1;
        this.type = type % QuillType.values().length;
        this.refreshTooltip();
    }

    @Override
    protected boolean isValidClickButton(int button) {
        return super.isValidClickButton(button) || button == 1;
    }

    @Override
    protected ClientTooltipPositioner createTooltipPositioner() {
        return DefaultTooltipPositioner.INSTANCE;

    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        guiGraphics.blit(textures[type], this.getX(), this.getY(), 0, 0, width, height, width, height);

    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    public ChatFormatting getChatFormatting() {
        return switch (this.getType()) {
            case BOLD -> ChatFormatting.BOLD;
            case STRIKETHROUGH -> ChatFormatting.STRIKETHROUGH;
            case ITALIC -> ChatFormatting.ITALIC;
            case UNDERLINE -> ChatFormatting.UNDERLINE;
            case OBFUSCATED -> ChatFormatting.OBFUSCATED;
            default -> ChatFormatting.RESET;
        };
    }

    public enum QuillType {
        DEFAULT,
        ITALIC,
        BOLD,
        UNDERLINE,
        STRIKETHROUGH,
        OBFUSCATED
    }
}
