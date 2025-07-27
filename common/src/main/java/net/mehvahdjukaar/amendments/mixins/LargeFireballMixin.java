package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LargeFireball.class)
public abstract class LargeFireballMixin extends Entity {

    public LargeFireballMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }//TODO:


}
