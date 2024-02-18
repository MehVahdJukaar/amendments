package net.mehvahdjukaar.amendments.mixins;

import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BookEditScreen.DisplayCache.class)
public abstract class DisplayCacheMixin {

    @Redirect(method = "getIndexAtPosition", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/StringSplitter;plainIndexAtWidth(Ljava/lang/String;ILnet/minecraft/network/chat/Style;)I"))
    public int plainIndexAtWidth(StringSplitter instance, String content, int maxWidth, Style style) {
        return instance.formattedIndexByWidth(content, maxWidth, style);
    }
}
