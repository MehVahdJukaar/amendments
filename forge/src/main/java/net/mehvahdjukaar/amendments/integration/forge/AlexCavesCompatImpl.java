package net.mehvahdjukaar.amendments.integration.forge;

import com.github.alexmodguy.alexscaves.client.particle.ACParticleRegistry;
import com.github.alexmodguy.alexscaves.server.block.ACBlockRegistry;
import com.github.alexmodguy.alexscaves.server.block.fluid.ACFluidRegistry;
import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import com.github.alexthe666.citadel.server.block.LecternBooks;
import com.github.alexthe666.rats.registry.RatsCauldronRegistry;
import fuzs.puzzleslib.impl.item.crafting.ForgeCombinedIngredients;
import net.mehvahdjukaar.amendments.common.block.LiquidCauldronBlock;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.misc.DataObjectReference;
import net.mehvahdjukaar.moonlight.api.resources.recipe.BlockTypeSwapIngredient;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.checkerframework.checker.units.qual.A;

import java.lang.reflect.Method;

public class AlexCavesCompatImpl {

    public static DataObjectReference<SoftFluid> ACID = new DataObjectReference<>(
            new ResourceLocation("alexscaves:acid"), SoftFluidRegistry.KEY);

    public static final Method SET_H = ObfuscationReflectionHelper.findMethod(
            Entity.class, "setFluidTypeHeight",
            FluidType.class, double.class);

    static {
        SET_H.setAccessible(true);
    }

    public static void acidDamage(SoftFluidStack fluid, Level level, BlockPos pos, BlockState state, Entity entity) {
        if (fluid.is(ACID.get())) {

            try {
                //hack
                FluidType acidFluid = ACFluidRegistry.ACID_FLUID_TYPE.get();
                double oldH = entity.getFluidTypeHeight(acidFluid);
                double stateH = state.getValue(LiquidCauldronBlock.LEVEL) * 0.25;
                SET_H.invoke(entity, acidFluid, stateH);

                LiquidBlock acid = ACBlockRegistry.ACID.get();
                acid.defaultBlockState().entityInside(level, pos, entity);

                SET_H.invoke(entity, acidFluid, oldH);
            }catch (Exception ignored) {
            }
        }
    }

    public static void acidParticles(SoftFluidStack fluid, Level level, BlockPos pos, RandomSource rand, double height) {
        if (fluid.is(ACID.get())) {
            if (rand.nextInt(400) == 0) {
                level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, ACSoundRegistry.ACID_IDLE.get(),
                        SoundSource.BLOCKS, 0.5F, rand.nextFloat() * 0.4F + 0.8F, false);
            }
            LiquidCauldronBlock.addSurfaceParticles(ACParticleRegistry.ACID_BUBBLE.get(),
                    level, pos, 1, height, rand,
                    (rand.nextFloat() - 0.5f) * 0.1f,
                    0.05F + rand.nextFloat() * 0.1f,
                    (rand.nextFloat() - 0.5f) * 0.1f
            );
        }
    }
}
