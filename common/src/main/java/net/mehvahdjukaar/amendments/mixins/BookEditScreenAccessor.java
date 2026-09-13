package net.mehvahdjukaar.amendments.mixins;

import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BookEditScreen.class)
public interface BookEditScreenAccessor {

    @Accessor("page")
    MultiLineEditBox getPageBox();

    @Accessor("currentPage")
    int getCurrentPage();

    @Invoker("updatePageContent")
    void callUpdatePageContent();
}
