package net.mehvahdjukaar.amendments.integration.neoforge;

import net.mehvahdjukaar.amendments.common.block.LiquidCauldronBlock;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.misc.DataObjectReference;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.resources.recipe.BlockTypeSwapIngredient;
import net.mehvahdjukaar.supplementaries.client.renderers.entities.CannonballRenderer;
import net.mehvahdjukaar.moonlight.api.misc.HolderReference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.lang.reflect.Method;

public class AlexCavesCompatImpl {

    public static HolderReference<SoftFluid> ACID_SF = HolderReference.of(
            ResourceLocation.fromNamespaceAndPath("alexscaves", "acid"), SoftFluidRegistry.KEY);

    public static HolderReference<FluidType> ACID_FLUID_TYPE = HolderReference.of(
            ResourceLocation.fromNamespaceAndPath("alexscaves", "acid"), NeoForgeRegistries.Keys.FLUID_TYPES);

    public static HolderReference<Block> ACID_BLOCK = HolderReference.of(
            ResourceLocation.fromNamespaceAndPath("alexscaves", "acid"), Registries.BLOCK);

    public static HolderReference<ParticleType<?>> ACID_BUBBLE = HolderReference.of(
            ResourceLocation.fromNamespaceAndPath("alexscaves", "acid_bubble"), Registries.PARTICLE_TYPE);

    public static HolderReference<SoundEvent> ACID_IDLE = HolderReference.of(
            ResourceLocation.fromNamespaceAndPath("alexscaves", "acid_idle"), Registries.SOUND_EVENT);


    public static final Method SET_H = ObfuscationReflectionHelper.findMethod(
            Entity.class, "setFluidTypeHeight",
            FluidType.class, double.class);

    static {
        SET_H.setAccessible(true);
    }

    public static void acidDamage(SoftFluidStack fluid, Level level, BlockPos pos, BlockState state, Entity entity) {
        if (fluid.is(ACID_SF)) {

            try {
                //hack
                FluidType acidFluid = ACID_FLUID_TYPE.get(level);
                double oldH = entity.getFluidTypeHeight(acidFluid);
                double stateH = state.getValue(LiquidCauldronBlock.LEVEL) * 0.25;
                SET_H.invoke(entity, acidFluid, stateH);

                Block acid = ACID_BLOCK.get(level);
                acid.defaultBlockState().entityInside(level, pos, entity);

                SET_H.invoke(entity, acidFluid, oldH);
            } catch (Exception ignored) {
            }
        }
    }

    public static void acidParticles(SoftFluidStack fluid, Level level, BlockPos pos, RandomSource rand, double height) {
        if (fluid.is(ACID_SF)) {
            if (rand.nextInt(400) == 0) {
                level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, ACID_IDLE.get(level),
                        SoundSource.BLOCKS, 0.5F, rand.nextFloat() * 0.4F + 0.8F, false);
            }
            LiquidCauldronBlock.addSurfaceParticles((ParticleOptions) ACID_BUBBLE.get(level),
                    level, pos, 1, height, rand,
                    (rand.nextFloat() - 0.5f) * 0.1f,
                    0.05F + rand.nextFloat() * 0.1f,
                    (rand.nextFloat() - 0.5f) * 0.1f
            );
        }
    }
}
