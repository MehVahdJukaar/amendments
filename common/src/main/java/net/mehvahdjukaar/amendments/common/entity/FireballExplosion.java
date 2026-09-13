package net.mehvahdjukaar.amendments.common.entity;

import net.mehvahdjukaar.amendments.common.network.ClientBoundFireballExplodePacket;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.block.ILightable;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

//this is an expanded explosion that does 5 things more than a normal explosion
//- sets blocks on fire, not just the one it breaks
//- sets entities on fire
//- has a max damage cap
//- does not cause knockback by default
//- different sound and different particles
//done like this instead of fully custom for better compat since we still call the super methods
public class FireballExplosion extends ServerExplosion {

    private final float maxDamage;
    private final boolean hasKnockback;
    private final int onFireSeconds; //same as blaze charge
    private final float soundVolume;

    private final Set<BlockPos> visitedBlocks = new HashSet<>();

    public static void explodeServer(
            Level level,
            @Nullable Entity source,
            @Nullable DamageSource damageSource,
            @Nullable ExplosionDamageCalculator damageCalculator,
            double x, double y, double z,
            float radius,
            boolean fire,
            Level.ExplosionInteraction explosionInteraction
    ) {
        explodeServer(level, source, damageSource, damageCalculator, x, y, z, radius, fire,
                explosionInteraction, new ExtraSettings());
    }

    //same as ServerLevel#explode, except the client gets our packet instead of the vanilla one
    public static void explodeServer(
            Level level,
            @Nullable Entity source,
            @Nullable DamageSource damageSource,
            @Nullable ExplosionDamageCalculator damageCalculator,
            double x, double y, double z,
            float radius,
            boolean fire,
            Level.ExplosionInteraction explosionInteraction,
            ExtraSettings settings
    ) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        Explosion.BlockInteraction blockInteraction = switch (explosionInteraction) {
            case NONE -> Explosion.BlockInteraction.KEEP;
            case BLOCK -> serverLevel.getDestroyType(GameRules.BLOCK_EXPLOSION_DROP_DECAY);
            case MOB -> PlatHelper.isMobGriefingOn(serverLevel, source) ?
                    serverLevel.getDestroyType(GameRules.MOB_EXPLOSION_DROP_DECAY) : Explosion.BlockInteraction.KEEP;
            case TNT -> serverLevel.getDestroyType(GameRules.TNT_EXPLOSION_DROP_DECAY);
            case TRIGGER -> Explosion.BlockInteraction.TRIGGER_BLOCK;
        };

        Vec3 center = new Vec3(x, y, z);
        FireballExplosion explosion = new FireballExplosion(serverLevel, source, damageSource, damageCalculator,
                center, radius, fire, blockInteraction, settings);
        if (ForgeHelper.fireOnExplosionStart(serverLevel, explosion)) return;
        explosion.explode();

        for (ServerPlayer player : serverLevel.players()) {
            if (player.distanceToSqr(center) < 4096.0) {
                NetworkHelper.sendToClientPlayer(player, new ClientBoundFireballExplodePacket(center, radius,
                        Optional.ofNullable(explosion.getHitPlayers().get(player)),
                        ModRegistry.FIREBALL_EXPLOSION_SOUND, settings.soundVolume));
            }
        }
    }

    public FireballExplosion(ServerLevel level, @Nullable Entity source, @Nullable DamageSource damageSource,
                             @Nullable ExplosionDamageCalculator damageCalculator, Vec3 center, float radius,
                             boolean fire, BlockInteraction blockInteraction, ExtraSettings settings) {
        super(level, source, damageSource, damageCalculator, center, radius, fire, blockInteraction);
        this.onFireSeconds = settings.onFireSeconds;
        this.maxDamage = settings.maxDamage;
        this.hasKnockback = settings.hasKnockback;
        this.soundVolume = settings.soundVolume;
    }

    @Override
    public int explode() {
        int blockCount = super.explode();
        if (!hasKnockback) {
            this.getHitPlayers().clear();
        }
        if (this.fire) {
            ServerLevel level = this.level();
            for (BlockPos pos : visitedBlocks) {
                BlockState state = level.getBlockState(pos);
                if (state.getBlock() instanceof ILightable l) {
                    l.lightableInteractWithEntity(level, state, this.getDirectSourceEntity(), pos);
                } else if (state.isAir()) {
                    level.setBlockAndUpdate(pos, BaseFireBlock.getState(level, pos));
                }
            }
        }
        return blockCount;
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
        if (!entity.hurtServer(this.level(), source, amount)) {
            entity.setRemainingFireTicks(oldFire);
            return false;
        }
        return true;
    }

    public void addVisitedBlock(BlockPos pos) {
        visitedBlocks.add(pos);
    }

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
