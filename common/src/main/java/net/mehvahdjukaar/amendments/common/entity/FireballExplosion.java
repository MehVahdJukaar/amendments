package net.mehvahdjukaar.amendments.common.entity;

import net.mehvahdjukaar.amendments.common.network.ClientBoundFireballExplodePacket;
import net.mehvahdjukaar.amendments.common.network.ModNetwork;
import net.mehvahdjukaar.moonlight.api.block.ILightable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

//this is an expanded explosion that does 5 things more than a normal explosion
//- sets blocks on fire, not just the one it breaks
//- sets entities on fire
//- has a max damage cap
//- does not cause knockback by default
//- different sound and different particles
//done like this instead of fully custom for better compat since we still call the super methods
public class FireballExplosion extends Explosion {

    public float maxDamage = Float.MAX_VALUE;
    public boolean hasKnockback = false;

    //same as server level explode
    public static FireballExplosion explode(
            ServerLevel serverLevel,
            @Nullable Entity source,
            @Nullable DamageSource damageSource,
            @Nullable ExplosionDamageCalculator damageCalculator,
            double x,
            double y,
            double z,
            float radius,
            boolean fire,
            Level.ExplosionInteraction explosionInteraction
    ) {
        FireballExplosion explosion = explode(serverLevel, source, damageSource, damageCalculator, x, y, z, radius, fire, explosionInteraction, false);
        if (!explosion.interactsWithBlocks()) {
            explosion.clearToBlow();
        }

        for (ServerPlayer serverPlayer : serverLevel.players()) {
            if (serverPlayer.distanceToSqr(x, y, z) < 4096.0) {
                ModNetwork.CHANNEL.sendToClientPlayer(serverPlayer,
                        new ClientBoundFireballExplodePacket(x, y, z, radius, explosion.getToBlow(),
                                explosion.getHitPlayers().get(serverPlayer)));
            }
        }

        return explosion;
    }

    //same as level explode
    public static FireballExplosion explode(
            Level level,
            @Nullable Entity source,
            @Nullable DamageSource damageSource,
            @Nullable ExplosionDamageCalculator damageCalculator,
            double x,
            double y,
            double z,
            float radius,
            boolean fire,
            Level.ExplosionInteraction explosionInteraction,
            boolean spawnParticles
    ) {
        Explosion.BlockInteraction blockInteraction = switch (explosionInteraction) {
            case NONE -> Explosion.BlockInteraction.KEEP;
            case BLOCK -> level.getDestroyType(GameRules.RULE_BLOCK_EXPLOSION_DROP_DECAY);
            case MOB -> level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)
                    ? level.getDestroyType(GameRules.RULE_MOB_EXPLOSION_DROP_DECAY)
                    : Explosion.BlockInteraction.KEEP;
            case TNT -> level.getDestroyType(GameRules.RULE_TNT_EXPLOSION_DROP_DECAY);
        };
        FireballExplosion explosion = new FireballExplosion(level, source, damageSource, damageCalculator, x, y, z, radius, fire, blockInteraction);
        explosion.explode();
        explosion.finalizeExplosion(spawnParticles);
        return explosion;
    }

    public FireballExplosion(Level level, @Nullable Entity source, double toBlowX, double toBlowY, double toBlowZ, float radius, List<BlockPos> positions) {
        super(level, source, toBlowX, toBlowY, toBlowZ, radius, positions);
    }

    public FireballExplosion(Level level, @Nullable Entity source, double toBlowX, double toBlowY, double toBlowZ, float radius, boolean fire, BlockInteraction blockInteraction, List<BlockPos> positions) {
        super(level, source, toBlowX, toBlowY, toBlowZ, radius, fire, blockInteraction, positions);
    }

    public FireballExplosion(Level level, @Nullable Entity source, double toBlowX, double toBlowY, double toBlowZ, float radius, boolean fire, BlockInteraction blockInteraction) {
        super(level, source, toBlowX, toBlowY, toBlowZ, radius, fire, blockInteraction);
    }

    public FireballExplosion(Level level, @Nullable Entity source, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator damageCalculator, double toBlowX, double toBlowY, double toBlowZ, float radius, boolean fire, BlockInteraction blockInteraction) {
        super(level, source, damageSource, damageCalculator, toBlowX, toBlowY, toBlowZ, radius, fire, blockInteraction);
    }

    @Override
    public void explode() {
        super.explode();
        if (!hasKnockback) {
            this.getHitPlayers().clear();
        }
    }

    @Override
    public void finalizeExplosion(boolean spawnParticles) {
        super.finalizeExplosion(false);

        //spawn our own particles
        if (spawnParticles) {

        }
    }

    public boolean hasKnockback() {
        return hasKnockback;
    }


    public boolean hurtHitEntity(Entity entity, DamageSource source, float amount) {
        if (this.maxDamage > 0 && amount > this.maxDamage) {
            amount = this.maxDamage;
        }
        //TODO: set on fire here
        return entity.hurt(source, amount);
    }

    public void setBlockOnFire(BlockPos pos, BlockState state) {
        if (!this.fire) return;
        //sets block on fire
        if (state.getBlock() instanceof ILightable l) {
            l.interactWithEntity(level, state, this.getDirectSourceEntity(), pos);
        }

    }
}
