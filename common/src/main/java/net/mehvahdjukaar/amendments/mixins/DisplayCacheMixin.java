package net.mehvahdjukaar.amendments.mixins;

import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(BookEditScreen.DisplayCache.class)
public class DisplayCacheMixin {

    @(method = "getIndexAtPosition")
}
