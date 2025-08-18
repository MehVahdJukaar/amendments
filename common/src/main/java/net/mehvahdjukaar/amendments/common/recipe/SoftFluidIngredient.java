package net.mehvahdjukaar.amendments.common.recipe;

import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.network.FriendlyByteBuf;

// a stack without count
public final class SoftFluidIngredient {
    private final SoftFluidStack fluid;

    private SoftFluidIngredient(SoftFluidStack fluid) {
        this.fluid = fluid;
    }

    public static SoftFluidIngredient containing(SoftFluidStack result) {
        return new SoftFluidIngredient(result.copyWithCount(1));
    }

    public static SoftFluidIngredient loadFromBuffer(FriendlyByteBuf buffer) {
        return SoftFluidIngredient.containing(SoftFluidStack.loadFromBuffer(buffer));
    }

    public void saveToBuffer(FriendlyByteBuf buffer) {
        fluid.saveToBuffer(buffer);
    }

    public boolean matches(SoftFluidStack other) {
        return this.fluid.isFluidEqual(other);
    }

    public boolean isEmpty(){
        return this.fluid.isEmpty();
    }

    public SoftFluidStack createStack(){
        return fluid.copyWithCount(1);
    }
}
