package net.mehvahdjukaar.amendments.client.renderers;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import org.jetbrains.annotations.Nullable;

public interface ExtendedBellRenderState {

    @Nullable
    BlockStateModel amendments$getConnectionModel();

    void amendments$setConnectionModel(@Nullable BlockStateModel model);
}
