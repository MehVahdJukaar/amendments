package net.mehvahdjukaar.amendments.common.block;

import net.mehvahdjukaar.amendments.common.network.ClientBoundPlaySplashParticlesMessage;
import net.mehvahdjukaar.amendments.common.network.ModNetwork;
import net.mehvahdjukaar.amendments.common.network.ModNetwork;
import net.mehvahdjukaar.amendments.common.recipe.CauldronRecipeUtils;
import net.mehvahdjukaar.amendments.common.recipe.FluidAndItemsCraftResult;
import net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.misc.InvPlacer;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public abstract class ModCauldronBlock extends AbstractCauldronBlock implements EntityBlock {

    public ModCauldronBlock(BlockBehaviour.Properties properties) {
        super(properties, new CauldronInteraction.InteractionMap("amendments_empty", Map.of()));
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
            if (!level.isClientSide) {
                playSplashEffects(entity, this.getContentHeight(state));
            }
            super.fallOn(level, state, pos, entity, 0);
        } else super.fallOn(level, state, pos, entity, fallDistance);
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
                        level.gameEvent(entity, GameEvent.BLOCK_CHANGE, pos);
                    }
                }
            }
            handleEntityInsideFluid(state, level, pos, entity);
        }
    }


    protected abstract void handleEntityInsideFluid(BlockState state, Level level, BlockPos pos, Entity entity);


    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile te) {
            if (te.handleInteraction(player, hand)) {
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            if (!CommonConfigs.CAULDRON_CRAFTING.get()) return InteractionResult.PASS;

            SoftFluidTank tank = te.getSoftFluidTank();
            ItemStack stack = player.getItemInHand(hand);

            //try recoloring
            int tankCapacity = tank.getCapacity();
            SoftFluidStack currentFluid = tank.getFluid();

            FluidAndItemsCraftResult result = CauldronRecipeUtils.craftMultiple(level, tankCapacity, currentFluid, List.of(stack));

            if (result != null) {
                tank.setFluid(result.currentFluid());

                this.onPlayerCrafted(level, pos, player, hand, stack, result.craftedItems());
                te.setChanged();
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }


    public void onPlayerCrafted(Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack playerItem,
                                List<ItemStack> craftedItems) {

        level.playSound(player, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.3f);

        if (player instanceof ServerPlayer serverPlayer) {
            player.awardStat(Stats.ITEM_USED.get(playerItem.getItem()));
            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, playerItem);
        } else return;

        for (ItemStack crafted : craftedItems) {
            Utils.addItemOrDrop(player, crafted, InvPlacer.handOrExistingOrAny(hand));
        }
    }

    public abstract BlockState updateStateOnFluidChange(BlockState state, Level level, BlockPos pos, SoftFluidStack fluid);


    public static void playExtinguishSound(Level level, BlockPos pos, Entity entity) {
        level.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, entity.getSoundSource(),
                0.7F, 1.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.4F);
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

    public static void spawnResultItems(Level level, BlockPos pos, List<ItemStack> itemStacks) {
        if (itemStacks.isEmpty()) return;

        for (ItemStack item : itemStacks) {
            ItemEntity iteEntity = new ItemEntity(level, pos.getX()+0.5, pos.getY() + 0.25, pos.getZ()+0.5, item);
            iteEntity.setDefaultPickUpDelay();
            iteEntity.setDeltaMovement(0, 0.5, 0);
        }
    }
}
