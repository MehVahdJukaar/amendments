package net.mehvahdjukaar.amendments.common.tile;

import net.mehvahdjukaar.amendments.common.block.LiquidCauldronBlock;
import net.mehvahdjukaar.amendments.reg.ModBlockProperties;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.block.ISoftFluidTankProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.supplementaries.common.block.tiles.GobletBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class LiquidCauldronBlockTile extends BlockEntity implements IExtraModelDataProvider, ISoftFluidTankProvider {
    public static final ModelDataKey<SoftFluid> FLUID = new ModelDataKey<>(SoftFluid.class);

    private final SoftFluidTank fluidHolder;

    public LiquidCauldronBlockTile(BlockPos blockPos, BlockState blockState) {
        super(ModRegistry.LIQUID_CAULDRON_TILE.get(), blockPos, blockState);
        this.fluidHolder = SoftFluidTank.create(4);
    }


    @Override
    public ExtraModelData getExtraModelData() {
        return ExtraModelData.builder()
                .with(FLUID, fluidHolder.getFluid())
                .build();
    }

    @Override
    public SoftFluidTank getSoftFluidTank() {
        return fluidHolder;
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.fluidHolder.load(compound);
        if (this.level != null) {
            if (this.level.isClientSide) this.requestModelReload();
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        this.fluidHolder.save(tag);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public void setChanged() {
        if (this.level == null) return;
        //TODO: only call after you finished updating your tile so others can react properly (faucets)
        this.level.updateNeighborsAt(worldPosition, this.getBlockState().getBlock());
        int light = this.fluidHolder.getFluid().getLuminosity();
        BlockState state = this.getBlockState();
        if (light != state.getValue(ModBlockProperties.LIGHT_LEVEL)) {
            state = state.setValue(ModBlockProperties.LIGHT_LEVEL, light);
        }
        int height = this.fluidHolder.getCount();
        if (fluidHolder.isEmpty()) {
            state = Blocks.CAULDRON.defaultBlockState();
        } else if (height != state.getValue(LiquidCauldronBlock.LEVEL)) {
            state = state.setValue(LiquidCauldronBlock.LEVEL, height);
        }
        if (state != this.getBlockState()) {
            this.level.setBlock(this.worldPosition, state, 2);
        }
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        super.setChanged();
    }


    // does all the calculation for handling player interaction.
    public boolean handleInteraction(Player player, InteractionHand hand) {
        //interact with fluid holder
        if (this.fluidHolder.interactWithPlayer(player, hand, level, worldPosition)) {
            if (!level.isClientSide()) this.setChanged();
            return true;
        }
        return false;
    }

}
