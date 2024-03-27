package net.mehvahdjukaar.amendments.integration;

import com.google.common.base.Suppliers;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class CompatObjects {

    public static final Supplier<Item> SOUL_CANDLE_ITEM = make("buzzier_bees:soul_candle", BuiltInRegistries.ITEM);

    public static final Supplier<Block> SOUL_CANDLE = make("buzzier_bees:soul_candle", BuiltInRegistries.BLOCK);

    public static final Supplier<Item> SPECTACLE_CANDLE_ITEM = make("cave_enhancements:spectacle_candle", BuiltInRegistries.ITEM);

    public static final Supplier<Item> ETCHED_DISC = make("etched:etched_music_disc", BuiltInRegistries.ITEM);

    public static final Supplier<Item> CAKE_SLICE = make("farmersdelight:cake_slice", BuiltInRegistries.ITEM);

    public static final Supplier<Block> SPECTACLE_CANDLE = make("cave_enhancements:spectacle_candle", BuiltInRegistries.BLOCK);

    public static final Supplier<ParticleType<?>> SMALL_SOUL_FLAME = make("buzzier_bees:small_soul_fire_flame", BuiltInRegistries.PARTICLE_TYPE);


    private static <T> Supplier<@Nullable T> make(String name, Registry<T> registry) {
        return Suppliers.memoize(() -> registry.getOptional(new ResourceLocation(name)).orElse(null));
    }

}
