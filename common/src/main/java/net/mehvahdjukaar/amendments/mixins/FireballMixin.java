package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

// just here because it annoyed me that you can pick these
@Mixin(Fireball.class)
public abstract class FireballMixin extends Entity {

    public FireballMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow public abstract ItemStack getItem();

    @Override
    public @Nullable ItemStack getPickResult() {
        return getItem().copyWithCount(1);
    }

    @Override
    public boolean displayFireAnimation() {
        if (level().isClientSide &&
                (this.getType() == EntityType.FIREBALL || this.getType() == EntityType.SMALL_FIREBALL) &&
                ClientConfigs.FIREBALL_3D.get()) {
            return false;
        }
        return super.displayFireAnimation();
    }
}
