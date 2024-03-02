package net.mehvahdjukaar.amendments.common.item.placement;

import net.mehvahdjukaar.amendments.integration.CompatHandler;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.item.additional_placements.AdditionalItemPlacement;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;

public class WallLanternPlacement extends AdditionalItemPlacement {

    public WallLanternPlacement() {
        super(ModRegistry.WALL_LANTERN.get());
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
