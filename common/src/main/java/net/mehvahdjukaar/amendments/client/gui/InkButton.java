package net.mehvahdjukaar.amendments.client.gui;

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

public class InkButton extends AbstractWidget {

    protected static final ResourceLocation[] textures = Arrays.stream(Ink.values())
            .map(t -> Amendments.res("textures/gui/ink_well/" +
                    t.name().toLowerCase(Locale.ROOT) + ".png")).toArray(ResourceLocation[]::new);

    private final Runnable clickCallback;
    private int type = 0;

    public InkButton(LecternBookEditScreen screen) {
        super(screen.width / 2 - 130, 90, 52, 50, Component.empty());
        screen.hasShiftDown();
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
    public void onClick(double mouseX, double mouseY) {
        int length = Ink.values().length;
        if (Screen.hasShiftDown()) {
            this.type = (length + type - 1) % length;
        } else {
            this.type = ++type % length;
        }
        this.refreshTooltip();
        this.clickCallback.run();
    }

    @ForgeOverride
    public void onClick(double mouseX, double mouseY, int button) {
        this.onClick(mouseX, mouseY);
    }

    @Override
    protected boolean isValidClickButton(int button) {
        return super.isValidClickButton(button) || button == 1;
    }

    @Override
    protected ClientTooltipPositioner createTooltipPositioner() {
        return DefaultTooltipPositioner.INSTANCE;

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
    protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        guiGraphics.blit(textures[type], this.getX(), this.getY(), 0, 0, width, height, 64, 64);

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
