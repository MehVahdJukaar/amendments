package net.mehvahdjukaar.amendments.common.tile;

import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.mehvahdjukaar.amendments.integration.CompatObjects;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

//import net.mehvahdjukaar.amendments.integration.CaveEnhancementsCompat;

public class CandleSkullBlockTile extends EnhancedSkullBlockTile {

    private BlockState candle = Blocks.AIR.defaultBlockState();
    //client only
    private Identifier waxTexture = null;
    private ParticleType<?> particle = ParticleTypes.SMALL_FLAME;

    public CandleSkullBlockTile(BlockPos pWorldPosition, BlockState pBlockState) {
        super(ModRegistry.SKULL_CANDLE_TILE.get(), pWorldPosition, pBlockState);
    }

    public Identifier getWaxTexture() {
        return waxTexture;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable("Candle", BlockState.CODEC, this.candle);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        setCandle(input.read("Candle", BlockState.CODEC).orElse(null));
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
            Block b = candle.getBlock();
            if (b == CompatObjects.CUPRIC_CANDLE.get()) {
                this.particle = CompatObjects.SMALL_CUPRIC_FLAME.get();
            } else if (b == CompatObjects.ENDER_CANDLE.get()) {
                this.particle = CompatObjects.SMALL_ENDER_FLAME.get();
            } else if (b == CompatObjects.SOUL_CANDLE.get()) {
                this.particle = CompatObjects.SMALL_SOUL_FLAME.get();
            } else {
                this.particle = ParticleTypes.SMALL_FLAME;
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

            if (!this.level.isClientSide()) {
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
        //if (CompatHandler.CAVE_ENHANCEMENTS && e.candle.is(CompatObjects.SPECTACLE_CANDLE.get())) {
        //    CaveEnhancementsCompat.tick(level, pos, state);
        //}
    }

    public ParticleType<?> getParticle() {
        return particle;
    }
}
