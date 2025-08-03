package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.common.ProjectileStats;
import net.mehvahdjukaar.amendments.client.ElasticAnimation;
import net.mehvahdjukaar.amendments.common.entity.IVisualTransformationProvider;
import net.mehvahdjukaar.amendments.client.TumblingAnimation;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.supplementaries.common.entities.SlimeBallEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(SlimeBallEntity.class)
public abstract class SlimeBallMixin extends ThrowableItemProjectile implements IVisualTransformationProvider {


    @Unique
    private final TumblingAnimation amendments$tumblingAnimation = ProjectileStats.makeFasterTumbler();
    @Unique
    private final ElasticAnimation amendments$elasticAnimation = new ElasticAnimation();

    public SlimeBallMixin(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "bounce", at = @At("HEAD"), remap = false)
    public void amendments$addSquish(Vec3 newVel, CallbackInfo ci) {
        amendments$elasticAnimation.setSquishedDown(); // 1 second of squish
    }


    @Override
    public Matrix4f amendments$getVisualTransformation(float partialTicks) {
        Matrix4f mat = new Matrix4f();
        //apply squish
        mat.scale(amendments$elasticAnimation.getScale(partialTicks));
        return mat.rotate(this.amendments$tumblingAnimation.getRotation(partialTicks));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            amendments$elasticAnimation.tick(this.getDeltaMovement());
            if (ClientConfigs.PROJECTILE_TUMBLE.get()) amendments$tumblingAnimation.tick(random);
        }
    }

}
