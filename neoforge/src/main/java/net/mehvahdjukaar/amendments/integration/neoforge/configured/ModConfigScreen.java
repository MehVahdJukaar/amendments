package net.mehvahdjukaar.amendments.integration.neoforge.configured;


import com.mrcrayfish.configured.api.IModConfig;
import net.mehvahdjukaar.moonlight.api.integration.configured.CustomConfigScreen;
import net.mehvahdjukaar.moonlight.api.integration.configured.CustomConfigSelectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.HashMap;
import java.util.Map;

//credits to MrCrayfish's Configured Mod
public class ModConfigScreen extends CustomConfigScreen {

    private static final Map<String, ItemStack> CUSTOM_ICONS = new HashMap<>();

    static {
        addIcon("general", Items.BOOKSHELF);
        addIcon("jukebox", Items.JUKEBOX);
        addIcon("arrows", Items.ARROW);
        addIcon("brewing stand", Items.BREWING_STAND);
        addIcon("bell", Items.BELL);
        addIcon("cauldron", Items.CAULDRON);
        addIcon("lantern", Items.LANTERN);
        addIcon("lily pad", Items.LILY_PAD);
        addIcon("misc", Items.LAPIS_LAZULI);
        addIcon("banners", Items.RED_BANNER);
        addIcon("cake", Items.CAKE);
        addIcon("carpets", Items.ORANGE_CARPET);
        addIcon("flower pot", Items.FLOWER_POT);
        addIcon("mob head", Items.SKELETON_SKULL);
        addIcon("hanging sign", Items.SPRUCE_HANGING_SIGN);
    }

    protected ModConfigScreen(String modId, ItemStack icon, Component title, Screen parent, IModConfig config) {
        super(modId, icon, title, parent, config);
        this.icons.putAll(CUSTOM_ICONS);
    }

    public ModConfigScreen(CustomConfigSelectScreen customConfigSelectScreen, IModConfig iModConfig) {
        super(customConfigSelectScreen, iModConfig);
        this.icons.putAll(CUSTOM_ICONS);
    }

    private static void addIcon(String s, ItemLike i) {
        CUSTOM_ICONS.put(s, i.asItem().getDefaultInstance());
    }


    @Override
    public void onSave() {
    }

    @Override
    public Factory getSubScreenFactory() {
        return ModConfigScreen::new;
    }

}