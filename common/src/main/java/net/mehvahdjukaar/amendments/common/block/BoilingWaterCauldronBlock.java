package net.mehvahdjukaar.amendments.common.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.amendments.common.recipe.CauldronRecipeInput;
import net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile;
import net.mehvahdjukaar.amendments.reg.ModBlockProperties;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.MLBuiltinSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

import static net.mehvahdjukaar.amendments.events.behaviors.CauldronConversion.getNewState;

public class BoilingWaterCauldronBlock extends LayeredCauldronBlock {
    public static final MapCodec<BoilingWaterCauldronBlock> CODEC = RecordCodecBuilder.mapCodec((i) -> i.group(
            Biome.Precipitation.CODEC.fieldOf("precipitation").forGetter((c) -> c.precipitationType),
            CauldronInteraction.CODEC.fieldOf("interactions").forGetter((c) -> c.interactions),
            propertiesCodec()).apply(i, BoilingWaterCauldronBlock::new));

    public static final BooleanProperty BOILING = ModBlockProperties.BOILING;
    private final Biome.Precipitation precipitationType;

    public BoilingWaterCauldronBlock(Biome.Precipitation precipitationType,
                                     CauldronInteraction.InteractionMap interactions,
                                     Properties properties) {
        super(precipitationType, interactions, properties);
        this.precipitationType = precipitationType;
        this.registerDefaultState(this.defaultBlockState().setValue(BOILING, false));
    }

    @SuppressWarnings("all")
    @Override
    public MapCodec codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BOILING);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (!level.isClientSide && this.isEntityInsideContent(state, pos, entity)) {
            if (state.getValue(BOILING) && entity instanceof LivingEntity) {
                entity.hurt(new DamageSource(ModRegistry.BOILING_DAMAGE.getHolder(entity)), 1.0F);
            }
            if (entity.isOnFire()) LiquidCauldronBlock.playExtinguishSound(level, pos, entity);

            this.attemptStewCrafting(state, level, pos, entity);
        }
    }


    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        var s = super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
        if (direction == Direction.DOWN) {
            boolean isFire = LiquidCauldronBlock.shouldBoil(neighborState, SoftFluidStack.of(
                    MLBuiltinSoftFluids.WATER.getHolder(level.registryAccess())),
                    level, neighborPos);
            s = s.setValue(BOILING, isFire);
        }
        return s;
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (this.isEntityInsideContent(state, pos, entity)) {
            if (entity instanceof ItemEntity ie) {
                ie.setDefaultPickUpDelay();
            }
            if (!level.isClientSide) {
                ModCauldronBlock.playSplashEffects(entity, this.getContentHeight(state));
            }
            super.fallOn(level, state, pos, entity, 0);
        } else super.fallOn(level, state, pos, entity, fallDistance);
    }

    public static int getWaterColor(BlockState state, BlockAndTintGetter level, BlockPos pos, int i) {
        return i == 1 && level != null && pos != null ? BiomeColors.getAverageWaterColor(level, pos) : -1;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (state.getValue(BOILING)) {
            LiquidCauldronBlock.playBubblingAnimation(level, pos, getContentHeight(state), random,
                    getWaterColor(state, level, pos, 1), 0);
        }
    }

    //todo: optional potion crafting...
    private void attemptStewCrafting(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!state.getValue(BOILING) || !(entity instanceof ItemEntity ie)) return;
        if (ie.tickCount % 3 != 0) {
            //age sloower
            ie.setPickUpDelay(ie.pickupDelay + 1);
        }
        if (ie.tickCount % 10 != 0) return;

        var entities = level.getEntitiesOfClass(ItemEntity.class, new AABB(
                pos.getX() + 0.125, pos.getY() + 6 / 16f, pos.getZ() + 0.125,
                pos.getX() + 0.875, pos.getY() + this.getContentHeight(state), pos.getZ() + 0.875));

        List<ItemStack> ingredients = new ArrayList<>();
        for (ItemEntity e : entities) {
            ItemStack i = e.getItem();
            for (int c = 0; c < i.getCount(); c++)
                ingredients.add(i.copyWithCount(1));
        }

        ingredients.add(Items.BOWL.getDefaultInstance());

        CraftingInput container = CauldronRecipeInput.of(ingredients);
        var recipes = level.getRecipeManager().getRecipesFor(RecipeType.CRAFTING, container, level);
        for (var r : recipes) {
            ItemStack result = r.value().assemble(container, level.registryAccess());
            if (result.isEmpty()) continue;
            var fluid = SoftFluidStack.fromItem(result);
            if (fluid == null) continue;
            BlockState newState = getNewState(pos, level, fluid.getFirst());
            if (newState != null) {
                level.setBlockAndUpdate(pos, newState);
                if (level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile te) {
                    int lev = state.getValue(LEVEL); //water cauldron block
                    // yes this can give 1 bottle free on forge. not an issue since water is free anyway
                    int newLev = lev == 3 ? te.getSoftFluidTank().getCapacity() : lev;
                    te.getSoftFluidTank().setFluid(fluid.getFirst().copyWithCount(newLev));
                    te.setChanged();
                    level.gameEvent(entity, GameEvent.BLOCK_CHANGE, pos);
                    level.playSound(null, pos,
                            SoundEvents.BREWING_STAND_BREW,
                            SoundSource.BLOCKS, 0.9f, 0.6F);
                }
                clearUsedIngredients(entities, ingredients);
            }
        }
    }

    private static void clearUsedIngredients(List<ItemEntity> entities, List<ItemStack> ingredients) {
        //clear items
        //all these have count of 1
        for (var v : ingredients) {
            var iter = entities.iterator();
            while (iter.hasNext()) {
                ItemEntity e = iter.next();
                ItemStack itemEntityItem = e.getItem();
                if (ItemStack.isSameItemSameComponents(itemEntityItem, v)) {
                    itemEntityItem.shrink(1);
                    if (itemEntityItem.isEmpty()) {
                        e.discard();
                        iter.remove();
                    }
                }
            }
        }
    }
}

