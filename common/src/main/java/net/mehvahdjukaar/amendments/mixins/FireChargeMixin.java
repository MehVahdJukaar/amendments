package net.mehvahdjukaar.amendments.mixins;

import net.mehvahdjukaar.amendments.common.ProjectileStats;
import net.mehvahdjukaar.amendments.common.entity.MediumFireball;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FireChargeItem.class)
public class FireChargeMixin implements ProjectileItem {

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        MediumFireball snowball = new MediumFireball(level, pos.x(), pos.y(), pos.z());
        snowball.setItem(stack);
        return snowball;
    }

    @Override
    public DispenseConfig createDispenseConfig() {
        return ProjectileStats.DISPENSER_CONFIG;
    }
}
