package net.mehvahdjukaar.amendments.common.tile;

import net.mehvahdjukaar.amendments.AmendmentsPlatformStuff;
import net.mehvahdjukaar.amendments.common.block.DyeCauldronBlock;
import net.mehvahdjukaar.amendments.common.block.LiquidCauldronBlock;
import net.mehvahdjukaar.amendments.common.block.ModCauldronBlock;
import net.mehvahdjukaar.amendments.common.item.DyeBottleItem;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.block.ISoftFluidTankProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LiquidCauldronBlockTile extends BlockEntity implements IExtraModelDataProvider, ISoftFluidTankProvider {
    public static final ModelDataKey<SoftFluid> FLUID = new ModelDataKey<>(SoftFluid.class);

    private final SoftFluidTank fluidTank;

    public SoftFluidTank makeTank(BlockState blockState) {
        return blockState.getBlock() instanceof DyeCauldronBlock ?
                AmendmentsPlatformStuff.createCauldronDyeTank() :
                AmendmentsPlatformStuff.createCauldronLiquidTank(this::canMixPotions);
    }

    private boolean canMixPotions() {
        var config = CommonConfigs.POTION_MIXING.get();
        return config == CommonConfigs.MixingMode.ON || (config == CommonConfigs.MixingMode.ONLY_BOILING &&
                this.getBlockState().getValue(LiquidCauldronBlock.BOILING));
    }

    public LiquidCauldronBlockTile(BlockPos blockPos, BlockState blockState) {
        super(ModRegistry.LIQUID_CAULDRON_TILE.get(), blockPos, blockState);
        this.fluidTank = makeTank(blockState);
        //this.fluidHolder.setFluid(ModRegistry.DYE_SOFT_FLUID.get());
    }




    @Override
    public ExtraModelData getExtraModelData() {
        return ExtraModelData.builder()
                .with(FLUID, fluidTank.getFluidValue())
                .build();
    }

    @Override
    public SoftFluidTank getSoftFluidTank() {
        return fluidTank;
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.fluidTank.load(compound);
        if (this.level != null) {
            if (this.level.isClientSide) {
                fluidTank.refreshTintCache();
                this.requestModelReload();
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        this.fluidTank.save(tag);
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

        //update state based on fluid

        BlockState state = this.getBlockState();

        if (state.getBlock() instanceof ModCauldronBlock cb) {
            state = cb.updateStateOnFluidChange(state, level, worldPosition, fluidTank.getFluid());
        }

        if (state != this.getBlockState()) {
            this.level.setBlockAndUpdate(this.worldPosition, state);
        }

        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), state, Block.UPDATE_CLIENTS);

        super.setChanged();
    }


    // does all the calculation for handling player interaction.
    public boolean handleInteraction(Player player, InteractionHand hand) {
        //interact with fluid holder
        if (this.fluidTank.interactWithPlayer(player, hand, level, worldPosition)) {
            this.setChanged();
            return true;
        }
        return false;
    }

    public void consumeOneLayer() {
        this.fluidTank.getFluid().shrink(1);
        this.setChanged();
    }
}
