package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.Dummy;
import net.mehvahdjukaar.amendments.common.ProjectileStats;
import net.mehvahdjukaar.amendments.common.entity.MediumFireball;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FireChargeItem.class)
public abstract class FireChargeMixin extends Item implements ProjectileItem {

    public FireChargeMixin(Properties properties) {
        super(properties);
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        if (!CommonConfigs.THROWABLE_FIRE_CHARGES.get()) {
            //ugly
            RandomSource randomSource = level.getRandom();
            double d = randomSource.triangle(direction.getStepX(), 0.11485000000000001);
            double e = randomSource.triangle(direction.getStepY(), 0.11485000000000001);
            double f = randomSource.triangle(direction.getStepZ(), 0.11485000000000001);
            Vec3 vec3 = new Vec3(d, e, f);
            SmallFireball smallFireball = new SmallFireball(level, pos.x(), pos.y(), pos.z(), vec3.normalize());
            smallFireball.setItem(stack);
            return smallFireball;
        }
        MediumFireball snowball = new MediumFireball(level, pos.x(), pos.y(), pos.z());
        snowball.setItem(stack);
        return snowball;
    }

    @Override
    public void shoot(Projectile projectile, double x, double y, double z, float velocity, float inaccuracy) {
        if (CommonConfigs.THROWABLE_FIRE_CHARGES.get()) {
            projectile.shoot(x, y, z, velocity, inaccuracy);
        }
    }

    @Override
    public DispenseConfig createDispenseConfig() {
        if (!Dummy.MOD_LOADED || !CommonConfigs.THROWABLE_FIRE_CHARGES.get()) {
            return DispenseConfig.builder().positionFunction((blockSource, direction) -> {
                return DispenserBlock.getDispensePosition(blockSource, 1.0, Vec3.ZERO);
            }).uncertainty(6.6666665F).power(1.0F).overrideDispenseEvent(1018).build();
        }
        return ProjectileStats.DISPENSER_CONFIG;
    }
}
