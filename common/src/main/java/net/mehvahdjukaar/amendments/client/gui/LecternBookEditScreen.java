package net.mehvahdjukaar.amendments.client.gui;

import net.mehvahdjukaar.amendments.common.LecternEditMenu;
import net.mehvahdjukaar.amendments.common.network.ServerBoundSyncLecternBookMessage;
import net.mehvahdjukaar.amendments.mixins.BookEditScreenAccessor;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WritableBookContent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class LecternBookEditScreen extends BookEditScreen implements MenuAccess<LecternEditMenu> {

    private static final int BUTTON_WIDTH = 76;

    private final LecternEditMenu menu;
    private final List<String> lastSaved = new ArrayList<>();

    private final ContainerListener listener = new ContainerListener() {
        @Override
        public void slotChanged(AbstractContainerMenu containerToSend, int dataSlotIndex, ItemStack stack) {
            book = stack;
            pages.clear();
            WritableBookContent content = book.get(DataComponents.WRITABLE_BOOK_CONTENT);
            if (content != null) {
                content.getPages(Minecraft.getInstance().isTextFilteringEnabled()).forEach(pages::add);
            }
            if (pages.isEmpty()) pages.add("");
            markSaved();
            refreshPage();
        }

        @Override
        public void dataChanged(AbstractContainerMenu containerMenu, int dataSlotIndex, int value) {
            if (dataSlotIndex == 0) {
                setPage(value);
            }
        }
    };

    private int lastPage;
    private QuillButton quill;
    private InkButton ink;

    public LecternBookEditScreen(LecternEditMenu lecternMenu, Inventory inventory, Component component) {
        super(inventory.player, lecternMenu.getBook(), InteractionHand.MAIN_HAND,
                lecternMenu.getBook().getOrDefault(DataComponents.WRITABLE_BOOK_CONTENT, WritableBookContent.EMPTY));
        this.menu = lecternMenu;
        this.lastPage = this.menu.getPage();
        this.markSaved();
    }

    @Override
    public LecternEditMenu getMenu() {
        return this.menu;
    }

    @Override
    protected void init() {
        super.init();

        //vanilla adds its sign and done buttons last. both save straight to the held book so they have to go
        List<? extends GuiEventListener> widgets = this.children();
        GuiEventListener vanillaDone = widgets.get(widgets.size() - 1);
        GuiEventListener vanillaSign = widgets.get(widgets.size() - 2);
        this.removeWidget(vanillaDone);
        this.removeWidget(vanillaSign);

        this.quill = this.addRenderableWidget(new QuillButton(this));
        this.ink = this.addRenderableWidget(new InkButton(this));

        int y = 196;
        int separation = 80;
        this.addRenderableWidget(Button.builder(Component.translatable("lectern.take_book"), b -> {
            this.saveChanges(Optional.empty(), true);
            if (!this.isModified()) this.sendButtonClick(3);
        }).bounds(this.width / 2 - BUTTON_WIDTH / 2 - 6, y, BUTTON_WIDTH, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("book.signButton"), b ->
                this.minecraft.setScreen(new LecternBookSignScreen(this, this.minecraft.player))
        ).bounds((this.width - BUTTON_WIDTH) / 2 - separation - 6, y, BUTTON_WIDTH, 20).build());

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> {
            this.saveChanges();
            this.onClose();
        }).bounds((this.width - BUTTON_WIDTH) / 2 + separation - 6, y, BUTTON_WIDTH, 20).build());

        this.menu.addSlotListener(this.listener);
    }

    @Override
    protected void saveChanges() {
        this.saveChanges(Optional.empty(), false);
    }

    public void saveChanges(Optional<String> title, boolean takeBook) {
        if (!this.isModified() && !takeBook) return;
        this.eraseEmptyTrailingPages();
        this.updateLocalCopy();
        NetworkHelper.sendToServer(new ServerBoundSyncLecternBookMessage(this.menu.getPos(),
                this.pages, title.map(String::trim), takeBook));
        this.markSaved();
    }

    private boolean isModified() {
        return !this.pages.equals(this.lastSaved);
    }

    private void markSaved() {
        this.lastSaved.clear();
        this.lastSaved.addAll(this.pages);
    }

    @Override
    public void onClose() {
        this.minecraft.player.closeContainer();
        super.onClose();
    }

    //page turning goes through the server, several players can be looking at the same lectern
    @Override
    protected void pageBack() {
        this.sendButtonClick(1);
    }

    @Override
    protected void pageForward() {
        this.sendButtonClick(2);
    }

    //hack
    private void setPage(int value) {
        while (lastPage != value) {
            if (value > lastPage) {
                lastPage++;
                super.pageForward();
                if (lastPage > this.pages.size()) {
                    for (int i = 0; i < value - this.pages.size(); i++) {
                        this.pages.add("");
                    }
                }
            } else {
                lastPage--;
                super.pageBack();
            }
        }
    }

    private void sendButtonClick(int pageData) {
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, pageData);
    }

    @Override
    public void removed() {
        super.removed();
        this.menu.removeSlotListener(this.listener);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        MultiLineEditBox box = this.pageBox();
        if (box != null && box.isFocused() && event.isAllowedChatCharacter()) {
            StyledTextHelper.insertStyledText(box.textField, event.codepointAsString(),
                    ink.getChatFormatting(), quill.getChatFormatting());
            return true;
        }
        return super.charTyped(event);
    }

    public void onInkClicked() {
        //TODO: restyle the selection
    }

    public void onQuillClicked() {
    }

    private MultiLineEditBox pageBox() {
        return ((BookEditScreenAccessor) this).getPageBox();
    }

    private void refreshPage() {
        if (this.pageBox() == null) return;
        int current = ((BookEditScreenAccessor) this).getCurrentPage();
        while (this.pages.size() <= current) this.pages.add("");
        ((BookEditScreenAccessor) this).callUpdatePageContent();
    }
}
