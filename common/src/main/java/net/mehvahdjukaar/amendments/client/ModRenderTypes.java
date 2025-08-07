package net.mehvahdjukaar.amendments.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.mehvahdjukaar.amendments.Amendments;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public abstract class ModRenderTypes extends RenderType{
    public ModRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }
    // clone of render type TEXT with equivalent shader in vanilla (using crumbling) because iris hard crashes the game when this is used...
    public static final Function<ResourceLocation, RenderType> ENTITY_LIT = Util.memoize(
            resourceLocation -> RenderType.create(
                    Amendments.res("entity_lit").toString(),
                    DefaultVertexFormat.POSITION_COLOR_TEX,
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
