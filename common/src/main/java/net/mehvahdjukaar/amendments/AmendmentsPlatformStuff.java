package net.mehvahdjukaar.amendments;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.minecraft.client.renderer.block.model.BakedQuad;

import java.util.List;
import java.util.function.BooleanSupplier;

public class AmendmentsPlatformStuff {

    @ExpectPlatform
    public static SoftFluidTank createCauldronLiquidTank(BooleanSupplier canMix){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static SoftFluidTank createCauldronDyeTank(){
        throw new AssertionError();
    }
}
