package net.mehvahdjukaar.amendments.client;

import com.google.gson.JsonParser;
import net.mehvahdjukaar.amendments.Amendments;
import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.mehvahdjukaar.amendments.common.CakeRegistry;
import net.mehvahdjukaar.amendments.configs.ClientConfigs;
import net.mehvahdjukaar.amendments.configs.CommonConfigs;
import net.mehvahdjukaar.amendments.integration.CompatHandler;
import net.mehvahdjukaar.amendments.mixins.SignRendererAccessor;
import net.mehvahdjukaar.moonlight.api.events.AfterLanguageLoadEvent;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.resources.RPUtils;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.mehvahdjukaar.moonlight.api.resources.StaticResource;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynClientResourcesGenerator;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicTexturePack;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceGenTask;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceSink;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicClientResourceProvider;
import net.mehvahdjukaar.moonlight.api.resources.pack.PackGenerationStrategy;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceGenTask;
import net.mehvahdjukaar.moonlight.api.resources.pack.ResourceSink;
import net.mehvahdjukaar.moonlight.api.resources.textures.*;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ClientResourceGenerator extends DynClientResourcesGenerator {

    public ClientResourceGenerator() {
        super(new DynamicTexturePack(Amendments.res("generated_pack")));
        this.dynamicPack.addNamespaces("minecraft");
        if (ClientConfigs.PIXEL_CONSISTENT_SIGNS.get()) {
            //super hack and not ideal at all
            PlatHelper.getInstalledMods().forEach(this.dynamicPack::addNamespaces);
        }
    }


    @Override
    public Logger getLogger() {
        return Amendments.LOGGER;
    }

    @Override
    public void regenerateDynamicAssets(Consumer<ResourceGenTask> executor) {
        WallLanternModelsManager.refreshModels(Minecraft.getInstance().getResourceManager());
        if (ClientConfigs.JUKEBOX_MODEL.get()) {
            executor.accept(this::generateJukeboxAssets);
        }

        if (CommonConfigs.DOUBLE_CAKES.get()) {
            executor.accept(this::generateDoubleCakesAssets);
        }

        if (ClientConfigs.SIGN_ATTACHMENT.get()) {
            executor.accept(this::generateHangingSignAssets);
        }

        if (ClientConfigs.PIXEL_CONSISTENT_SIGNS.get()) {
            executor.accept(this::generateSignTextures);
            if (CompatHandler.FARMERS_DELIGHT) executor.accept(this::generateFdSignTextures);
            executor.accept(this::generateSignBlockModels);
        }

        executor.accept((manager, sink) -> {

            if (ClientConfigs.COLORED_ARROWS.get()) {
                sink.addItemModel(new ResourceLocation("crossbow_arrow"), JsonParser.parseString(
                        """ 
                                {
                                    "parent": "item/crossbow",
                                    "textures": {
                                        "layer0": "item/crossbow_arrow_base",
                                        "layer1": "item/crossbow_arrow_tip"
                                    }
                                }
                                """));
            }

            if (ClientConfigs.JUKEBOX_MODEL.get()) {
                sink.addItemModel(new ResourceLocation("jukebox"), JsonParser.parseString(
                        """ 
                                {
                                  "parent": "amendments:block/jukebox"
                                }
                                """));

                sink.addBlockState(new ResourceLocation("jukebox"), JsonParser.parseString(
                        """ 
                                {
                                  "variants": {
                                    "has_record=true": {
                                      "model": "amendments:block/jukebox_on"
                                    },
                                    "has_record=false": {
                                      "model": "amendments:block/jukebox"
                                    }
                                  }
                                }
                                """));
            }
        });
    }

    private void generateSignTextures(ResourceManager manager, ResourceSink sink) {
        TextureCollager transformer = TextureCollager.builder(64, 32, 64, 16)
                .copyFrom(0, 16, 8, 16)
                .to(56, 0)
                .build();

        try (TextureImage template = TextureImage.open(manager,
                Amendments.res("block/signs/template"));
             TextureImage mask = TextureImage.open(manager,
                     Amendments.res("block/signs/mask"))) {

            Respriter respriter = Respriter.masked(template, mask);

            for (WoodType w : WoodTypeRegistry.INSTANCE.getValues()) {
                Block sing = w.getBlockOfThis("sign");
                if (sing == null) continue;

                ResourceLocation blockLocation = Amendments.res("block/signs/" + w.getVariantId("sign"));
                ResourceLocation signTextureLocation = findSignTexture(manager, w, sing, false);
                if (signTextureLocation == null) continue;
                sink.addTextureIfNotPresent(manager, blockLocation, () -> {
                    try (TextureImage signTexture = TextureImage.open(manager, signTextureLocation);
                         TextureImage modPlankTexture = TextureImage.open(manager,
                                 RPUtils.findFirstBlockTextureLocation(manager, w.planks));) {
                        List<Palette> palette = Palette.fromAnimatedImage(modPlankTexture);
                        for (var p : palette) {
                            p.remove(p.getLightest());
                        }

                        TextureImage newImage = respriter.recolorWithAnimation(palette, modPlankTexture.getMcMeta());
                        transformer.apply(signTexture, newImage);
                        return newImage;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

            }
        } catch (Exception e) {
            Amendments.LOGGER.warn("Failed to generate sign extension textures, ", e);
        }
    }

    @Nullable
    private static ResourceLocation findSignTexture(ResourceManager manager, WoodType w, Block sing, boolean hanging) {
        var vanilla = w.toVanilla();
        if (vanilla == null) {
            Amendments.LOGGER.error("Vanilla wood type for wood {} was null. This is a bug", w);
            return null;
        }
        Material signMaterial = hanging ? Sheets.getHangingSignMaterial(vanilla) :
                Sheets.getSignMaterial(vanilla);
        if (signMaterial == null) {
            try {
                BlockEntity be = ((EntityBlock) sing).newBlockEntity(BlockPos.ZERO, sing.defaultBlockState());
                BlockEntityRenderer<?> renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(be);
                if (renderer instanceof SignRendererAccessor sr) {
                    signMaterial = sr.invokeGetSignMaterial(vanilla);
                }
            } catch (Exception e) {
                Amendments.LOGGER.error("Failed to get sign material for wood (from block entity renderer) {}, ", w, e);
            }
        }
        //when all else fails, guess
        if (signMaterial == null) {
            ResourceLocation relativeLocation = w.getId().withPrefix("entity/signs/" + (hanging ? "hanging/" : ""));
            ResourceLocation id = ResType.TEXTURES.getPath(relativeLocation);
            if (manager.getResource(id).isPresent()) return relativeLocation;
        } else {
            return signMaterial.texture();
        }

        Amendments.LOGGER.error("Sign material for wood {} was null. " +
                "This is likely due to some mod calling Sheets.getSignMaterial too early or by some wood mod not registering their wood type properly by not adding it to the vanilla texture map. Sheets.getSignMaterial is NOT Nullable, i shouldn't even have this check.", w);
        return null;
    }

    private static String joinNonEmpty(String first, String second) {
        if (first.isEmpty()) return second;
        if (second.isEmpty()) return first;
        return first + "_" + second;
    }

    private void generateFdSignTextures(ResourceManager manager, ResourceSink sink) {
        //TODO:fix flip in top texture
        TextureCollager transformer = TextureCollager.builder(64, 32, 64, 16)
                .copyFrom(0, 16, 8, 16)
                .to(56, 0) //stick

                .copyFrom(0, 0, 32, 16)
                .to(0, 0)
                .copyFrom(0, 12, 28, 2)
                .to(0, 9)

                .copyFrom(26, 2, 2, 14)
                .to(18, 2)
                .copyFrom(24, 7, 2, 10)
                .to(16, 4)

                .copyFrom(23, 2, 3, 3)
                .to(15, 2)
                .copyFrom(28, 2, 24, 12)
                .to(20, 2)
                .copyFrom(28, 12, 24, 2)
                .to(20, 9)
                .copyFrom(50, 2, 2, 8)
                .to(34, 2)
                .build();


        List<String> names = new ArrayList<>();
        Arrays.stream(DyeColor.values()).forEach(d -> names.add(d.getName()));
        names.add("");
        for (String d : names) {
            ResourceLocation texturePath = ResourceLocation.fromNamespaceAndPath("farmersdelight",
                    joinNonEmpty("entity/signs/canvas", d));
            ResourceLocation blockTexturePath = Amendments.res("block/signs/farmersdelight/" + joinNonEmpty(d, "canvas_sign"));

            sink.addTextureIfNotPresent(manager, blockTexturePath, () -> {
                try (TextureImage vanillaTexture = TextureImage.open(manager, texturePath)){

                    TextureImage newImg = TextureImage.createNew(64, 16);
                    transformer.apply(vanillaTexture, newImg);
                    return newImg;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }


    private void generateSignBlockModels(ResourceManager manager, ResourceSink sink) {
        AmendmentsClient.SIGN_THAT_WE_RENDER_AS_BLOCKS.clear();
        StaticResource sign0 = StaticResource.getOrThrow(manager, ResType.BLOCK_MODELS.getPath(Amendments.res("signs/sign_oak_0")));
        StaticResource sign1 = StaticResource.getOrThrow(manager, ResType.BLOCK_MODELS.getPath(Amendments.res("signs/sign_oak_1")));
        StaticResource sign2 = StaticResource.getOrThrow(manager, ResType.BLOCK_MODELS.getPath(Amendments.res("signs/sign_oak_2")));
        StaticResource sign3 = StaticResource.getOrThrow(manager, ResType.BLOCK_MODELS.getPath(Amendments.res("signs/sign_oak_3")));
        StaticResource signWall = StaticResource.getOrThrow(manager, ResType.BLOCK_MODELS.getPath(Amendments.res("signs/sign_oak_wall")));
        StaticResource blockState = StaticResource.getOrThrow(manager, ResType.BLOCKSTATES.getPath(Amendments.res("sign_oak")));
        StaticResource blockStateWall = StaticResource.getOrThrow(manager, ResType.BLOCKSTATES.getPath(Amendments.res("sign_oak_wall")));
        String blockStateText = new String(blockState.data, StandardCharsets.UTF_8);
        String blockStateWallText = new String(blockStateWall.data, StandardCharsets.UTF_8);

        for (WoodType w : WoodTypeRegistry.INSTANCE.getValues()) {
            Block sign = w.getBlockOfThis("sign");
            Block wallSign = w.getBlockOfThis("wall_sign");
            if (sign == null || wallSign == null) continue;
            String variantId = w.getVariantId("sign");
            sink.addSimilarJsonResource(manager, sign0, "sign_oak", variantId);
            sink.addSimilarJsonResource(manager, sign1, "sign_oak", variantId);
            sink.addSimilarJsonResource(manager, sign2, "sign_oak", variantId);
            sink.addSimilarJsonResource(manager, sign3, "sign_oak", variantId);
            sink.addSimilarJsonResource(manager, signWall, "sign_oak", variantId);

            sink.addBytes(Utils.getID(sign), blockStateText.replace("sign_oak", variantId).getBytes(), ResType.BLOCKSTATES);
            sink.addBytes(Utils.getID(wallSign), blockStateWallText.replace("sign_oak", variantId).getBytes(), ResType.BLOCKSTATES);

            AmendmentsClient.SIGN_THAT_WE_RENDER_AS_BLOCKS.add(sign);
            AmendmentsClient.SIGN_THAT_WE_RENDER_AS_BLOCKS.add(wallSign);
        }

        List<String> names = new ArrayList<>();
        Arrays.stream(DyeColor.values()).forEach(d -> names.add(d.getName() + "_"));
        names.add("");
        if (CompatHandler.FARMERS_DELIGHT) {
            for (Block canvas : BlockScanner.getInstance().getFdSigns()) {
                ResourceLocation id = Utils.getID(canvas);
                Block canvasWall = BuiltInRegistries.BLOCK.getOptional(
                                id.withPath(p -> p.replace("sign", "wall_sign")))
                        .orElse(null);
                if (canvasWall == null) continue;
                ResourceLocation canvasWallId = Utils.getID(canvasWall);
                String variantId = "farmersdelight/" + id.getPath();
                sink.addSimilarJsonResource(manager, sign0, "sign_oak", variantId);
                sink.addSimilarJsonResource(manager, sign1, "sign_oak", variantId);
                sink.addSimilarJsonResource(manager, sign2, "sign_oak", variantId);
                sink.addSimilarJsonResource(manager, sign3, "sign_oak", variantId);
                sink.addSimilarJsonResource(manager, signWall, "sign_oak", variantId);

                sink.addBytes(id, blockStateText.replace("sign_oak", variantId).getBytes(), ResType.BLOCKSTATES);
                sink.addBytes(canvasWallId, blockStateWallText.replace("sign_oak", variantId).getBytes(), ResType.BLOCKSTATES);

                AmendmentsClient.SIGN_THAT_WE_RENDER_AS_BLOCKS.add(canvas);
                AmendmentsClient.SIGN_THAT_WE_RENDER_AS_BLOCKS.add(canvasWall);
            }
        }
    }


    private void generateHangingSignAssets(ResourceManager manager, ResourceSink sink) {
        TextureCollager transformer = TextureCollager.builder(32, 64, 16, 16)
                .copyFrom(26, 0, 2, 4).to(4, 0)
                .copyFrom(26, 8, 6, 8).to(4, 4)
                .copyFrom(28, 24, 4, 8).to(0, 4)
                .copyFrom(26, 20, 2, 4).to(6, 0)
                .copyFrom(26, 28, 2, 8).to(10, 4)
                .flippedX()
                .build();

        for (WoodType w : WoodTypeRegistry.INSTANCE.getValues()) {
            Block hangingSign = w.getBlockOfThis("hanging_sign");
            if (hangingSign == null) continue;
            //hanging sign extension textures
            ResourceLocation signTexturePath = findSignTexture(manager, w, hangingSign, true);
            if (signTexturePath == null) continue;
            try (TextureImage vanillaTexture = TextureImage.open(manager, signTexturePath);
                 TextureImage rotated = TextureOps.createRotated(vanillaTexture, Rotation.CLOCKWISE_90);
                 TextureImage newIm = TextureOps.createScaled(vanillaTexture, 0.25f, 0.5f)) {

                newIm.clear();
                transformer.apply(rotated, newIm);

                sink.addTexture(Amendments.res("entity/signs/hanging/" + w.getVariantId("extension")), newIm);
            } catch (Exception e) {
                Amendments.LOGGER.warn("Failed to generate hanging sign extension texture for {}, ", w, e);
            }
        }
        if (CompatHandler.FARMERS_DELIGHT) {
            //hanging sign extension textures
            try (TextureImage vanillaTexture = TextureImage.open(manager,
                    new ResourceLocation("farmersdelight:entity/signs/hanging/canvas"));
                 TextureImage rotated = TextureOps.createRotated(vanillaTexture, Rotation.CLOCKWISE_90);
                 TextureImage newIm = TextureOps.createScaled(rotated, 0.5f, 0.25f)) {

                newIm.clear();
                transformer.apply(rotated, newIm);
                sink.addTexture(Amendments.res("entity/signs/hanging/farmersdelight/extension_canvas"), newIm);
            } catch (Exception e) {
                Amendments.LOGGER.warn("Failed to generate hanging sign extension texture for {}, ", "canvas sign", e);
            }
        }
    }

    private void generateJukeboxAssets(ResourceManager manager, ResourceSink sink) {
        TextureCollager transformer = TextureCollager.builder(16, 16, 16, 16)
                .copyFrom(5, 6, 3, 2)
                .to(6, 6)
                .copyFrom(8, 6, 1, 1)
                .to(9, 7)
                .copyFrom(7, 7, 3, 2)
                .to(7, 8)
                .copyFrom(6, 8, 1, 1)
                .to(6, 8)
                .copyFrom(9, 6, 1, 1)
                .to(9, 6)
                .copyFrom(5, 8, 1, 1)
                .to(6, 9)
                .build();


        try (TextureImage fallback = TextureImage.open(manager,
                Amendments.res("block/music_discs/music_disc_generic"));
             TextureImage template = TextureImage.open(manager,
                     Amendments.res("block/music_discs/music_disc_template"));
             TextureImage mask = TextureImage.open(manager,
                     Amendments.res("block/music_discs/music_disc_mask"));) {
            Respriter respriter = Respriter.of(template);

            for (var e : AmendmentsClient.getAllRecords().entrySet()) {
                ResourceLocation texturePath = Amendments.res(e.getValue().texture().getPath());
                if (sink.alreadyHasTextureAtLocation(manager, texturePath)) continue;
                //hanging sign extension textures
                try (TextureImage vanillaTexture = TextureImage.open(manager,
                        RPUtils.findFirstItemTextureLocation(manager, e.getKey()))) {

                    Palette p = Palette.fromImage(vanillaTexture, mask);
                    amendJukeboxPalette(p);
                    try (TextureImage newImage = respriter.recolor(p)) {
                        transformer.apply(vanillaTexture, newImage);

                        if (newImage.getPixel(6, 6) == p.get(p.size() - 2).rgb().toInt()) {
                            newImage.setPixel(6, 6, p.getLightest().value());
                            newImage.setPixel(9, 9, p.getLightest().value());
                        }
                        sink.addTexture(texturePath, newImage);
                    }
                } catch (Exception ex) {
                    getLogger().warn("Failed to generate record item texture for {}. No model / texture found", e.getKey());
                    sink.addTexture(texturePath, fallback);
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void amendJukeboxPalette(Palette p) {
        float averLum = p.getAverageLuminanceStep();
        if (averLum > 0.06) {
            p.increaseInner();
        }
        var darkest = p.getDarkest();
        var beforeDarkest = p.get(1);
        if (beforeDarkest.luminance() - darkest.luminance() > averLum - 0.005) {
            p.remove(darkest);
            p.increaseDown();
        }
    }


    private void generateDoubleCakesAssets(ResourceManager manager, ResourceSink sink) {
        StaticResource[] cakeModels = Stream.of("full", "slice1", "slice2", "slice3", "slice4", "slice5", "slice6")
                .map(s -> StaticResource.getOrLog(manager,
                        ResType.BLOCK_MODELS.getPath(Amendments.res("double_cake/vanilla_" + s)))).toArray(StaticResource[]::new);

        StaticResource doubleCakeModelState = StaticResource.getOrLog(manager,
                ResType.BLOCKSTATES.getPath(Amendments.res("double_cake")));
        for (var t : CakeRegistry.INSTANCE.getValues()) {
            if (!t.isVanilla()) {
                try {
                    ResourceLocation dcId = Utils.getID(t.getBlockOfThis("double_cake"));
                    ResourceLocation top = RPUtils.findFirstBlockTextureLocation(manager, t.cake, s -> s.contains("top"));
                    ResourceLocation side = RPUtils.findFirstBlockTextureLocation(manager, t.cake, s -> s.contains("side"));
                    ResourceLocation bottom = RPUtils.findFirstBlockTextureLocation(manager, t.cake, s -> s.contains("bottom"));
                    ResourceLocation inner = RPUtils.findFirstBlockTextureLocation(manager, t.cake, s -> s.contains("inner"));

                    for (var m : cakeModels) {
                        sink.addSimilarJsonResource(manager, m, s -> s
                                        .replace("amendments:block/double_cake", "")
                                        .replace("minecraft:block/cake", "")
                                        .replace("\"/", "\"amendments:block/double_cake/")
                                        .replace("_top", top.toString())
                                        .replace("_side", side.toString())
                                        .replace("_inner", inner.toString())
                                        .replace("_bottom", bottom.toString()),
                                s -> s.replace("vanilla", dcId.getPath()));
                    }
                    sink.addSimilarJsonResource(manager, doubleCakeModelState,
                            s -> s.replace("vanilla", dcId.getPath()),
                            s -> s.replace("double_cake", dcId.getPath()));
                } catch (Exception e) {
                    Amendments.LOGGER.error("Failed to generate model for double cake {},", t);
                }
            }
        }
    }


    @Override
    public void addDynamicTranslations(AfterLanguageLoadEvent languageEvent) {
        if (languageEvent.isDefault()) {
            languageEvent.addEntry("item.minecraft.lingering_potion.effect.empty", "Lingering Mixed Potion");
            languageEvent.addEntry("item.minecraft.splash_potion.effect.empty", "Splash Mixed Potion");
            languageEvent.addEntry("item.minecraft.potion.effect.empty", "Mixed Potion");
        }
    }
}
