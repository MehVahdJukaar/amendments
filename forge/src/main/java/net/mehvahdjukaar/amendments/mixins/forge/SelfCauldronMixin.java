package net.mehvahdjukaar.amendments.mixins.forge;

import net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile;
import net.mehvahdjukaar.moonlight.api.block.ISoftFluidTankProvider;
import net.mehvahdjukaar.moonlight.api.fluids.forge.SoftFluidStackImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LiquidCauldronBlockTile.class)
public abstract class SelfCauldronMixin extends BlockEntity implements IFluidHandler, ISoftFluidTankProvider {

    @Unique
    public final LazyOptional<IFluidHandler> amendments$cap = LazyOptional.of(() -> this);

    public SelfCauldronMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return ForgeCapabilities.FLUID_HANDLER.orEmpty(cap, this.amendments$cap);
        }
        return super.getCapability(cap, side);
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int i) {
        return ((SoftFluidStackImpl) this.getSoftFluidTank().getFluid()).toForgeFluid();
    }

    @Override
    public int getTankCapacity(int i) {
        return SoftFluidStackImpl.bottlesToMB(getSoftFluidTank().getCapacity());
    }

    @Override
    public boolean isFluidValid(int i, @NotNull FluidStack fluidStack) {
        return true;
    }

    @Override
    public int fill(FluidStack fluidStack, FluidAction fluidAction) {
        var tank = getSoftFluidTank();
        var original = SoftFluidStackImpl.fromForgeFluid(fluidStack);
        int filled = tank.addFluid(original, fluidAction.simulate() ? true : false);
        int bottlesRemoved = SoftFluidStackImpl.fromForgeFluid(fluidStack).getCount() - original.getCount();
        fluidStack.shrink(SoftFluidStackImpl.bottlesToMB(bottlesRemoved));
        return filled;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack fluidStack, FluidAction fluidAction) {
        return drain(fluidStack.getAmount(), fluidAction);
    }

    @Override
    public @NotNull FluidStack drain(int i, FluidAction fluidAction) {
        var tank = getSoftFluidTank();
        var drained = tank.removeFluid(i, fluidAction.simulate() ? true : false);
        return ((SoftFluidStackImpl) drained).toForgeFluid();
    }
}
