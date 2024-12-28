package net.mehvahdjukaar.amendments.integration;

import com.google.common.base.Suppliers;
import gg.moonflower.etched.common.component.DiscAppearanceComponent;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
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
    public static final Supplier<Block> ENDER_CANDLE = make("buzzier_bees:ender_candle", BuiltInRegistries.BLOCK);
    public static final Supplier<Block> CUPRIC_CANDLE = make("caves_and_chasms:cupric_candle", BuiltInRegistries.BLOCK);

    public static final Supplier<Item> SPECTACLE_CANDLE_ITEM = make("cave_enhancements:spectacle_candle", BuiltInRegistries.ITEM);

    public static final Supplier<Item> ETCHED_DISC = make("etched:etched_music_disc", BuiltInRegistries.ITEM);

    public static final Supplier<Item> CAKE_SLICE = make("farmersdelight:cake_slice", BuiltInRegistries.ITEM);
    public static final Supplier<Item> SOUL_CANDLE_HOLDER = make("supplementaries:candle_holder_soul", BuiltInRegistries.ITEM);

    public static final Supplier<Item> SCONCE_LEVER = make("supplementaries:sconce_lever", BuiltInRegistries.ITEM);
    public static final Supplier<Item> SCONCE = make("supplementaries:sconce", BuiltInRegistries.ITEM);

    public static final Supplier<Block> SPECTACLE_CANDLE = make("cave_enhancements:spectacle_candle", BuiltInRegistries.BLOCK);

    public static final Supplier<ParticleType<?>> SMALL_SOUL_FLAME = make("buzzier_bees:small_soul_fire_flame", BuiltInRegistries.PARTICLE_TYPE);
    public static final Supplier<DataComponentType<?>> DISC_APPEARANCE = make("etched:disc_appearance", BuiltInRegistries.DATA_COMPONENT_TYPE);


    private static <T> Supplier<@Nullable T> make(String name, Registry<T> registry) {
        return Suppliers.memoize(() -> registry.getOptional(ResourceLocation.tryParse(name)).orElse(null));
    }

}
