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
import net.mehvahdjukaar.moonlight.api.resources.textures.ImageTransformer;
import net.mehvahdjukaar.moonlight.api.resources.textures.Palette;
import net.mehvahdjukaar.moonlight.api.resources.textures.Respriter;
import net.mehvahdjukaar.moonlight.api.resources.textures.TextureImage;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.supplementaries.client.renderers.color.ColorHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.logging.log4j.Logger;

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
            executor.accept(this::generateSignAssets);
            executor.accept(this::generateFdSignAssets);
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

    @Override
    public void regenerateDynamicAssets(ResourceManager manager) {

        //need this here for reasons I forgot
        WallLanternModelsManager.refreshModels(manager);
        super.regenerateDynamicAssets(manager);
    }

    private void generateFdSignAssets(ResourceManager manager, ResourceSink sink){
        ImageTransformer transformer = ImageTransformer.builder(64, 32, 64, 32)
                .copyRect(0, 12, 28, 2, 0, 9)
                .copyRect(26, 2, 2, 14, 18, 2)
                .copyRect(24, 7, 2, 10, 16, 4)

                .copyRect(23, 2, 3, 3, 15, 2)
                .copyRect(28, 2, 24, 12, 20, 2)
                .copyRect(28, 12, 24, 2, 20, 9)
                .copyRect(50, 2, 2, 8, 34, 2)
                .build();

        List<String> names = new ArrayList<>();
        Arrays.stream(DyeColor.values()).forEach(d -> names.add("_"+d.getName()));
        names.add("");
        for(var d : names){
            ResourceLocation res = new ResourceLocation( "farmersdelight:entity/signs/canvas"+ d);

            try (TextureImage vanillaTexture = TextureImage.open(manager, res)) {
                TextureImage newImage = vanillaTexture.makeCopy();
                transformer.apply(vanillaTexture, newImage);
                sink.addAndCloseTexture(res, newImage);
            } catch (Exception e) {
                Amendments.LOGGER.warn("Failed to generate Farmers Delight sign extension texture for {}, ", d, e);
            }
        }
    }

    private void generateSignAssets(ResourceManager manager, ResourceSink sink) {
        ImageTransformer transformer = ImageTransformer.builder(64, 32, 64, 32)
                .copyRect(0, 16, 16, 16, 0, 16)
                .build();

        try (TextureImage template = TextureImage.open(manager,
                Amendments.res("entity/sign/template"));
             TextureImage mask = TextureImage.open(manager,
                     Amendments.res("entity/sign/mask"))) {

            Respriter respriter = Respriter.masked(template, mask);

            for (WoodType w : WoodTypeRegistry.INSTANCE.getValues()) {
                Block sing = w.getBlockOfThis("sign");
                if (sing == null) continue;

                net.minecraft.world.level.block.state.properties.WoodType vanilla = w.toVanilla();
                if (vanilla == null) {
                    Amendments.LOGGER.error("Vanilla wood type for wood {} was null. This is a bug", w);
                    continue;
                }
                Material signMaterial = Sheets.getSignMaterial(vanilla);
                if (signMaterial == null) {
                    try {
                        BlockEntity be = ((EntityBlock) sing).newBlockEntity(BlockPos.ZERO, sing.defaultBlockState());
                        BlockEntityRenderer<?> renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(be);
                        if (renderer instanceof SignRendererAccessor sr) {
                            signMaterial = sr.invokeGetSignMaterial(vanilla);
                        }
                    } catch (Exception e) {
                        Amendments.LOGGER.error("Failed to get sign material for wood (from block entity renderer) {}, ", w, e);
                        continue;
                    }
                }
                if (signMaterial == null) {
                    Amendments.LOGGER.error("Sign material for wood {} was null. " +
                            "This is likely due to some mod calling Sheets.getSignMaterial too early or by some wood mod not registering their wood type properly by not adding it to the vanilla texture map. Sheets.getSignMaterial is NOT Nullable, i shouldn't even have this check.", w);
                    continue;
                }
                try (TextureImage vanillaTexture = TextureImage.open(manager,
                        signMaterial.texture());
                     TextureImage modPlankTexture = TextureImage.open(manager,
                             RPUtils.findFirstBlockTextureLocation(manager, w.planks));) {
                    List<Palette> palette = Palette.fromAnimatedImage(modPlankTexture);
                    for (var p : palette) {
                        p.remove(p.getLightest());
                    }
                    TextureImage newImage = respriter.recolorWithAnimationOf(modPlankTexture);
                    transformer.apply(vanillaTexture, newImage);
                    sink.addAndCloseTexture(signMaterial.texture(), newImage);
                } catch (Exception e) {
                    Amendments.LOGGER.warn("Failed to generate hanging sign extension texture for {}, ", w, e);
                }

            }
        } catch (Exception e) {
            int aa = 1;
        }
    }

    private void generateHangingSignAssets(ResourceManager manager, ResourceSink sink) {
        ImageTransformer transformer = ImageTransformer.builder(32, 64, 16, 16)
                .copyRect(26, 0, 2, 4, 4, 0)
                .copyRect(26, 8, 6, 8, 4, 4)
                .copyRect(28, 24, 4, 8, 0, 4)
                .copyRect(26, 20, 2, 4, 6, 0)
                //cheaty as it has to be flipped. todo: find a way to rotate it instead as this work wotk with packs
                .copyRect(26, 28, 1, 8, 11, 4)
                .copyRect(27, 28, 1, 8, 10, 4)
                .build();

        for (WoodType w : WoodTypeRegistry.INSTANCE.getValues()) {
            Block hangingSign = w.getBlockOfThis("hanging_sign");
            if (hangingSign == null) continue;
            //hanging sign extension textures
            net.minecraft.world.level.block.state.properties.WoodType vanilla = w.toVanilla();
            if (vanilla == null) {
                Amendments.LOGGER.error("Vanilla wood type for wood {} was null. This is a bug", w);
                continue;
            }
            Material hangingSignMaterial = Sheets.getHangingSignMaterial(vanilla);
            if (hangingSignMaterial == null) {
                try {
                    BlockEntity be = ((EntityBlock) hangingSign).newBlockEntity(BlockPos.ZERO, hangingSign.defaultBlockState());
                    BlockEntityRenderer<?> renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(be);
                    if (renderer instanceof SignRendererAccessor sr) {
                        hangingSignMaterial = sr.invokeGetSignMaterial(vanilla);
                    }
                } catch (Exception e) {
                    Amendments.LOGGER.error("Failed to get hanging sign material for wood (from block entity renderer) {}, ", w, e);
                    continue;
                }
            }
            if (hangingSignMaterial == null) {
                Amendments.LOGGER.error("Hanging sign material for wood {} was null. " +
                        "This is likely due to some mod calling Sheets.getHangingSignMaterial too early or by some wood mod not registering their wood type properly by not adding it to the vanilla texture map. Sheets.getHangingSignMaterial is NOT Nullable, i shouldn't even have this check.", w);
                continue;
            }
            try (TextureImage vanillaTexture = TextureImage.open(manager,
                    hangingSignMaterial.texture())) {
                TextureImage flipped = vanillaTexture.createRotated(Rotation.CLOCKWISE_90);
                TextureImage newIm = flipped.createResized(0.5f, 0.25f);
                newIm.clear();

                transformer.apply(flipped, newIm);
                flipped.close();
                sink.addAndCloseTexture(Amendments.res("entity/signs/hanging/" + w.getVariantId("extension")), newIm);
            } catch (Exception e) {
                Amendments.LOGGER.warn("Failed to generate hanging sign extension texture for {}, ", w, e);
            }
        }
        if (CompatHandler.FARMERS_DELIGHT) {
            //hanging sign extension textures
            try (TextureImage vanillaTexture = TextureImage.open(manager,
                    new ResourceLocation("farmersdelight:entity/signs/hanging/canvas"))) {
                TextureImage flipped = vanillaTexture.createRotated(Rotation.CLOCKWISE_90);
                TextureImage newIm = flipped.createResized(0.5f, 0.25f);
                newIm.clear();

                transformer.apply(flipped, newIm);
                flipped.close();
                sink.addAndCloseTexture(Amendments.res("entity/signs/hanging/farmersdelight/extension_canvas"), newIm);
            } catch (Exception e) {
                Amendments.LOGGER.warn("Failed to generate hanging sign extension texture for {}, ", "canvas sign", e);
            }
        }
    }

    private void generateJukeboxAssets(ResourceManager manager, ResourceSink sink) {

        ImageTransformer transformer = ImageTransformer.builder(16, 16, 16, 16)
                .copyRect(5, 6, 3, 2, 6, 6)
                .copyRect(8, 6, 1, 1, 9, 7)
                .copyRect(7, 7, 3, 2, 7, 8)
                .copyRect(6, 8, 1, 1, 6, 8)
                .copyRect(9, 6, 1, 1, 9, 6)
                .copyRect(5, 8, 1, 1, 6, 9)
                .build();
        try (TextureImage fallback = TextureImage.open(manager,
                Amendments.res("block/music_discs/music_disc_generic"));
             TextureImage template = TextureImage.open(manager,
                     Amendments.res("block/music_discs/music_disc_template"));
             TextureImage mask = TextureImage.open(manager,
                     Amendments.res("block/music_discs/music_disc_mask"));) {
            Respriter respriter = Respriter.of(template);

            for (var e : AmendmentsClient.getAllRecords().entrySet()) {
                ResourceLocation res = Amendments.res(e.getValue().texture().getPath());
                if (sink.alreadyHasTextureAtLocation(manager, res)) continue;
                //hanging sign extension textures
                try (TextureImage vanillaTexture = TextureImage.open(manager,
                        RPUtils.findFirstItemTextureLocation(manager, e.getKey()))) {

                    var p = Palette.fromImage(vanillaTexture, mask);
                    amendPalette(p);
                    TextureImage newImage = respriter.recolor(p);
                    transformer.apply(vanillaTexture, newImage);
                    if (newImage.getImage().getPixelRGBA(6, 6) == p.get(p.size() - 2).rgb().toInt()) {
                        newImage.setFramePixel(0, 6, 6, p.getLightest().rgb().toInt());
                        newImage.setFramePixel(0, 9, 9, p.getLightest().rgb().toInt());
                    }
                    sink.addAndCloseTexture(res, newImage);
                } catch (Exception ex) {
                    getLogger().warn("Failed to generate record item texture for {}. No model / texture found", e.getKey());
                    sink.addAndCloseTexture(res, fallback.makeCopy());
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void amendPalette(Palette p) {
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
