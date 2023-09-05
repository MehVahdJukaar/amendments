package net.mehvahdjukaar.amendments;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.world.item.DyeableLeatherItem;

import java.util.List;

public class AmendmentsPlatformStuff {
    @ExpectPlatform
    public static List<BakedQuad> removeAmbientOcclusion(List<BakedQuad> supportQuads) {
        throw new AssertionError();
    }
    @ExpectPlatform
    public static SoftFluidTank createTank(int capacity){
        throw new AssertionError();
    }
}
