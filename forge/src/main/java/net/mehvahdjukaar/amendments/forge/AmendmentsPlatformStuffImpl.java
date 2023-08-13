package net.mehvahdjukaar.amendments.forge;

import net.minecraft.client.renderer.block.model.BakedQuad;

import java.util.Arrays;
import java.util.List;

public class AmendmentsPlatformStuffImpl {
    public static void removeAmbientOcclusion(List<BakedQuad> supportQuads) {
        supportQuads.replaceAll(quad->{
            int[] vertices = quad.getVertices();
            return new BakedQuad(
                    Arrays.copyOf(vertices, vertices.length), quad.getTintIndex(), quad.getDirection(), quad.getSprite(),
                    true, false
            );
        });
    }


}
