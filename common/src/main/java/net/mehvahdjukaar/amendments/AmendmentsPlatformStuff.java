package net.mehvahdjukaar.amendments;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.renderer.block.model.BakedQuad;

import java.util.List;

public class AmendmentsPlatformStuff {
    @ExpectPlatform
    public static void removeAmbientOcclusion(List<BakedQuad> supportQuads) {
        throw new AssertionError();
    }
}
