package net.mehvahdjukaar.amendments.common.tile;

import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.block.ISoftFluidTankProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.IExtraModelDataProvider;
import net.mehvahdjukaar.moonlight.api.client.model.ModelDataKey;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class LiquidCauldronBlockTile extends BlockEntity implements IExtraModelDataProvider, ISoftFluidTankProvider {
    public static final ModelDataKey<SoftFluid> FLUID = new ModelDataKey<>(SoftFluid.class);

    private final SoftFluidTank fluidHolder;

    public LiquidCauldronBlockTile(BlockPos blockPos, BlockState blockState) {
        super(ModRegistry.LIQUID_CAULDRON_TILE.get(), blockPos, blockState);
        this.fluidHolder = SoftFluidTank.create(3);
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
}
