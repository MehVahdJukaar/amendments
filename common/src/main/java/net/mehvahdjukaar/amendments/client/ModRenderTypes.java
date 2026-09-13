package net.mehvahdjukaar.amendments.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.mehvahdjukaar.amendments.Amendments;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

public class ModRenderTypes {

    //entity cutout that ignores both the lightmap and directional shading
    public static final RenderPipeline ENTITY_LIT_PIPELINE = RenderPipeline.builder(RenderPipelines.ENTITY_EMISSIVE_SNIPPET)
            .withLocation(Amendments.res("pipeline/entity_lit"))
            .withShaderDefine("ALPHA_CUTOUT", 0.1F)
            .withShaderDefine("NO_CARDINAL_LIGHTING")
            .withShaderDefine("NO_OVERLAY")
            .withCull(false)
            .build();

    public static final Function<Identifier, RenderType> ENTITY_LIT = Util.memoize(
            texture -> RenderType.create("entity_lit", RenderSetup.builder(ENTITY_LIT_PIPELINE)
                    .withTexture("Sampler0", texture)
                    .affectsCrumbling()
                    .createRenderSetup()));
}
