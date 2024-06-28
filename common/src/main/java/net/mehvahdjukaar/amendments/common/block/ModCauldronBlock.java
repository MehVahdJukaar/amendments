package net.mehvahdjukaar.amendments.common.block;

import net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public abstract class ModCauldronBlock extends AbstractCauldronBlock implements EntityBlock {

    public ModCauldronBlock(BlockBehaviour.Properties properties) {
        super(properties, Map.of());
    }

    @Override
    public Item asItem() {
        return Items.CAULDRON;
    }

    public abstract IntegerProperty getLevelProperty();

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return state.getValue(getLevelProperty());
    }

    @Override
    public void handlePrecipitation(BlockState state, Level level, BlockPos pos, Biome.Precipitation precipitation) {
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LiquidCauldronBlockTile(pos, state);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (isEntityInsideContent(state, pos, entity)) {
            if (level.isClientSide && level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile tile) {
                int color = tile.getSoftFluidTank().getCachedParticleColor(level, pos);
                int light = tile.getSoftFluidTank().getFluidValue().getEmissivity();
                playSplashAnimation(level, pos, entity, getContentHeight(state), color, light);
            }
            super.fallOn(level, state, pos, entity, 0);
        } else super.fallOn(level, state, pos, entity, fallDistance);
    }

    public static void playSplashAnimation(Level level, BlockPos pos, Entity e, double waterLevel, int color, int light) {
        Entity feetEntity = e.isVehicle() && e.getControllingPassenger() != null ? e.getControllingPassenger() : e;
        float offset = feetEntity == e ? 0.2F : 0.9F;
        Vec3 movement = feetEntity.getDeltaMovement();
        RandomSource rand = level.random;
        float speed = Math.min(1.0F, (float) Math.sqrt(movement.x * movement.x * 0.2 + movement.y * movement.y + movement.z * movement.z * 0.2) * offset);
        if (speed < 0.25F) {
            level.playLocalSound(e.getX(), e.getY(), e.getZ(),
                    e.getSwimSplashSound(), e.getSoundSource(),
                    speed, 1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.4F, false);
        } else {
            level.playLocalSound(e.getX(), e.getY(), e.getZ(),
                    e.getSwimHighSpeedSplashSound(), e.getSoundSource(),
                    speed, 1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.4F, false);
        }

        double surface = pos.getY() + waterLevel;

        float radius = 1.5f;
        float width = e.getBbWidth();

        spawnSplashParticles(level, e, pos, rand, surface, color, light,
                ModRegistry.BOILING_PARTICLE.get(), radius, width);

        spawnSplashParticles(level, e, pos, rand, surface, color, light,
                ModRegistry.SPLASH_PARTICLE.get(), radius, width);

        e.gameEvent(GameEvent.SPLASH);
    }

    private static void spawnSplashParticles(Level level, Entity e, BlockPos pos,
                                             RandomSource rand, double surface,
                                             int color, int light,
                                             ParticleOptions particleOptions,
                                             float radius, float width) {
        float mx = pos.getX() + 0.125f;
        float Mx = pos.getX() + 1 - 0.125f;
        float mz = pos.getZ() + 0.125f;
        float Mz = pos.getZ() + 1 - 0.125f;

        double z;
        double x;
        for (int i = 0; i < 1.0F + width * 20.0F; ++i) {
            x = e.getX() + (rand.nextDouble() - 0.5) * width * radius;
            z = e.getZ() + (rand.nextDouble() - 0.5) * width * radius;
            if (x >= mx && x <= Mx && z >= mz && z <= Mz) {
                level.addParticle(particleOptions,
                        x, surface, z, color, surface, light);
            }
        }
    }


    public static void playExtinguishSound(Level level, BlockPos pos, Entity entity) {
        level.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, entity.getSoundSource(),
                0.7F, 1.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.4F);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (isEntityInsideContent(state, pos, entity)) {
            entity.wasTouchingWater = true;

            if (level.isClientSide) return;

            if (entity.isOnFire()) {
                entity.clearFire();
                playExtinguishSound(level, pos, entity);
                if (entity.mayInteract(level, pos)) {
                    if (level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile te) {
                        te.consumeOneLayer();
                    }
                }
            }
            handleEntityInside(state, level, pos, entity);
        }
    }


    protected abstract void handleEntityInside(BlockState state, Level level, BlockPos pos, Entity entity);

    public boolean doCraftItem(Level level, BlockPos pos, Player player, InteractionHand hand,
                               SoftFluidStack fluid, ItemStack itemStack, ItemStack crafted,
                               float layerPerItem, int itemCountMultiplier) {

        int maxRecolorable = (int) (crafted.getCount() * itemCountMultiplier * fluid.getCount() / layerPerItem);
        int amountToRecolor = Math.min(maxRecolorable, itemStack.getCount());
        if (amountToRecolor <= 0) return false;
        crafted.setCount(amountToRecolor);

        level.playSound(player, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.3f);

        if (player instanceof ServerPlayer serverPlayer) {
            player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, itemStack);
        } else return true;

        if (!player.isCreative()) {
            itemStack.shrink(amountToRecolor);
            fluid.shrink(Mth.ceil((layerPerItem * amountToRecolor / (float) itemCountMultiplier)));
        }

        if (itemStack.isEmpty()) {
            player.setItemInHand(hand, crafted);
        } else {
            if (!player.getInventory().add(crafted)) {
                player.drop(crafted, false);
            }
        }
        return true;
    }

    public abstract BlockState updateStateOnFluidChange(BlockState state, Level level, BlockPos pos, SoftFluidStack fluid);

    public static void addSurfaceParticles(ParticleOptions type, Level level, BlockPos pos, int count, double surface, RandomSource rand,
                                           float r, float g, float b) {
        for (int i = 0; i < count; i++) {
            double x = pos.getX() + 0.1875D + (rand.nextFloat() * 0.625D);
            double y = pos.getY() + surface;
            double z = pos.getZ() + 0.1875D + (rand.nextFloat() * 0.625D);
            level.addParticle(type, x, y, z, r, g, b);
        }
    }

}
