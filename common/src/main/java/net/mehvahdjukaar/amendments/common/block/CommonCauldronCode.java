package net.mehvahdjukaar.amendments.common.block;

import net.mehvahdjukaar.amendments.common.network.ClientBoundPlaySplashParticlesMessage;
import net.mehvahdjukaar.amendments.common.recipe.CauldronRecipeUtils;
import net.mehvahdjukaar.amendments.common.recipe.FluidAndItemCraftResult;
import net.mehvahdjukaar.amendments.common.recipe.FluidAndItemsCraftResult;
import net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.events.behaviors.CauldronConversion;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.amendments.reg.ModTags;
import net.mehvahdjukaar.moonlight.api.block.ILightable;
import net.mehvahdjukaar.moonlight.api.fluids.MLBuiltinSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.misc.InvPlacer;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

//since ModCauldronBlock and BoilingWaterCauldronBlock can't share a parent we put all common logic here
public final class CommonCauldronCode {


    public static void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, Supplier<Double> heightProvider) {
        boolean canFluidExtinguish = true;
        boolean shouldCheckFluid = !entity.wasTouchingWater || entity.isOnFire(); //minor optimization
        if (shouldCheckFluid) {
            if (getFluidOrWater(state, pos, level).is(ModTags.CANT_EXTINGUISH)) {
                canFluidExtinguish = false;
            }
        }
        if (canFluidExtinguish) {
            entity.wasTouchingWater = true;
        }
        if (level.isClientSide) return;

        if (state.getValue(ModCauldronBlock.BOILING) && entity instanceof LivingEntity) {
            entity.hurt(new DamageSource(ModRegistry.BOILING_DAMAGE.getHolder(level)), 1.0F);
        }
        if (entity.isOnFire() && canFluidExtinguish) {
            playExtinguishSound(level, pos, entity);

            //same as super of layered cauldron
            if (!(state.getBlock() instanceof LayeredCauldronBlock)) {
                extinguishLikeSuperLayeredCauldron(level, pos, entity);
            }
        }

        attemptInWorldCrafting(state, level, pos, entity, heightProvider);
    }

    private static void extinguishLikeSuperLayeredCauldron(Level level, BlockPos pos, Entity entity) {
        entity.clearFire();
        if (entity.mayInteract(level, pos)) {
            if (level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile te) {
                te.consumeOneLayer();
                level.gameEvent(entity, GameEvent.BLOCK_CHANGE, pos);
            }
        }
    }

    private static void playExtinguishSound(Level level, BlockPos pos, Entity entity) {
        level.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, entity.getSoundSource(),
                0.7F, 1.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.4F);
    }


    public static BlockState updateBoilingState(Direction direction, BlockState neighborState, LevelAccessor level, BlockPos neighborPos, BlockState newState, BlockPos currentPos) {
        if (direction == Direction.DOWN) {
            SoftFluidStack fluidStack = getFluidOrWater(newState, currentPos, level);
            boolean isFire = shouldBoil(neighborState, fluidStack, level, neighborPos);
            newState = newState.setValue(ModCauldronBlock.BOILING, isFire);
        }
        return newState;
    }

    private static SoftFluidStack getFluidOrWater(BlockState newState, BlockPos pos, LevelAccessor level) {
        if (newState.getBlock() instanceof ModCauldronBlock && level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile te) {
            return te.getSoftFluidTank().getFluid();
        } else {
            return SoftFluidStack.of(MLBuiltinSoftFluids.WATER.getHolder(level.registryAccess()),
                    newState.getValue(LayeredCauldronBlock.LEVEL));
        }
    }

    public static boolean shouldBoil(BlockState belowState, SoftFluidStack fluid, LevelAccessor level, BlockPos pos) {
        if (!belowState.is(ModTags.HEAT_SOURCES) || fluid.is(ModTags.CANT_BOIL)) return false;

        if (belowState.hasProperty(CampfireBlock.LIT)) {
            return belowState.getValue(CampfireBlock.LIT);
        }
        if (belowState.getBlock() instanceof ILightable il) {
            return il.isLitUp(belowState, level, pos);
        }
        return true;
    }


    public static void onEntityFallOnContent(Level level, BlockState state, Entity entity, double height) {
        if (entity instanceof ItemEntity ie) {
            ie.setDefaultPickUpDelay();
        }
        if (!level.isClientSide) {
            playSplashEffects(entity, height);
        }
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


    public static void addSurfaceParticles(ParticleOptions type, Level level, BlockPos pos, int count, double surface, RandomSource rand,
                                           float r, float g, float b) {
        for (int i = 0; i < count; i++) {
            double x = pos.getX() + 0.1875D + (rand.nextFloat() * 0.625D);
            double y = pos.getY() + surface;
            double z = pos.getZ() + 0.1875D + (rand.nextFloat() * 0.625D);
            level.addParticle(type, x, y, z, r, g, b);
        }
    }

    public static void playSplashEffects(Entity entity, double waterHeight) {

        // also send game event
        entity.gameEvent(GameEvent.SPLASH);


        Level level = entity.level();
        Entity feetEntity = entity.isVehicle() && entity.getControllingPassenger() != null ? entity.getControllingPassenger() : entity;
        float offset = feetEntity == entity ? 0.2F : 0.9F;
        Vec3 movement = feetEntity.getDeltaMovement();
        float speed = Math.min(1.0F, (float) Math.sqrt(movement.x * movement.x * 0.2 + movement.y * movement.y + movement.z * movement.z * 0.2) * offset);

        BlockPos pos = BlockPos.containing(entity.position());
        Vec3 hitPos = new Vec3(entity.getX(), pos.getY() + waterHeight, entity.getZ());

        RandomSource rand = level.random;
        // same logic as normal water splash sounds (just on server side)
        if (speed < 0.25F) {
            level.playSound(null, hitPos.x(), hitPos.y(), hitPos.z(),
                    entity.getSwimSplashSound(), entity.getSoundSource(),
                    speed, 1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.4F);
        } else {
            level.playSound(null, hitPos.x(), hitPos.y(), hitPos.z(),
                    entity.getSwimHighSpeedSplashSound(), entity.getSoundSource(),
                    speed, 1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.4F);
        }


        var particlePacket = new ClientBoundPlaySplashParticlesMessage(hitPos, speed, feetEntity.getBbWidth());

        NetworkHelper.sendToAllClientPlayersTrackingEntityAndSelf(entity, particlePacket);
    }


    //crafting

    private static void attemptInWorldCrafting(BlockState state, Level level, BlockPos pos, Entity entity,
                                               Supplier<Double> heightProvider) {
        if (!CommonConfigs.CAULDRON_HAND_CRAFTING.get()) return;

        if (!(entity instanceof ItemEntity ie)) return;
        //age sloower
        ie.setPickUpDelay(ie.pickupDelay + 1);
        // if (ie.tickCount % 10 != 0) return;

        var entities = level.getEntitiesOfClass(ItemEntity.class, new AABB(
                pos.getX() + 0.125, pos.getY() + 6 / 16f, pos.getZ() + 0.125,
                pos.getX() + 0.875, pos.getY() + heightProvider.get(), pos.getZ() + 0.875));

        List<ItemStack> ingredients = new ArrayList<>();
        for (ItemEntity e : entities) {
            ingredients.add(e.getItem());
        }

        SoftFluidStack cauldronFluid = getFluidOrWater(state, pos, level);

        FluidAndItemCraftResult craftResult = CauldronRecipeUtils.craft(level,
                state.getValue(ModCauldronBlock.BOILING), 3, cauldronFluid, ingredients);

        if (craftResult == null) return;

        SoftFluidStack resultFluid = craftResult.resultFluid();
        //stupid for water
        if (cauldronFluid.is(MLBuiltinSoftFluids.WATER) && cauldronFluid.getCount() == 3
                && resultFluid.getCount() == 3
                && PlatHelper.getPlatform().isForge()) {
            resultFluid.setCount(4);
        }

        playCraftSound(pos, level, cauldronFluid, resultFluid);

        CauldronConversion.setCorrectCauldronStateAndTile(state, level, pos, resultFluid);

        level.gameEvent(entity, GameEvent.BLOCK_CHANGE, pos);

        spawnResultItems(level, pos, List.of(craftResult.craftedItem()));
        //clear items
        //all these have count of 1
        for (var e : entities) {
            if (e.getItem().isEmpty()) {
                e.discard();
            }
        }
    }


    private static void spawnResultItems(Level level, BlockPos pos, List<ItemStack> itemStacks) {
        if (itemStacks.isEmpty()) return;

        for (ItemStack item : itemStacks) {
            ItemEntity iteEntity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.25, pos.getZ() + 0.5, item);
            iteEntity.setDefaultPickUpDelay();
            iteEntity.setDeltaMovement(level.random.nextGaussian() * 0.01, 0.375, level.random.nextGaussian() * 0.01);
            level.addFreshEntity(iteEntity);
        }
    }


    public static boolean attemptPlayerCrafting(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                                int tankCapacity, SoftFluidStack currentFluid) {
        if (!CommonConfigs.CAULDRON_HAND_CRAFTING.get()) return false;

        ItemStack playerStack = player.getItemInHand(hand);

        FluidAndItemsCraftResult result = CauldronRecipeUtils.craftMultiple(level,
                state.getValue(ModCauldronBlock.BOILING),
                tankCapacity, currentFluid, List.of(playerStack));

        if (result != null) {
            playCraftSound(pos, level, currentFluid, result.resultFluid());
            CauldronConversion.setCorrectCauldronStateAndTile(state, level, pos, result.resultFluid());

            onPlayerCrafted(level, pos, player, hand, playerStack, result.craftedItems());
            return true;
        }
        return false;
    }


    private static void onPlayerCrafted(Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack playerItem,
                                        List<ItemStack> craftedItems) {

        if (player instanceof ServerPlayer serverPlayer) {
            player.awardStat(Stats.ITEM_USED.get(playerItem.getItem()));
            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, playerItem);
        } else return;

        for (var crafted : craftedItems) {
            Utils.addItemOrDrop(player, crafted, InvPlacer.handOrExistingOrAny(hand));
        }
    }

    private static void playCraftSound(BlockPos pos, Level level, SoftFluidStack oldStack, SoftFluidStack newStack) {
        if (oldStack.fluid() != newStack.fluid()) {
            level.playSound(null, pos,
                    SoundEvents.BREWING_STAND_BREW,
                    SoundSource.BLOCKS, 0.9f, 0.6F);
        } else {
            level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.3f);
        }
    }
}
