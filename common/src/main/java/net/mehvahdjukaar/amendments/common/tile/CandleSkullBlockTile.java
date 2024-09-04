package net.mehvahdjukaar.amendments.common.tile;

import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.mehvahdjukaar.amendments.integration.CaveEnhancementsCompat;
import net.mehvahdjukaar.amendments.integration.CompatHandler;
import net.mehvahdjukaar.amendments.integration.CompatObjects;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CandleSkullBlockTile extends EnhancedSkullBlockTile {

    private BlockState candle = Blocks.AIR.defaultBlockState();
    //client only
    private ResourceLocation waxTexture = null;

    public CandleSkullBlockTile(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModRegistry.SKULL_CANDLE_TILE.get(), pWorldPosition, pBlockState);
    }

    public ResourceLocation getWaxTexture() {
        return waxTexture;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Candle", NbtUtils.writeBlockState(this.candle));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        BlockState c = null;
        if (tag.contains("Candle", 10)) {
            c = Utils.readBlockState(tag.getCompound("Candle"), level);
        }
        setCandle(c);
    }

    public BlockState getCandle() {
        return candle;
    }

    public void setCandle(BlockState candle) {
        this.candle = candle;
        if (PlatHelper.getPhysicalSide().isClient()) {
            this.waxTexture = null;
            if (this.candle != null) {
                this.waxTexture = AmendmentsClient.SKULL_CANDLES_TEXTURES.get().get(this.candle.getBlock());
            }
        }
    }

    public boolean tryAddingCandle(CandleBlock candle) {
        if (this.candle.isAir() || (candle == this.candle.getBlock() && this.candle.getValue(CandleBlock.CANDLES) != 4)) {

            if (this.candle.isAir()) {
                this.setCandle(candle.defaultBlockState());
            } else {
                this.candle.cycle(CandleBlock.CANDLES);
            }

            if (!this.level.isClientSide) {
                BlockState state = this.getBlockState();
                BlockState newState = Utils.replaceProperty(this.candle, state, CandleBlock.CANDLES);
                this.level.setBlockAndUpdate(this.worldPosition, newState);
                this.setChanged();
            }
            return true;
        }
        return false;
    }


    @Override
    public void initialize(SkullBlockEntity oldTile, ItemStack stack, Player player, InteractionHand hand) {
        super.initialize(oldTile, stack, player, hand);
        if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof CandleBlock candleBlock) {
            tryAddingCandle(candleBlock);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CandleSkullBlockTile e) {
        e.tick(level, pos, state);
        if (CompatHandler.CAVE_ENHANCEMENTS && e.candle.is(CompatObjects.SPECTACLE_CANDLE.get())) {
            CaveEnhancementsCompat.tick(level, pos, state);
        }
    }
}
