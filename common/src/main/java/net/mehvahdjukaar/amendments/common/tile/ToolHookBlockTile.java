package net.mehvahdjukaar.amendments.common.tile;

import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.block.ItemDisplayTile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

public class ToolHookBlockTile extends ItemDisplayTile {

    public ToolHookBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.TOOL_HOOK_TILE.get(), pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return Component.literal("tool hook");
    }
}
