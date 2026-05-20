package net.mehvahdjukaar.amendments.mixins;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {

    @Invoker
    SoundEvent invokeGetSwimSplashSound();

    @Invoker
    SoundEvent invokeGetSwimHighSpeedSplashSound();
}
