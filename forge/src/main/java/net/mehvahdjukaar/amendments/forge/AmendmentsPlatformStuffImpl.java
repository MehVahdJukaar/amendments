package net.mehvahdjukaar.amendments.forge;

import net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.fluids.forge.SoftFluidTankImpl;
import net.mehvahdjukaar.moonlight.api.util.PotionNBTHelper;

import java.util.function.BooleanSupplier;

public class AmendmentsPlatformStuffImpl {

    public static SoftFluidTank createCauldronLiquidTank(BooleanSupplier canMix) {
        return new SoftFluidTankImpl(4) {

            @Override
            public boolean canAddSoftFluid(SoftFluidStack fluidStack) {
                if (fluidStack.is(BuiltInSoftFluids.WATER.get())) return false;
                if (canMix.getAsBoolean() && fluidStack.is(BuiltInSoftFluids.POTION.get()) && fluidStack.is(this.getFluidValue())) {
                    // just compares bottle types
                    return this.getSpace() >= fluidStack.getCount() && this.fluid.getTag()
                            .getString(PotionNBTHelper.POTION_TYPE_KEY).equals(
                                    fluidStack.getTag().getString(PotionNBTHelper.POTION_TYPE_KEY));
                }
                return super.canAddSoftFluid(fluidStack);
            }

            @Override
            protected void addFluidOntoExisting(SoftFluidStack incoming) {
                if (canMix.getAsBoolean() && incoming.is(BuiltInSoftFluids.POTION.get())) {
                    LiquidCauldronBlockTile.mixPotions(this.fluid, incoming);
                    needsColorRefresh = true;

                }
                super.addFluidOntoExisting(incoming);
            }
        };
    }

    public static SoftFluidTank createCauldronDyeTank() {
        return new SoftFluidTankImpl(3) {

            @Override
            public boolean canAddSoftFluid(SoftFluidStack fluidStack) {
                if (fluidStack.is(ModRegistry.DYE_SOFT_FLUID.get()) && fluidStack.is(this.getFluidValue())) {
                    return this.getSpace() >= fluidStack.getCount(); //discard nbt
                } else return super.canAddSoftFluid(fluidStack);
            }

            @Override
            protected void addFluidOntoExisting(SoftFluidStack fluidStack) {
                if (fluidStack.is(ModRegistry.DYE_SOFT_FLUID.get())) {
                    LiquidCauldronBlockTile.mixDye(this.fluid, fluidStack);
                }
                super.addFluidOntoExisting(fluidStack);
            }
        };
    }

}
