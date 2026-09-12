package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.client.renderers.ExtendedSignRenderState;
import net.mehvahdjukaar.amendments.client.renderers.HangingSignRenderData;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SignRenderState.class)
public class SignRenderStateMixin implements ExtendedSignRenderState {

    @Unique
    private boolean amendments$pixelConsistent;
    @Unique
    private HangingSignRenderData amendments$data;

    @Override
    public boolean amendments$isPixelConsistent() {
        return amendments$pixelConsistent;
    }

    @Override
    public void amendments$setPixelConsistent(boolean pixelConsistent) {
        this.amendments$pixelConsistent = pixelConsistent;
    }

    @Override
    public HangingSignRenderData amendments$getData() {
        if (amendments$data == null) amendments$data = new HangingSignRenderData();
        return amendments$data;
    }
}
