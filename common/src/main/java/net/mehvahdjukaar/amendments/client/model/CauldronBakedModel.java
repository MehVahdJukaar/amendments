package net.mehvahdjukaar.amendments.client.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.mehvahdjukaar.amendments.common.block.ModCauldronBlock;
import net.mehvahdjukaar.amendments.common.tile.LiquidCauldronBlockTile;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.moonlight.api.client.model.CustomBlockModel;
import net.mehvahdjukaar.moonlight.api.client.model.CustomUnbakedModel;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.client.model.QuadEmitter;
import net.mehvahdjukaar.moonlight.api.fluids.MLBuiltinSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidRegistry;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CauldronBakedModel implements CustomBlockModel {

    private final BlockStateModel cauldron;
    private final BlockStateModel fluid;
    private final boolean translucent;

    public CauldronBakedModel(BlockStateModel cauldron, BlockStateModel fluid, boolean translucent) {
        this.cauldron = cauldron;
        this.fluid = fluid;
        this.translucent = translucent;
    }

    @Override
    public void emitQuads(QuadEmitter emitter, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos,
                          @Nullable BlockState state, RandomSource random, ExtraModelData data) {
        emitter.emitAll(this.cauldron, level, pos, state, random);

        ClientLevel clientLevel = Minecraft.getInstance().level;
        if (clientLevel == null || state == null) return;

        var liquidQuads = CustomBlockModel.collectQuads(this.fluid, level, pos, state, random);
        if (liquidQuads.isEmpty()) return;

        RegistryAccess access = clientLevel.registryAccess();
        ResourceKey<SoftFluid> fluidKey = data.get(LiquidCauldronBlockTile.FLUID);
        if (fluidKey == null) {
            if (state.getBlock() instanceof ModCauldronBlock) return;
            for (BakedQuad q : liquidQuads) {
                emitter.fromQuad(q).ambientOcclusion(false).forceTranslucent(this.translucent).emit();
            }
            return;
        }

        SoftFluid fluid = SoftFluidRegistry.get(access).get(fluidKey);
        // fluid might not be received immediately so it can be empty for a split second
        if (fluid == null || MLBuiltinSoftFluids.EMPTY.is(fluidKey)) return;

        Boolean glowing = data.get(LiquidCauldronBlockTile.GLOWING);
        if (glowing == null) glowing = false;

        Identifier stillTexture = fluid.getStillTexture();
        if (ClientConfigs.POTION_TEXTURE.get() && MLBuiltinSoftFluids.POTION.is(fluidKey)) {
            stillTexture = AmendmentsClient.POTION_TEXTURE;
        } else if (MLBuiltinSoftFluids.MUSHROOM_STEW.is(fluidKey)) {
            stillTexture = AmendmentsClient.MUSHROOM_STEW;
        } else if (MLBuiltinSoftFluids.BEETROOT_SOUP.is(fluidKey)) {
            stillTexture = AmendmentsClient.BEETROOT_SOUP;
        } else if (MLBuiltinSoftFluids.RABBIT_STEW.is(fluidKey)) {
            stillTexture = AmendmentsClient.RABBIT_STEW;
        } else if (MLBuiltinSoftFluids.SUS_STEW.is(fluidKey)) {
            stillTexture = AmendmentsClient.SUS_STEW;
        }

        TextureAtlasSprite sprite = ClientHelper.getBlockMaterial(stillTexture).sprite();
        int emissivity = Math.max(glowing ? 14 : 0, fluid.getEmissivity());
        for (BakedQuad q : liquidQuads) {
            emitter.fromQuad(q)
                    .sprite(sprite)
                    .ambientOcclusion(false)
                    .lightEmission(emissivity)
                    .forceTranslucent(this.translucent)
                    .emit();
        }
    }

    @Override
    public TextureAtlasSprite getParticle(ExtraModelData data) {
        return this.cauldron.particleMaterial().sprite();
    }

    @Override
    public @Nullable Object geometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state,
                                        RandomSource random, ExtraModelData data) {
        return new Key(data.get(LiquidCauldronBlockTile.FLUID), data.get(LiquidCauldronBlockTile.GLOWING), state);
    }

    private record Key(@Nullable ResourceKey<SoftFluid> fluid, @Nullable Boolean glowing, BlockState state) {
    }

    public record Unbaked(BlockStateModel.Unbaked cauldron, BlockStateModel.Unbaked fluid, boolean translucent)
            implements CustomUnbakedModel {

        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                BlockStateModel.Unbaked.CODEC.fieldOf("cauldron").forGetter(Unbaked::cauldron),
                BlockStateModel.Unbaked.CODEC.fieldOf("fluid").forGetter(Unbaked::fluid),
                Codec.BOOL.optionalFieldOf("translucent", false).forGetter(Unbaked::translucent)
        ).apply(i, Unbaked::new));

        @Override
        public CustomBlockModel bake(ModelBaker baker) {
            return new CauldronBakedModel(this.cauldron.bake(baker), this.fluid.bake(baker), this.translucent);
        }

        @Override
        public MapCodec<? extends CustomUnbakedModel> codec() {
            return CODEC;
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            this.cauldron.resolveDependencies(resolver);
            this.fluid.resolveDependencies(resolver);
        }
    }
}
