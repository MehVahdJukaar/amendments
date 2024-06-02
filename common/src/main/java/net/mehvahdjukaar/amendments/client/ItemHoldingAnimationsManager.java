package net.mehvahdjukaar.amendments.client;

import net.mehvahdjukaar.amendments.client.renderers.CandleHolderRendererExtension;
import net.mehvahdjukaar.amendments.client.renderers.LanternRendererExtension;
import net.mehvahdjukaar.amendments.client.renderers.TorchRendererExtension;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.moonlight.api.item.IThirdPersonSpecialItemRenderer;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.stream.Collectors;

public class ItemHoldingAnimationsManager {

    private static boolean animAdded = false;

    public static void addAnimations() {
        if (animAdded) return;
        animAdded = true;

        if (ClientConfigs.LANTERN_HOLDING.get()) {
            var anim = new LanternRendererExtension();
            BlockScanner.getLanterns()
                    .stream().map(Block::asItem).filter(i -> i != Items.AIR)
                    .collect(Collectors.toSet()).forEach(item ->
                            IThirdPersonSpecialItemRenderer.attachToItem(item, anim));
        }
        if (ClientConfigs.TORCH_HOLDING.get()) {
            var anim = new TorchRendererExtension();
            BlockScanner.getTorches()
                    .stream().map(Block::asItem).filter(i -> i != Items.AIR)
                    .collect(Collectors.toSet()).forEach(item ->
                            IThirdPersonSpecialItemRenderer.attachToItem(item, anim));
        }
        if(ClientConfigs.CANDLE_HOLDER_HOLDING.get()){
            var anim = new CandleHolderRendererExtension();
            BlockScanner.getCandleHolders()
                    .stream().map(Block::asItem).filter(i -> i != Items.AIR)
                    .collect(Collectors.toSet()).forEach(item ->
                            IThirdPersonSpecialItemRenderer.attachToItem(item, anim));
        }
    }
}
