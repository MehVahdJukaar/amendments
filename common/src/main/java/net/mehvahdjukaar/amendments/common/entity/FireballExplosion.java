package net.mehvahdjukaar.amendments.common.entity;

import net.mehvahdjukaar.amendments.common.network.ClientBoundFireballExplodePacket;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.block.ILightable;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.supplementaries.reg.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

//this is an expanded explosion that does 5 things more than a normal explosion
//- sets blocks on fire, not just the one it breaks
//- sets entities on fire
//- has a max damage cap
//- does not cause knockback by default
//- different sound and different particles
//done like this instead of fully custom for better compat since we still call the super methods
public class FireballExplosion extends Explosion {

    private final float maxDamage;
    private final boolean hasKnockback;
    private final int onFireSeconds; //same as blaze charge
    private final float soundVolume; //same as blaze charge

    private final Set<BlockPos> visitedBlock = new HashSet<>();

    public static FireballExplosion explodeServer(
            Level serverLevel,
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
        return explodeServer(serverLevel, source, damageSource, damageCalculator, x, y, z, radius, fire,
                explosionInteraction, new ExtraSettings());
    }

    //same as server level explode
    public static FireballExplosion explodeServer(
            Level serverLevel,
            @Nullable Entity source,
            @Nullable DamageSource damageSource,
            @Nullable ExplosionDamageCalculator damageCalculator,
            double x,
            double y,
            double z,
            float radius,
            boolean fire,
            Level.ExplosionInteraction explosionInteraction,
            ExtraSettings settings
    ) {
        FireballExplosion explosion = explode(serverLevel, source, damageSource, damageCalculator, x, y, z,
                radius, fire, explosionInteraction, false,
                ModRegistry.FIREBALL_EXPLOSION_SOUND,
                settings);

        if (!(serverLevel instanceof ServerLevel sl)) {
            return explosion;
        }
        if (!explosion.interactsWithBlocks()) {
            explosion.clearToBlow();
        }

        for (ServerPlayer serverPlayer : sl.players()) {
            if (serverPlayer.distanceToSqr(x, y, z) < 4096.0) {
                NetworkHelper.sendToClientPlayer(serverPlayer,
                        new ClientBoundFireballExplodePacket(x, y, z, radius, explosion.getToBlow(),
                                explosion.getHitPlayers().get(serverPlayer),
                                explosion.getBlockInteraction(), explosion.getSmallExplosionParticles(),
                                explosion.getLargeExplosionParticles(), explosion.getExplosionSound(),
                                explosion.soundVolume));
            }
        }

        return explosion;
    }

    // same as Level.explode
    public static FireballExplosion explode(
            Level level,
                                     @Nullable Entity source,
            @Nullable DamageSource damageSource,
            @Nullable ExplosionDamageCalculator damageCalculator,
            double x, double y, double z,
            float radius, boolean fire,
            Level.ExplosionInteraction explosionInteraction,
            boolean spawnParticles,
            Holder<SoundEvent> explosionSound,
            ExtraSettings settings) {
        Explosion.BlockInteraction inter;
        switch (explosionInteraction) {
            case NONE -> inter = BlockInteraction.KEEP;
            case BLOCK -> inter = level.getDestroyType(GameRules.RULE_BLOCK_EXPLOSION_DROP_DECAY);
            case MOB -> inter = level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) ? level.getDestroyType(GameRules.RULE_MOB_EXPLOSION_DROP_DECAY) : BlockInteraction.KEEP;
            case TNT -> inter = level.getDestroyType(GameRules.RULE_TNT_EXPLOSION_DROP_DECAY);
            case TRIGGER -> inter = BlockInteraction.TRIGGER_BLOCK;
            default -> throw new MatchException(null, null);
        }

        Explosion.BlockInteraction blockInteraction = inter;
        FireballExplosion explosion = new FireballExplosion(level, source, damageSource, damageCalculator, x, y, z, radius,
                fire, blockInteraction,
                ParticleTypes.EXPLOSION_EMITTER, ParticleTypes.EXPLOSION_EMITTER, //unused
                explosionSound, settings);
        explosion.explode();
        explosion.finalizeExplosion(spawnParticles);
        return explosion;
    }


    public FireballExplosion(Level level, @Nullable Entity source, double toBlowX, double toBlowY, double toBlowZ,
                             float radius, List<BlockPos> positions, BlockInteraction blockInteraction,
                             ParticleOptions smallExplosionParticles,
                             ParticleOptions largeExplosionParticles, Holder<SoundEvent> explosionSound,
                             ExtraSettings settings) {
        super(level, source, toBlowX, toBlowY, toBlowZ, radius, positions,
                blockInteraction, smallExplosionParticles, largeExplosionParticles, explosionSound);
        this.onFireSeconds = settings.onFireSeconds;
        this.maxDamage = settings.maxDamage;
        this.hasKnockback = settings.hasKnockback;
        this.soundVolume = settings.soundVolume;

    }

    public FireballExplosion(Level level, @Nullable Entity source, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator damageCalculator,
                             double toBlowX, double toBlowY, double toBlowZ,
                             float radius, boolean fire, BlockInteraction blockInteraction,
                             ParticleOptions smallExplosionParticles,
                             ParticleOptions largeExplosionParticles, Holder<SoundEvent> explosionSound,
                             ExtraSettings settings) {
        super(level, source, damageSource, damageCalculator, toBlowX, toBlowY, toBlowZ, radius, fire, blockInteraction,
                smallExplosionParticles, largeExplosionParticles, explosionSound);
        this.onFireSeconds = settings.onFireSeconds;
        this.maxDamage = settings.maxDamage;
        this.hasKnockback = settings.hasKnockback;
        this.soundVolume = settings.soundVolume;
    }

    //remove knockback on client by clearing the list
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
            this.level.addParticle(ModRegistry.FIREBALL_EMITTER_PARTICLE.get(), this.x, this.y, this.z,
                    radius, 0.0, 0.0);
        }

        if (!this.fire) return;

        for (BlockPos pos : visitedBlock) {
            //sets block on fire
            BlockState state = this.level.getBlockState(pos);
            if (state.getBlock() instanceof ILightable l) {
                l.lightableInteractWithEntity(level, state, this.getDirectSourceEntity(), pos);
            } else if (state.getBlock() == Blocks.AIR) {
                this.level.setBlockAndUpdate(pos, BaseFireBlock.getState(this.level, pos));
            }
        }
    }

    //for knockback on server
    public boolean hasKnockback() {
        return hasKnockback;
    }


    public boolean hurtHitEntity(Entity entity, DamageSource source, float amount) {
        if (this.maxDamage > 0 && amount > this.maxDamage) {
            amount = this.maxDamage;
        }
        int oldFire = entity.getRemainingFireTicks();
        entity.igniteForSeconds(onFireSeconds); //same as blaze charge
        if (!entity.hurt(source, amount)) {
            entity.setRemainingFireTicks(oldFire);
            return false;
        }

        return true;
    }

    public void addVisitedBlock(BlockPos pos, BlockState state) {
        visitedBlock.add(pos);
    }

    //so basically the only reason w
    public float getExplosionVolume() {
        return soundVolume;
    }

    public static class ExtraSettings {
        public float soundVolume = 4.0f;
        public float maxDamage = Float.MAX_VALUE; //max damage cap
        public int onFireSeconds = 0; //same as blaze charge
        public boolean hasKnockback = false; //if the explosion should have knockback
    }

}
