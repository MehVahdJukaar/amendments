package net.mehvahdjukaar.amendments.mixins.fabric;


import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile;
import net.mehvahdjukaar.moonlight.api.block.ISoftFluidTankProvider;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.fabric.SoftFluidStackImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LiquidCauldronBlockTile.class)
public abstract class SelfCauldronMixin extends BlockEntity implements SingleSlotStorage<FluidVariant>, ISoftFluidTankProvider {


    public SelfCauldronMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        var tank = getSoftFluidTank();
        SoftFluidStack stack = SoftFluidStackImpl.fromFabricFluid(resource, (int) (maxAmount/ FluidConstants.BOTTLE));
        var res = tank.addFluid(stack, true);
        //actually inserts
        transaction.addCloseCallback((t, r) -> {
            if (r.wasCommitted()){
                tank.addFluid(stack, false);
                this.setChanged();
            }
        });
        return res * FluidConstants.BOTTLE;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        var tank = getSoftFluidTank();
        int i = (int) (maxAmount / FluidConstants.BOTTLE);
        var drained = tank.removeFluid(i, false);
        //actually drains
        transaction.addCloseCallback((t, r) -> {
            if (r.wasCommitted()){
                tank.removeFluid(i, false);
                this.setChanged();
            }
        });
        return drained.getCount() * FluidConstants.BOTTLE;
    }

    @Override
    public boolean isResourceBlank() {
        return getSoftFluidTank().isEmpty();
    }

    @Override
    public FluidVariant getResource() {
        return SoftFluidStackImpl.toFabricFluid(getSoftFluidTank().getFluid());
    }

    @Override
    public long getAmount() {
        return getSoftFluidTank().getFluidCount() * FluidConstants.BOTTLE;
    }

    @Override
    public long getCapacity() {
        return getSoftFluidTank().getCapacity() * FluidConstants.BOTTLE;
    }
}
