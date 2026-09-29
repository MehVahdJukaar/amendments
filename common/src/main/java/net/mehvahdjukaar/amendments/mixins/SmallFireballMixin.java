package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SmallFireball.class)
public abstract class SmallFireballMixin extends Entity {

    public SmallFireballMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    //pickable so the player can actually hit it. PlayerMixin lets the punch deflect it
    @Override
    public boolean isPickable() {
        if (this.getType() == EntityType.SMALL_FIREBALL && CommonConfigs.DEFLECT_FIRE_CHARGES.get()) {
            return true;
        }
        return super.isPickable();
    }

}
