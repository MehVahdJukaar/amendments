package net.mehvahdjukaar.amendments.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import net.mehvahdjukaar.amendments.Amendments;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Arrays;
import java.util.Locale;

public class InkButton extends AbstractWidget {

    protected static final Identifier[] textures = Arrays.stream(Ink.values())
            .map(t -> Amendments.res("textures/gui/ink_well/" +
                    t.name().toLowerCase(Locale.ROOT) + ".png")).toArray(Identifier[]::new);

    private final Runnable clickCallback;
    private int type = 0;

    public InkButton(LecternBookEditScreen screen) {
        super(screen.width / 2 - 130, 90, 52, 50, Component.empty());
        refreshTooltip();
        clickCallback = screen::onInkClicked;
    }

    private void refreshTooltip() {
        this.setTooltip(Tooltip.create(Component.translatable("gui.amendments.ink." + getType().name().toLowerCase(Locale.ROOT))));
    }
    //TODO: custom sounds

    public Ink getType() {
        return Ink.values()[type];
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        int length = Ink.values().length;
        this.type = (type + (backwards(event) ? -1 : 1) + length) % length;
        this.refreshTooltip();
        this.clickCallback.run();
    }

    static boolean backwards(MouseButtonEvent event) {
        return event.hasShiftDown() || event.button() == InputConstants.MOUSE_BUTTON_RIGHT;
    }

    @Override
    protected boolean isValidClickButton(MouseButtonInfo buttonInfo) {
        return super.isValidClickButton(buttonInfo) || buttonInfo.button() == InputConstants.MOUSE_BUTTON_RIGHT;
    }

    public ChatFormatting getChatFormatting() {
        return switch (this.getType()) {
            case LIGHT_PURPLE -> ChatFormatting.LIGHT_PURPLE;
            case DARK_PURPLE -> ChatFormatting.DARK_PURPLE;
            case BLUE -> ChatFormatting.BLUE;
            case DARK_AQUA -> ChatFormatting.DARK_AQUA;
            case AQUA -> ChatFormatting.AQUA;
            case GRAY -> ChatFormatting.GRAY;
            case DARK_GRAY -> ChatFormatting.DARK_GRAY;
            case BLACK -> ChatFormatting.BLACK;
            case GREEN -> ChatFormatting.GREEN;
            case DARK_GREEN -> ChatFormatting.DARK_GREEN;
            case YELLOW -> ChatFormatting.YELLOW;
            case GOLD -> ChatFormatting.GOLD;
            case DARK_BLUE -> ChatFormatting.DARK_BLUE;
            case DARK_RED -> ChatFormatting.DARK_RED;
            case RED -> ChatFormatting.RED;
            case WHITE -> ChatFormatting.WHITE;
        };
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int i, int j, float f) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, textures[type], this.getX(), this.getY(), 0, 0, width, height, 64, 64);

    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    public enum Ink {
        BLACK,
        DARK_RED,
        RED,
        LIGHT_PURPLE,
        DARK_PURPLE,
        DARK_BLUE,
        BLUE,
        DARK_AQUA,
        AQUA,
        DARK_GREEN,
        GREEN,
        YELLOW,
        GOLD,
        WHITE,
        GRAY,
        DARK_GRAY
    }
}
