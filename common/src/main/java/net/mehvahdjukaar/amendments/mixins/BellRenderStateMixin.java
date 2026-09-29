package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.client.renderers.ExtendedBellRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.blockentity.state.BellRenderState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BellRenderState.class)
public class BellRenderStateMixin implements ExtendedBellRenderState {

    @Unique
    private BlockStateModel amendments$connectionModel;

    @Override
    public @Nullable BlockStateModel amendments$getConnectionModel() {
        return amendments$connectionModel;
    }

    @Override
    public void amendments$setConnectionModel(@Nullable BlockStateModel model) {
        this.amendments$connectionModel = model;
    }
}
