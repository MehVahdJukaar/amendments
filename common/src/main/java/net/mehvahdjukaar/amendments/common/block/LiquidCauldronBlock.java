package net.mehvahdjukaar.amendments.common.block;

import net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile;
import net.mehvahdjukaar.amendments.reg.ModBlockProperties;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.amendments.reg.ModTags;
import net.mehvahdjukaar.moonlight.api.client.util.ColorUtil;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.util.PotionNBTHelper;
import net.mehvahdjukaar.moonlight.api.util.math.ColorUtils;
import net.mehvahdjukaar.moonlight.api.util.math.colors.HSLColor;
import net.mehvahdjukaar.moonlight.api.util.math.colors.RGBColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class LiquidCauldronBlock extends AbstractCauldronBlock implements EntityBlock {
    public static final IntegerProperty LEVEL = ModBlockProperties.LEVEL_1_4;
    public static final IntegerProperty LIGHT_LEVEL = ModBlockProperties.LIGHT_LEVEL;
    public static final BooleanProperty BOILING = ModBlockProperties.BOILING;

    public LiquidCauldronBlock(Properties properties) {
        super(properties, Map.of());
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(LEVEL, 1).setValue(LIGHT_LEVEL, 0).setValue(BOILING, false));
        //lingering pots can be consumed
    }


    @Override
    public Item asItem() {
        return Items.CAULDRON;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return state.getValue(LEVEL);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LEVEL, LIGHT_LEVEL, BOILING);
    }

    @Override
    protected boolean canReceiveStalactiteDrip(Fluid fluid) {
        return fluid != Fluids.WATER && fluid != Fluids.LAVA;
    }

    @Override
    public void receiveStalactiteDrip(BlockState state, Level level, BlockPos pos, Fluid fluid) {
        if (!isFull(state) && level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile te) {
            var sf = SoftFluidRegistry.fromVanillaFluid(fluid);
            if (sf != null && te.getSoftFluidTank().addFluid(new SoftFluidStack(sf, 1))) {
                level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
                level.levelEvent(1047, pos, 0);
            }
        }
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
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile te) {
            if (te.handleInteraction(player, hand)) {
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean isFull(BlockState state) {
        return state.getValue(LEVEL) == 4;
    }

    @Override
    protected double getContentHeight(BlockState state) {
        return (5.0D + state.getValue(LEVEL) * 2.5D) / 16.0D;
    }

    //TODO: get other stuff from layered cauldron

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        var s = super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
        if (direction == Direction.DOWN) {
            if (level.getBlockEntity(currentPos) instanceof LiquidCauldronBlockTile te) {
                boolean isFire = shouldBoil(neighborState, te.getSoftFluidTank().getFluid());
                s = s.setValue(BOILING, isFire);
            }
        }
        return s;
    }

    public static boolean shouldBoil(BlockState belowState, SoftFluidStack fluid) {
        return belowState.is(ModTags.HEAT_SOURCES) && fluid.is(BuiltInSoftFluids.POTION.get());
    }


    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (isEntityInsideContent(state, pos, entity)) {
            entity.wasTouchingWater = true;

            if (level.isClientSide) return;

            boolean hasToLower = false;
            if (entity.isOnFire()) {
                entity.clearFire();
                if (entity.mayInteract(level, pos)) {
                    hasToLower = true;
                }
            }
            if (state.getValue(BOILING)) {
                entity.hurt(new DamageSource(ModRegistry.BOILING_DAMAGE.getHolder()), 1.0F);
            }
            if (entity instanceof LivingEntity living && entity.mayInteract(level, pos) &&
                    level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile tile) {

                SoftFluidStack stack = tile.getSoftFluidTank().getFluid();
                if (isSplashOrLingeringPot(stack) && applyPotionFluidEffects(level, pos, living, stack)) {
                    hasToLower = true;
                }
                if (hasToLower) {
                    lowerFillLevel(state, level, pos);
                }
            }
        }
    }

    private boolean applyPotionFluidEffects(Level level, BlockPos pos, LivingEntity living, SoftFluidStack stack) {
        List<MobEffectInstance> effects = PotionUtils.getAllEffects(stack.getTag());
        boolean success = false;
        for (MobEffectInstance effect : effects) {
            MobEffect ef = effect.getEffect();
            if (living.hasEffect(ef)) continue;
            if (ef.isInstantenous()) {
                ef.applyInstantenousEffect(null, null, living,
                        effect.getAmplifier(), 1.0D);
            } else {
                living.addEffect(new MobEffectInstance(effect));
            }
            success = true;
        }
        if (success) {
            level.playSound(null, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
        return success;
    }

    public void lowerFillLevel(BlockState state, Level level, BlockPos pos) {
        int i = state.getValue(LEVEL) - 1;
        BlockState blockState = i == 0 ? Blocks.CAULDRON.defaultBlockState() : state.setValue(LEVEL, i);
        level.setBlockAndUpdate(pos, blockState);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(blockState));
    }


    //Spawn potion when removed

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {

        if (level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile te) {
            if (state.getValue(BOILING)) {
                int color = te.getSoftFluidTank().getParticleColor(level, pos);
                addBubblingParticles(ModRegistry.BOILING_PARTICLE.get(), level, pos, 2,
                        getContentHeight(state), rand, color);

                if (level.random.nextInt(4) == 0) {
                    level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                            SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.BLOCKS,
                            0.4F + level.random.nextFloat() * 0.2F,
                            0.35F + level.random.nextFloat() * 0.2F, false);
                }
            }

            if (level.random.nextInt(4) == 0 && isSplashOrLingeringPot(te.getSoftFluidTank().getFluid())) {
                int color = te.getSoftFluidTank().getParticleColor(level, pos);
                addPotionParticles(ParticleTypes.AMBIENT_ENTITY_EFFECT, level, pos, 1,
                        getContentHeight(state), rand, color);
            }
        }
    }

    private boolean isSplashOrLingeringPot(SoftFluidStack stack) {
        PotionNBTHelper
        return stack.is(BuiltInSoftFluids.POTION.get()) && stack.hasTag() && !stack.getTag()
                .getString(SoftFluidStack.POTION_TYPE_KEY).equals("REGULAR");
    }



    private void addBubblingParticles(ParticleOptions type, Level level, BlockPos pos, int count,
                                      double surface, RandomSource rand, int color) {

        color = getBubbleColor(color);
        float col = Float.intBitsToFloat(color);
        for (int i = 0; i < count; i++) {
            double x = pos.getX() + 0.1875D + (rand.nextFloat() * 0.625D);
            double y = pos.getY() + 5 / 16f;
            double z = pos.getZ() + 0.1875D + (rand.nextFloat() * 0.625D);
            level.addParticle(type, x, y, z, col, pos.getY() + surface, 0);
        }
    }

    private void addPotionParticles(ParticleOptions type, Level level, BlockPos pos, int count,
                                    double surface, RandomSource rand, int color) {

        float r = FastColor.ARGB32.red(color) / 255f;
        float g = FastColor.ARGB32.green(color) / 255f;
        float b = FastColor.ARGB32.blue(color) / 255f;

        for (int i = 0; i < count; i++) {
            double x = pos.getX() + 0.1875D + (rand.nextFloat() * 0.625D);
            double y = pos.getY() + surface;
            double z = pos.getZ() + 0.1875D + (rand.nextFloat() * 0.625D);
            level.addParticle(type, x, y, z, r, g, b);
        }
    }


    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (isEntityInsideContent(state, pos, entity)) {
            if (level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile tile) {
                int color = tile.getSoftFluidTank().getParticleColor(level, pos);
                playSplashAnimation(level, pos, entity, getContentHeight(state), color);
            }
            super.fallOn(level, state, pos, entity, 0);
        } else super.fallOn(level, state, pos, entity, fallDistance);
    }

    public static void playExtinguishSound(Level level, BlockPos pos, Entity entity) {
        level.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, entity.getSoundSource(),
                0.7F, 1.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.4F);
    }

    public static void playSplashAnimation(Level level, BlockPos pos, Entity e, double waterLevel, int color) {
        Entity feetEntity = e.isVehicle() && e.getControllingPassenger() != null ? e.getControllingPassenger() : e;
        float offset = feetEntity == e ? 0.2F : 0.9F;
        Vec3 movement = feetEntity.getDeltaMovement();
        RandomSource rand = level.random;
        float speed = Math.min(1.0F, (float) Math.sqrt(movement.x * movement.x * 0.2 + movement.y * movement.y + movement.z * movement.z * 0.2) * offset);
        if (speed < 0.25F) {
            e.playSound(e.getSwimSplashSound(), speed, 1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.4F);
        } else {
            e.playSound(e.getSwimHighSpeedSplashSound(), speed, 1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.4F);
        }

        double surface = pos.getY() + waterLevel;

        float radius = 1.5f;
        float width = e.getBbWidth();

        spawnSplashParticles(level, e, pos, rand, surface, getBubbleColor(color),
                ModRegistry.BOILING_PARTICLE.get(), radius, width);

        spawnSplashParticles(level, e, pos, rand, surface, color,
                ModRegistry.SPLASH_PARTICLE.get(), radius, width);

        e.gameEvent(GameEvent.SPLASH);
    }

    private static void spawnSplashParticles(Level level, Entity e, BlockPos pos,
                                             RandomSource rand, double surface, int color,
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
                        x, surface, z, Float.intBitsToFloat(color), surface, 0);
            }
        }
    }


    private static int getBubbleColor(int color) {
        return color;
    }

}
