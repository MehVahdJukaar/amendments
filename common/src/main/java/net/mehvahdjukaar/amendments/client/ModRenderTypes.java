package net.mehvahdjukaar.amendments.client;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.mehvahdjukaar.amendments.Amendments;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public abstract class ModRenderTypes extends RenderType {
    public ModRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }


    public static final VertexFormat POSITION_COLOR_TEX = VertexFormat.builder()
            .add("Position", VertexFormatElement.POSITION)
            .add("Color", VertexFormatElement.COLOR)
            .add("UV0", VertexFormatElement.UV0)
            .build();

    // clone of render type TEXT with equivalent shader in vanilla (using crumbling) because iris hard crashes the game when this is used...
    public static final Function<ResourceLocation, RenderType> ENTITY_LIT = Util.memoize(
            resourceLocation -> RenderType.create(
                    Amendments.res("entity_lit").toString(),
                    POSITION_COLOR_TEX,
                    VertexFormat.Mode.QUADS,
                    256,
                    true,
                    false,
                    CompositeState.builder()
                            .setShaderState(RENDERTYPE_CRUMBLING_SHADER)//because it isnt lit
                            .setTextureState(new TextureStateShard(resourceLocation, false, false))
                            .setTransparencyState(NO_TRANSPARENCY)
                            .setOverlayState(OVERLAY)
                            .createCompositeState(true)
            )
    );


}
