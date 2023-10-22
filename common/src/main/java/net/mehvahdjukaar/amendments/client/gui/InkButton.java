package net.mehvahdjukaar.amendments.client.gui;

import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.moonlight.api.set.BlocksColorAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

import java.util.Locale;

public class InkButton extends AbstractWidget {

    protected static final ResourceLocation[] textures = BlocksColorAPI.SORTED_COLORS.stream()
            .map(t -> Amendments.res("textures/gui/ink_well/" +
                    t.name().toLowerCase(Locale.ROOT) + ".png")).toArray(ResourceLocation[]::new);

    private int type = 3;

    public InkButton(Screen screen) {
        super(screen.width / 2 - 130, screen.height / 2 - 20, 52, 50, Component.empty());
    }
    //TODO: custom sounds

    public DyeColor getType() {
        return BlocksColorAPI.SORTED_COLORS.get(type);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.type = ++type % DyeColor.values().length;
    }

    public ChatFormatting getChatFormatting() {
        return switch (this.getType()) {
            case MAGENTA -> ChatFormatting.LIGHT_PURPLE;
            case PURPLE -> ChatFormatting.DARK_PURPLE;
            case BLUE -> ChatFormatting.BLUE;
            case CYAN -> ChatFormatting.DARK_AQUA;
            case LIGHT_BLUE -> ChatFormatting.AQUA;
            case LIGHT_GRAY -> ChatFormatting.GRAY;
            case GRAY -> ChatFormatting.DARK_GRAY;
            case BLACK -> ChatFormatting.BLACK;
            case LIME -> ChatFormatting.GREEN;
            case GREEN -> ChatFormatting.DARK_GREEN;
            case YELLOW -> ChatFormatting.YELLOW;
            case ORANGE -> ChatFormatting.GOLD;
            case BROWN -> ChatFormatting.DARK_BLUE;
            case RED -> ChatFormatting.DARK_RED;
            case PINK -> ChatFormatting.RED;
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
}
