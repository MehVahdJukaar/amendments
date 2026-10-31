package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.client.TumblingAnimation;
import net.mehvahdjukaar.amendments.common.ProjectileStats;
import net.mehvahdjukaar.amendments.common.entity.IVisualTransformationProvider;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;

@Pseudo
@Mixin(targets = "com.teamabnormals.environmental.common.entity.projectile.ThrownMudBall")
public abstract class CompatEnvironmentalMudBallMixin extends ThrowableItemProjectile implements IVisualTransformationProvider {

    @Unique
    private final TumblingAnimation amendments$tumblingAnimation = ProjectileStats.makeFasterTumbler();

    protected CompatEnvironmentalMudBallMixin(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public Matrix4f amendments$getVisualTransformation(float partialTicks) {
        return new Matrix4f().rotate(this.amendments$tumblingAnimation.getRotation(partialTicks));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide && ClientConfigs.PROJECTILE_TUMBLE.get()) {
            amendments$tumblingAnimation.tick(random);
        }
    }
}
