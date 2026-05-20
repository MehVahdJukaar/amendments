package net.mehvahdjukaar.amendments.mixins;

import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SignRenderer.class)
public interface SignRendererAccessor {

    @Invoker
    Material invokeGetSignMaterial(WoodType woodType);

    @Invoker
    Vec3 invokeGetTextOffset();
}
