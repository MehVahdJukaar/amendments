package net.mehvahdjukaar.amendments.reg;

import net.mehvahdjukaar.amendments.common.block.DyeCauldronBlock;
import net.mehvahdjukaar.amendments.common.block.WallLanternBlock;
import net.mehvahdjukaar.amendments.common.item.DyeBottleItem;
import net.mehvahdjukaar.amendments.common.item.behaviors.SkullCandleConversion;
import net.mehvahdjukaar.amendments.common.item.placement.WallLanternPlacement;
import net.mehvahdjukaar.amendments.common.tile.CarpetedBlockTile;
import net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.item.additional_placements.AdditionalItemPlacement;
import net.mehvahdjukaar.moonlight.api.item.additional_placements.AdditionalItemPlacementsAPI;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ModEvents {

    public static void init() {
        if (CommonConfigs.WALL_LANTERN.get()) {
            AdditionalItemPlacementsAPI.register((i) -> new WallLanternPlacement(((BlockItem) i).getBlock()),
                    i -> i instanceof BlockItem bi && WallLanternBlock.isValidBlock(bi.getBlock()));
        }
        AdditionalItemPlacementsAPI.register((i) -> new AdditionalItemPlacement(ModRegistry.SKULL_CANDLE.get()),
                SkullCandleConversion::isCorrectCandle);

        //block items don't work here
            /*
            if (ServerConfigs.cached.SKULL_CANDLES) {
                if (i.builtInRegistryHolder().is(ItemTags.CANDLES) &&
                        i.getRegistryName().getNamespace().equals("minecraft")) {
                    ((IExtendedItem) i).addAdditionalBehavior(new SkullCandlesPlacement());
                    continue;
                }
            }*/
    }

    @EventCalled
    public static InteractionResult onRightClickBlock(Player player, Level level, InteractionHand hand,
                                                      BlockHitResult hitResult) {
        //TODO: move to interactions
        ItemStack stack = player.getItemInHand(hand);
        BlockPos pos = hitResult.getBlockPos();
        BlockState state = level.getBlockState(pos);
        Item item = stack.getItem();
        if(state.is(Blocks.CAULDRON)){
            var fluid = SoftFluidRegistry.fromItem(item);
            if(!fluid.isEmpty() && !Utils.getID(fluid).getNamespace().equals(Moonlight.MOD_ID)){
                Block b = item == ModRegistry.DYE_BOTTLE_ITEM.get() ? ModRegistry.DYE_CAULDRON.get() :
                        ModRegistry.LIQUID_CAULDRON.get();
                level.setBlockAndUpdate(pos, b.defaultBlockState());
                if(level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile te){
                    te.handleInteraction(player, hand);
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }


            return InteractionResult.PASS;
        }
        if(state.is(Blocks.WATER_CAULDRON)){
            if(item instanceof DyeItem dye){
                Integer l = state.getValue(LayeredCauldronBlock.LEVEL);
                level.setBlockAndUpdate(pos, ModRegistry.DYE_CAULDRON.get().defaultBlockState().setValue(DyeCauldronBlock.LEVEL,l));
                if(level.getBlockEntity(pos) instanceof LiquidCauldronBlockTile te){
                    DyeBottleItem.fillCauldron(te.getSoftFluidTank(), dye.getDyeColor(), l);
                }
                stack.shrink(1);
                //TODO: advancements and stats
                return InteractionResult.sidedSuccess(level.isClientSide);

            }
            return InteractionResult.PASS;
        }

        if (player.isSecondaryUseActive()) return InteractionResult.PASS;
        if (player.getAbilities().mayBuild && item instanceof BlockItem bi &&
                (bi.getBlock() instanceof CarpetBlock || bi.getBlock().defaultBlockState().is(BlockTags.WOOL_CARPETS))) {

            BlockState replacingBlock = getReplacingBlock(state);
            if (replacingBlock != null) {
                level.setBlockAndUpdate(pos, replacingBlock);
                if (level.getBlockEntity(pos) instanceof CarpetedBlockTile tile) {
                    var carpet = bi.getBlock().defaultBlockState();
                    tile.initialize(state, carpet);
                    if (!player.getAbilities().instabuild) stack.shrink(1);

                    if (player instanceof ServerPlayer serverPlayer) {
                        CriteriaTriggers.PLACED_BLOCK.trigger(serverPlayer, pos, stack);
                    }
                    level.gameEvent(player, GameEvent.BLOCK_PLACE, pos);

                    SoundType sound = carpet.getSoundType();
                    level.playSound(player, pos, sound.getPlaceSound(), SoundSource.BLOCKS,
                            (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);

                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }

        return InteractionResult.PASS;
    }

    @Nullable
    private static BlockState getReplacingBlock(BlockState state) {
        Block b = state.getBlock();
        if (!(b instanceof EntityBlock)) {
            if (b instanceof StairBlock &&
                    state.getValue(StairBlock.HALF) == Half.BOTTOM) {
                return ModRegistry.CARPET_STAIRS.get().withPropertiesOf(state);
            } else if (b instanceof SlabBlock && state.getValue(SlabBlock.TYPE) == SlabType.BOTTOM) {
                return ModRegistry.CARPET_SLAB.get().withPropertiesOf(state);
            }
        }
        return null;
    }

}
