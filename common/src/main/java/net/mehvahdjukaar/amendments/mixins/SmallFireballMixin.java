package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
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

    //returning true makes the player attack push it around
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return true;
    }

    @Override
    public boolean hurtClient(DamageSource source) {
        return true;
    }

    @Override
    public boolean isPickable() {
        if(this.getType() == EntityType.SMALL_FIREBALL &&
                CommonConfigs.DEFLECT_FIRE_CHARGES.get()){
            return false;
        }
        return super.isPickable();
    }

}
