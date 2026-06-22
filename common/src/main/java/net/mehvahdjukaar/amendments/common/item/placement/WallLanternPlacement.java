package net.mehvahdjukaar.amendments.common.item.placement;

import net.mehvahdjukaar.amendments.common.block.WallLanternBlock;
import net.mehvahdjukaar.amendments.integration.CompatHandler;
import net.mehvahdjukaar.moonlight.api.item.additional_placements.AdditionalItemPlacement;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;

public class WallLanternPlacement extends AdditionalItemPlacement {

    public WallLanternPlacement(WallLanternBlock wallLantern) {
        super(wallLantern);
    }

    @Override
    public InteractionResult overridePlace(BlockPlaceContext pContext) {
        if (CompatHandler.TORCHSLAB) {
            double y = pContext.getClickLocation().y() % 1;
            if (y < 0.5) return null;
        }
        return super.overridePlace(pContext);
    }
}
