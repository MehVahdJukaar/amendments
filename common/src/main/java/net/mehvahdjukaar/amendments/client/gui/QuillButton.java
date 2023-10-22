package net.mehvahdjukaar.amendments.client.gui;

import net.mehvahdjukaar.amendments.Amendments;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

import java.util.Arrays;
import java.util.Locale;

public class QuillButton extends AbstractWidget {


    protected static final ResourceLocation[] textures = Arrays.stream(QuillType.values())
            .map(t -> Amendments.res("textures/gui/quill/" +
                    t.name().toLowerCase(Locale.ROOT) + ".png")).toArray(ResourceLocation[]::new);

    private int type = 0;

    public QuillButton(Screen screen) {
        super(screen.width/2+70, screen.height/2-100, 48, 144, Component.empty());
    }

    public QuillType getType() {
        return QuillType.values()[type];
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.type = ++type % QuillType.values().length;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        guiGraphics.blit(textures[type], this.getX(), this.getY(), 0, 0, width, height,width, height);

    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    public ChatFormatting getChatFormatting(){
        return   switch (this.getType()){
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
