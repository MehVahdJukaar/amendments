package net.mehvahdjukaar.amendments.integration.fabric;

import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.moonlight.api.client.gui.LinkButton;
import net.mehvahdjukaar.moonlight.api.platform.configs.fabric.FabricConfigListScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

public class ModConfigSelectScreen extends FabricConfigListScreen {

    public ModConfigSelectScreen(Screen parent) {
        super(Amendments.MOD_ID, Items.OAK_HANGING_SIGN.getDefaultInstance(),
                Component.literal("§6Amendments Configs"), new ResourceLocation("textures/block/deepslate_tiles.png"),
                parent, ClientConfigs.SPEC, CommonConfigs.SPEC);
    }

    @Override
    protected void addExtraButtons() {

        int y = this.height - 27;
        int centerX = this.width / 2;

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, (button) -> this.minecraft.setScreen(this.parent))
                .bounds(centerX - 45, y, 90, 20).build());

        this.addRenderableWidget(LinkButton.create(this, centerX - 45 - 22, y, 3, 1,
                "https://www.patreon.com/user?u=53696377", "Support me on Patreon :D"));

        this.addRenderableWidget(LinkButton.create(this, centerX - 45 - 22 * 2, y, 2, 2,
                "https://ko-fi.com/mehvahdjukaar", "Donate a Coffee"));

        this.addRenderableWidget(LinkButton.create(this, centerX - 45 - 22 * 3, y, 1, 2,
                "https://www.curseforge.com/minecraft/mc-mods/amendments", "CurseForge Page"));

        this.addRenderableWidget(LinkButton.create(this, centerX - 45 - 22 * 4, y, 0, 2,
                "https://github.com/MehVahdJukaar/Supplementaries/wiki/amdnemdnets", "Mod Wiki"));


        this.addRenderableWidget(LinkButton.create(this, centerX + 45 + 2, y, 1, 1,
                "https://discord.com/invite/qdKRTDf8Cv", "Mod Discord"));

        this.addRenderableWidget(LinkButton.create(this, centerX + 45 + 2 + 22, y, 0, 1,
                "https://www.youtube.com/watch?v=LSPNAtAEn28&t=1s", "Youtube Channel"));

        this.addRenderableWidget(LinkButton.create(this, centerX + 45 + 2 + 22 * 2, y, 2, 1,
                "https://twitter.com/Supplementariez?s=09", "Twitter Page"));

        this.addRenderableWidget(LinkButton.create(this, centerX + 45 + 2 + 22 * 3, y, 3, 2,
                "https://www.akliz.net/supplementaries", "Need a server? Get one with Akliz"));

    }

}
