package net.mehvahdjukaar.amendments.forge;

import net.minecraft.client.renderer.block.model.BakedQuad;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AmendmentsPlatformStuffImpl {
    public static List<BakedQuad> removeAmbientOcclusion(List<BakedQuad> supportQuads) {
        var newList = new ArrayList<BakedQuad>();
        for(var quad : supportQuads){
            int[] vertices = quad.getVertices();
            newList.add( new BakedQuad(
                    Arrays.copyOf(vertices, vertices.length), quad.getTintIndex(), quad.getDirection(), quad.getSprite(),
                    true, false
            ));
        };
        return newList;
    }


}
