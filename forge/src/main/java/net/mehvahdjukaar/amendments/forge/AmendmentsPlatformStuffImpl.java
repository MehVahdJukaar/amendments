package net.mehvahdjukaar.amendments.forge;

import net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile;
import net.mehvahdjukaar.amendments.reg.ModRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.fluids.forge.SoftFluidTankImpl;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AmendmentsPlatformStuffImpl {
    public static List<BakedQuad> removeAmbientOcclusion(List<BakedQuad> supportQuads) {
        var newList = new ArrayList<BakedQuad>();
        for(var quad : supportQuads){
            int[] vertices = quad.getVertices();
            newList.add( new BakedQuad(
                    Arrays.copyOf(vertices, vertices.length), quad.getTintIndex(), quad.getDirection(), quad.getSprite(),
                    true, false
            ));
        }
        return newList;
    }

    public static SoftFluidTank createCauldronLiquidTank() {
        return new SoftFluidTankImpl(4){

            @Override
            public boolean canAddSoftFluid(SoftFluid s, int count, @Nullable CompoundTag nbt) {
                if(s == BuiltInSoftFluids.POTION.get() || s == ModRegistry.DYE_SOFT_FLUID.get()) {
                    return this.canAdd(count) && this.getFluid().equals(s); //discard nbt
                }
                else return super.canAddSoftFluid(s,count, nbt);
            }

            @Override
            protected void addFluidOntoExisting(SoftFluid incoming, int amount, @Nullable CompoundTag tag) {
                if(incoming == BuiltInSoftFluids.POTION.get() && !areNbtEquals(tag, this.nbt)) {
                    LiquidCauldronBlockTile.mixPotions(this, incoming, amount, tag);
                }else if(incoming == ModRegistry.DYE_SOFT_FLUID.get()){
                    LiquidCauldronBlockTile.mixDye(this, incoming, amount, tag);
                }
                super.addFluidOntoExisting(incoming, amount, tag);
            }
        };
    }

    public static SoftFluidTank createCauldronDyeTank() {
        return new SoftFluidTankImpl(3){

            @Override
            public boolean canAddSoftFluid(SoftFluid s, int count, @Nullable CompoundTag nbt) {
                if(s == BuiltInSoftFluids.POTION.get() || s == ModRegistry.DYE_SOFT_FLUID.get()) {
                    return this.canAdd(count) && this.getFluid().equals(s); //discard nbt
                }
                else return super.canAddSoftFluid(s,count, nbt);
            }

            @Override
            protected void addFluidOntoExisting(SoftFluid incoming, int amount, @Nullable CompoundTag tag) {
                if(incoming == BuiltInSoftFluids.POTION.get() && !areNbtEquals(tag, this.nbt)) {
                    LiquidCauldronBlockTile.mixPotions(this, incoming, amount, tag);
                }else if(incoming == ModRegistry.DYE_SOFT_FLUID.get()){
                    LiquidCauldronBlockTile.mixDye(this, incoming, amount, tag);
                }
                super.addFluidOntoExisting(incoming, amount, tag);
            }
        };
    }

}
