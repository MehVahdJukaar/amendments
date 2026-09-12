package net.mehvahdjukaar.amendments.mixins;

import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractSignRenderer.class)
public interface SignRendererAccessor {

    @Invoker
    SpriteId invokeGetSignSprite(WoodType woodType);
}
