package net.mehvahdjukaar.amendments.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

// a stack without count
public final class SoftFluidIngredient {
    private final SoftFluidStack fluid;

    private SoftFluidIngredient(SoftFluidStack fluid) {
        this.fluid = fluid;
    }

    public static SoftFluidIngredient containing(SoftFluidStack result) {
        return new SoftFluidIngredient(result.copyWithCount(1));
    }

    public boolean matches(SoftFluidStack other) {
        return this.fluid.isSameFluidSameComponents(other);
    }

    public boolean isEmpty() {
        return this.fluid.isEmpty();
    }

    public SoftFluidStack createStack() {
        return fluid.copyWithCount(1);
    }

    public static final Codec<SoftFluidIngredient> CODEC = SoftFluidStack.CODEC
            .validate(
                    stack -> {
                        if (stack.getCount() != 1) {
                            return DataResult.error(() -> "SoftFluidIngredient must have count 1");
                        } else return DataResult.success(stack);
                    })
            .xmap(SoftFluidIngredient::containing, SoftFluidIngredient::createStack);

    public static final StreamCodec<RegistryFriendlyByteBuf, SoftFluidIngredient> STREAM_CODEC =
            SoftFluidStack.STREAM_CODEC.map(SoftFluidIngredient::containing,
                    SoftFluidIngredient::createStack);
}
