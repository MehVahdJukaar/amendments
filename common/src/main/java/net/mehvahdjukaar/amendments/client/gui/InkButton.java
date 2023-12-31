package net.mehvahdjukaar.amendments.client.gui;

import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.amendments.Amendments;
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

    private int type = 0;

    public InkButton(Screen screen) {
        super(screen.width / 2 - 130, screen.height / 2 - 20, 52, 50, Component.empty());
        refreshTooltip();
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
        this.type = ++type % Ink.values().length;
        this.refreshTooltip();
    }

    @PlatformOnly(PlatformOnly.FORGE)
    public void onClick(double mouseX, double mouseY, int button) {
        type += button == 0 ? 1 : -1;
        this.type = type % QuillButton.QuillType.values().length;
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
