package net.mehvahdjukaar.amendments.common.block;

import com.mojang.serialization.MapCodec;
import net.mehvahdjukaar.amendments.common.recipe.RecipeUtils;
import net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.integration.AlexCavesCompat;
import net.mehvahdjukaar.amendments.integration.CompatHandler;
import net.mehvahdjukaar.amendments.reg.ModBlockProperties;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.amendments.reg.ModTags;
import net.mehvahdjukaar.moonlight.api.MoonlightRegistry;
import net.mehvahdjukaar.moonlight.api.block.ILightable;
import net.mehvahdjukaar.moonlight.api.fluids.MLBuiltinSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.PotionBottleType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.StreamSupport;

public class LiquidCauldronBlock extends ModCauldronBlock {

    public static final MapCodec<LiquidCauldronBlock> CODEC = simpleCodec(LiquidCauldronBlock::new);

    public static final int MAX_LEVEL = PlatHelper.getPlatform().isFabric() ? 3 : 4;
    public static final IntegerProperty LEVEL = PlatHelper.getPlatform().isFabric() ?
            BlockStateProperties.LEVEL_CAULDRON : ModBlockProperties.LEVEL_1_4;

    public static final IntegerProperty LIGHT_LEVEL = ModBlockProperties.LIGHT_LEVEL;
    public static final BooleanProperty BOILING = ModBlockProperties.BOILING;

    public LiquidCauldronBlock(Properties properties) {
        super(properties.lightLevel(value -> value.getValue(LIGHT_LEVEL)));
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(LEVEL, 1).setValue(LIGHT_LEVEL, 0).setValue(BOILING, false));
    }

    @Override
    protected MapCodec<? extends LiquidCauldronBlock> codec() {
        return CODEC;
    }

    @Override
    public IntegerProperty getLevelProperty() {
        return LEVEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LEVEL, LIGHT_LEVEL, BOILING);
    }

    @Override
    protected boolean canReceiveStalactiteDrip(Fluid fluid) {
        return true;
    }

    @Override
    public void receiveStalactiteDrip(BlockState state, Level level, BlockPos pos, Fluid fluid) {
        if (!isFull(state) && level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile te) {
            int amount = SoftFluid.BOTTLE_COUNT;
            //dumb but matches vanilla
            if(!CommonConfigs.LAVA_LAYERS.get() && fluid == Fluids.LAVA) {
                amount = SoftFluid.BUCKET_COUNT;
            }
            var sf = SoftFluidStack.fromFluid(fluid, amount);
            if (!sf.isEmpty() && te.getSoftFluidTank().addFluid(sf, false) != 0) {
                te.setChanged();
                level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
                if (fluid == Fluids.LAVA) {
                    level.levelEvent(1046, pos, 0);

                } else {
                    level.levelEvent(1047, pos, 0);
                }
            }
        }
    }

    @Override
    public void handlePrecipitation(BlockState state, Level level, BlockPos pos, Biome.Precipitation precipitation) {
        super.handlePrecipitation(state, level, pos, precipitation);
        if (!isFull(state) && level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile te) {
            SoftFluidTank softFluidTank = te.getSoftFluidTank();
            var sf = softFluidTank.getFluid();
            if (precipitation == Biome.Precipitation.RAIN && sf.is(MLBuiltinSoftFluids.WATER) &&
                    softFluidTank.addFluid(SoftFluidStack.fromFluid(Fluids.WATER, 1), false) > 0) {
                level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
                te.setChanged();

            }
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile te) {
            if (te.interactWithPlayerItem(player, hand, stack)) {
                maybeSendPotionMixMessage(te.getSoftFluidTank(), player);
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
            if (!CommonConfigs.CAULDRON_CRAFTING.get()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

            SoftFluidTank tank = te.getSoftFluidTank();
            SoftFluidStack fluid = tank.getFluid();

            var crafted = RecipeUtils.craftWithFluid(level, tank.getFluid(), stack, true);
            if (crafted != null) {

                int mult = fluid.is(MLBuiltinSoftFluids.POTION) ? CommonConfigs.POTION_RECIPES_PER_LAYER.get() : 1;
                if (this.doCraftItem(level, pos, player, hand, fluid, stack, crafted.getFirst(), crafted.getSecond(), mult)) {
                    te.setChanged();
                    return ItemInteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public boolean isFull(BlockState state) {
        return state.getValue(LEVEL) == MAX_LEVEL;
    }

    @Override
    protected double getContentHeight(BlockState state) {
        return 0.4375 + 0.125 * state.getValue(LEVEL);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        var s = super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
        if (direction == Direction.DOWN) {
            if (level.getBlockEntity(currentPos) instanceof LiquidCauldronBlockTile te) {
                boolean isFire = shouldBoil(neighborState, te.getSoftFluidTank().getFluid(), level, neighborPos);
                s = s.setValue(BOILING, isFire);
            }
        }
        return s;
    }

    public static boolean shouldBoil(BlockState belowState, SoftFluidStack fluid, LevelAccessor level, BlockPos pos) {
        if (!belowState.is(ModTags.HEAT_SOURCES) || !fluid.is(ModTags.CAN_BOIL)) return false;

        if (belowState.hasProperty(CampfireBlock.LIT)) {
            return belowState.getValue(CampfireBlock.LIT);
        }
        if (belowState.getBlock() instanceof ILightable il) {
            return il.isLitUp(belowState, level, pos);
        }
        return true;
    }

    @Override
    protected void handleEntityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (state.getValue(BOILING) && entity instanceof LivingEntity) {
            entity.hurt(new DamageSource(ModRegistry.BOILING_DAMAGE), 1.0F);
        }
        if (entity.mayInteract(level, pos) && level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile tile) {

            SoftFluidStack fluid = tile.getSoftFluidTank().getFluid();
            PotionBottleType potType = getPotType(fluid);
            if (entity instanceof LivingEntity living) {
                if (potType != null && potType != PotionBottleType.REGULAR &&
                        applyPotionFluidEffects(level, pos, living, fluid)) {
                    tile.consumeOneLayer();
                    level.gameEvent(entity, GameEvent.BLOCK_CHANGE, pos);
                }

                if (CompatHandler.ALEX_CAVES) {
                    AlexCavesCompat.acidDamage(fluid, level, pos, state, entity);
                }
            }

            if (!tile.isGlowing() && potType != null && entity instanceof ItemEntity ie
                    && ie.getItem().is(Items.GLOW_INK_SAC)
                    && isEntityInsideContent(state, pos, entity)
            ) {
                ModCauldronBlock.playSplashEffects(entity, getContentHeight(state));

                tile.setGlowing(true);
                level.gameEvent(entity, GameEvent.BLOCK_CHANGE, pos);
                ie.getItem().shrink(1);
                if (ie.getItem().isEmpty()) {
                    ie.discard();
                }
            }

        }
    }

    private boolean applyPotionFluidEffects(Level level, BlockPos pos, LivingEntity living, SoftFluidStack stack) {
        List<MobEffectInstance> effects = getPotionEffects(stack);
        boolean success = false;
        for (MobEffectInstance effect : effects) {
            Holder<MobEffect> ef = effect.getEffect();
            if (living.hasEffect(ef)) continue;
            if (ef.value().isInstantenous()) {
                ef.value().applyInstantenousEffect(null, null, living,
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

    //Spawn potion when removed

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {

        if (level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile te) {
            SoftFluidTank tank = te.getSoftFluidTank();
            if (state.getValue(BOILING)) {
                int color = tank.getCachedParticleColor(level, pos);
                int light = tank.getFluidValue().getEmissivity();
                playBubblingAnimation(level, pos, getContentHeight(state), rand, color, light);
            }

            if (level.random.nextInt(4) == 0) {
                SoftFluidStack fluid = tank.getFluid();
                PotionBottleType type = getPotType(fluid);
                double height = getContentHeight(state);
                if (type != null) {
                    if (getPotionEffects(fluid).size() >= CommonConfigs.POTION_MIXING_LIMIT.get()) {
                        addSurfaceParticles(ParticleTypes.SMOKE, level, pos, 2, height, rand, 0, 0, 0);
                    }
                    if (type != PotionBottleType.REGULAR) {

                        int color = tank.getCachedParticleColor(level, pos);
                        int alpha = type == PotionBottleType.SPLASH ? Mth.floor(38.25F) : 255;
                        ParticleOptions particle = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT,
                                FastColor.ARGB32.color(alpha, color));

                        addPotionParticles(particle, level, pos, 1, height, rand, color);
                    }
                }
                if (CompatHandler.ALEX_CAVES) {
                    AlexCavesCompat.acidParticles(fluid, level, pos, rand, height);
                }

                BlockPos blockPos = pos.above();
                if (fluid.is(MLBuiltinSoftFluids.LAVA) && level.getBlockState(blockPos).isAir() && !level.getBlockState(blockPos).isSolidRender(level, blockPos)) {
                    var c = pos.getCenter();
                    if (rand.nextInt(20) == 0) {
                        addSurfaceParticles(ParticleTypes.LAVA, level, pos, 1, height, rand, 0, 0, 0);
                        level.playLocalSound(c.x, height, c.z, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.2F + rand.nextFloat() * 0.2F, 0.9F + rand.nextFloat() * 0.15F, false);
                    }
                    if (rand.nextInt(40) == 0) {
                        level.playLocalSound(c.x, height, c.z, SoundEvents.LAVA_AMBIENT, SoundSource.BLOCKS, 0.2F + rand.nextFloat() * 0.2F, 0.9F + rand.nextFloat() * 0.15F, false);
                    }
                }
            }
        }
    }

    @Nullable
    private PotionBottleType getPotType(SoftFluidStack stack) {
        if (stack.is(MLBuiltinSoftFluids.POTION)) {
            return stack.getOrDefault(MoonlightRegistry.BOTTLE_TYPE.get(), PotionBottleType.REGULAR);
        }
        return null;
    }

    private List<MobEffectInstance> getPotionEffects(SoftFluidStack stack) {
        return StreamSupport.stream(stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)
                .getAllEffects().spliterator(), false).toList();
    }


    public static void playBubblingAnimation(Level level, BlockPos pos,
                                             double surface, RandomSource rand, int color, int light) {

        var type = ModRegistry.BOILING_PARTICLE.get();
        int count = 2;
        addSurfaceParticles(type, level, pos, count, surface, rand, color, pos.getY() + 5 / 16f, light);

        if (level.random.nextInt(4) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.BLOCKS,
                    0.4F + level.random.nextFloat() * 0.2F,
                    0.35F + level.random.nextFloat() * 0.2F, false);
        }
    }

    private void addPotionParticles(ParticleOptions type, Level level, BlockPos pos, int count,
                                    double surface, RandomSource rand, int color) {

        float r = FastColor.ARGB32.red(color) / 255f;
        float g = FastColor.ARGB32.green(color) / 255f;
        float b = FastColor.ARGB32.blue(color) / 255f;
        addSurfaceParticles(type, level, pos, count, surface, rand, r, g, b);
    }

    @Override
    public BlockState updateStateOnFluidChange(BlockState state, Level level, BlockPos pos, SoftFluidStack fluid) {
        //explosions?
        BlockState exploded = maybeExplode(state, level, pos, fluid);
        if (exploded != null) return exploded;

        int light = fluid.fluid().getLuminosity();
        if (light != state.getValue(ModBlockProperties.LIGHT_LEVEL)) {
            state = state.setValue(ModBlockProperties.LIGHT_LEVEL, light);
        }
        int height = fluid.getCount();
        if (fluid.isEmpty()) {
            state = Blocks.CAULDRON.defaultBlockState();
        } else {
            state = state.setValue(LiquidCauldronBlock.LEVEL, height);
        }
        return state;
    }

    public void maybeSendPotionMixMessage(SoftFluidTank fluidTank, Player player) {
        if (fluidTank.getFluid().is(MLBuiltinSoftFluids.POTION)) {
            var potionEffects = getPotionEffects(fluidTank.getFluid());
            int potionEffectAmount = potionEffects.size();
            if (potionEffectAmount == CommonConfigs.POTION_MIXING_LIMIT.get()) {
                player.displayClientMessage(Component.translatable("message.amendments.cauldron"), true);
            }
        }
    }

    @Nullable
    private BlockState maybeExplode(BlockState state, Level level, BlockPos pos, SoftFluidStack fluid) {
        var potionEffects = getPotionEffects(fluid);
        int potionEffectAmount = potionEffects.size();
        if (potionEffectAmount >= CommonConfigs.POTION_MIXING_LIMIT.get()) {
            if (potionEffectAmount > CommonConfigs.POTION_MIXING_LIMIT.get()) {
                level.destroyBlock(pos, true);
                Vec3 vec3 = pos.getCenter();
                level.explode(null, level.damageSources().badRespawnPointExplosion(vec3), null, vec3.x, vec3.y, vec3.z,
                        1.4f, false, Level.ExplosionInteraction.NONE);

                return state;
            } else {
                if (level.isClientSide)
                    addSurfaceParticles(ParticleTypes.SMOKE, level, pos, 12, getContentHeight(state), level.random, 0, 0, 0);
                level.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1.0f, 1.0f);
                return null;
            }
        }
        //fizzle
        var inverse = CommonConfigs.INVERSE_POTIONS.get();
        var effects = potionEffects.stream().map(e -> e.getEffect().value()).toList();
        for (var effect : effects) {
            var inv = inverse.get(effect);
            if (inv != null && effects.contains(inv)) {
                if (level.isClientSide)
                    addSurfaceParticles(ParticleTypes.POOF, level, pos, 8, getContentHeight(state), level.random,
                            0, 0.01f + level.random.nextFloat() * 0.1f, 0);
                level.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1.0f, 1.0f);
                return Blocks.CAULDRON.defaultBlockState();
            }
        }

        return null;
    }


}
