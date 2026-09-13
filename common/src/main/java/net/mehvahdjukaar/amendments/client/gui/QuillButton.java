package net.mehvahdjukaar.amendments.client.gui;

import net.mehvahdjukaar.amendments.Amendments;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Arrays;
import java.util.Locale;

public class QuillButton extends AbstractWidget {


    protected static final Identifier[] textures = Arrays.stream(QuillType.values())
            .map(t -> Amendments.res("textures/gui/quill/" +
                    t.name().toLowerCase(Locale.ROOT) + ".png")).toArray(Identifier[]::new);

    private int type = 0;

    public QuillButton(Screen screen) {
        super(screen.width / 2 + 70, 20, 48, 144, Component.empty());
        this.refreshTooltip();
    }

    private void refreshTooltip() {
        this.setTooltip(Tooltip.create(Component.translatable("gui.amendments.quill." + getType().name().toLowerCase(Locale.ROOT))));
    }

    public QuillType getType() {
        return QuillType.values()[type];
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        int length = QuillType.values().length;
        type = (type + (InkButton.backwards(event) ? -1 : 1) + length) % length;
        this.refreshTooltip();
    }

    @Override
    protected boolean isValidClickButton(MouseButtonInfo buttonInfo) {
        return super.isValidClickButton(buttonInfo) || buttonInfo.button() == 1;
    }


    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int i, int j, float f) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, textures[type], this.getX(), this.getY(), 0, 0, width, height, width, height);

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
